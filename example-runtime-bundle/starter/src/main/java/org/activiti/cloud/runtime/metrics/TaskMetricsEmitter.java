/*
 * Copyright 2017-2020 Alfresco Software, Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.cloud.runtime.metrics;

import cern.jet.random.Normal;
import cern.jet.random.engine.MersenneTwister64;
import cern.jet.random.engine.RandomEngine;
import io.micrometer.core.instrument.Metrics;
import lombok.Builder;
import lombok.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

@Component
public class TaskMetricsEmitter {
    private final static Logger log = LoggerFactory.getLogger(TaskMetricsEmitter.class);

    private static final List<String> outcomes = Arrays.asList("Approved", "Rejected", "Updated");
    private static final List<String> users = Arrays.asList("hruser", "testuser", "hradmin");
    private static final List<String> names = Arrays.asList("Submit", "Review", "Update");
    private static final List<String> groups = Arrays.asList("", "hrusers", "hradmins");

    private static final Map<String, ProcessDefinition> processDefinitions = Map.of("pd1",
                                                                                    ProcessDefinition.builder()
                                                                                                     .id("pd1")
                                                                                                     .name("Process Name 1")
                                                                                                     .key("key1")
                                                                                                     .version("1")
                                                                                                     .build(),
                                                                                    "pd2",
                                                                                    ProcessDefinition.builder()
                                                                                                     .id("pd2")
                                                                                                     .name("Proces Name 2")
                                                                                                     .key("key2")
                                                                                                     .version("1")
                                                                                                     .build(),
                                                                                    "pd3",
                                                                                    ProcessDefinition.builder()
                                                                                                     .id("pd3")
                                                                                                     .name("Process Name 3")
                                                                                                     .key("key1")
                                                                                                     .version("1")
                                                                                                     .build());

    static final Map<Long, Map.Entry<ProcessExecution, CountDownLatch>> processInstances = new ConcurrentHashMap<>();
    static final Map<Long, Map.Entry<TaskExecution, CountDownLatch>> tasks = new ConcurrentHashMap<>();

    @EventListener(ApplicationReadyEvent.class)
    public void createMetrics() {
        RandomEngine r = new MersenneTwister64(0);
        Normal incomingRequests = new Normal(0,1,r);
        Normal duration = new Normal(30,120,r);
        Normal delay = new Normal(10,30, r);
        Normal outcome = new Normal(0,1,r);

        TaskMetrics taskMetrics = new TaskMetrics(Metrics.globalRegistry);
        ProcessMetrics processMetrics = new ProcessMetrics(Metrics.globalRegistry);

        AtomicInteger latencyForThisSecond = new AtomicInteger(duration.nextInt());

        Flux.interval(Duration.ofSeconds(1))
            .doOnEach(d -> latencyForThisSecond.set(duration.nextInt()))
            .subscribe();

        // the potential for the "new process instance" every 30s
        Flux.interval(Duration.ofSeconds(10))
            .doOnEach(tick -> {
                if (incomingRequests.nextDouble() + 0.4 > 0 && processInstances.size() < 10) {

                    synchronized (processInstances) {
                        Long sample = tick.get();

                        ProcessDefinition processDefinition = toProcessDefinition(sample);

                        ProcessExecution processExecution = ProcessExecution.builder()
                                                                            .processInstanceId("pi" + sample)
                                                                            .startTime(new Date())
                                                                            .processName("Process " + sample)
                                                                            .processDefinitionKey(processDefinition.getKey())
                                                                            .processDefinitionId(processDefinition.getId())
                                                                            .processDefinitionVersion(processDefinition.getVersion())
                                                                            .processDefinitionName(processDefinition.getName())
                                                                            .build();

                        processInstances.put(sample,
                                             Map.entry(processExecution, new CountDownLatch(3)));

                        processMetrics.onProcessStart(processExecution);

                        log.info("{} -> onProcessStart: {}", sample, processExecution);
                    }
                }
            })
            .subscribe();

        // the potential for the "new task" every 10s
        Flux.interval(Duration.ofSeconds(10))
            .doOnEach(tick -> {
                if (incomingRequests.nextDouble() + 0.4 > 0 && tasks.size() < 20 && !processInstances.isEmpty()) {
                    int taskDuration = Math.abs(duration.nextInt());

                    synchronized (tasks) {
                        Long sample = tick.get();
                        TaskExecution taskExecution = TaskMetricsEmitter.toTaskExecution(sample);

                        taskMetrics.onTaskCreate(taskExecution);

                        Duration taskStart = Duration.ofSeconds(Math.abs(delay.nextInt()));

                        Mono.just(sample)
                            .doOnSubscribe(s -> {
                                tasks.put(sample,
                                          Map.entry(taskExecution,
                                                    new CountDownLatch(taskDuration)));

                                taskMetrics.onTaskStart(taskExecution);

                                log.info("{} -> onTaskStart with delay: {}, {}",
                                         sample,
                                         taskStart,
                                         taskExecution);
                            })
                            .delaySubscription(taskStart)
                            .subscribe();


                        log.info("{} -> onTaskCreate: start={}, duration={}, {}",
                                 sample,
                                 taskStart,
                                 taskDuration,
                                 taskExecution);
                    }
                }
            })
            .subscribe();

        // Complete tasks
        Flux.interval(Duration.ofSeconds(1))
            .doOnEach(tick -> {
                synchronized (tasks) {
                    for (Map.Entry<Long, Map.Entry<TaskExecution, CountDownLatch>> e : tasks.entrySet()) {
                        Long sampleId = e.getKey();
                        CountDownLatch latch = e.getValue()
                                                .getValue();

                        latch.countDown();

                        if (latch.getCount() == 0) {
                            TaskExecution taskExecution = e.getValue()
                                                           .getKey();

                            taskExecution.setEndTime(new Date());

                            if (outcome.nextDouble() + 0.1 > 0) {
                                taskMetrics.onTaskCancel(taskExecution,
                                                         new RuntimeException("error"));
                                log.info("{} -> onTaskCancel: {}",
                                         sampleId,
                                         taskExecution);
                            } else {
                                taskExecution.setOutcome(toOutcome(sampleId));
                                taskMetrics.onTaskComplete(taskExecution);
                                log.info("{} -> onTaskComplete: {}",
                                         sampleId,
                                         taskExecution);
                            }

                            tasks.remove(sampleId);

                            synchronized (processInstances) {
                                findProcessInstance(taskExecution.getProcessInstanceId()).ifPresent(it -> {
                                    Long processSampleId = it.getKey();
                                    CountDownLatch processLatch = it.getValue()
                                                                    .getValue();
                                    ProcessExecution processExecution = it.getValue()
                                                                          .getKey();

                                    processLatch.countDown();

                                    if (processLatch.getCount() == 0) {
                                        processExecution.setEndTime(new Date());

                                        if (outcome.nextDouble() + 0.05 > 0) {
                                            processMetrics.onProcessCancel(processExecution,
                                                                           new RuntimeException("error"));
                                            log.info("{} -> onProcessCancel: {}",
                                                     processSampleId,
                                                     processExecution);
                                        } else {
                                            processMetrics.onProcessComplete(processExecution);
                                            log.info("{} -> onProcessComplete: {}",
                                                     processSampleId,
                                                     processExecution);
                                        }

                                        processInstances.remove(processSampleId);
                                    }
                                });
                            }
                        }
                    }
                }
            })
            .subscribe();
    }

    private static TaskExecution toTaskExecution(long i) {
        TaskExecution.TaskExecutionBuilder builder = TaskExecution.builder();

        Optional<ProcessExecution> processInstance = toProcessInstance(i);

        String taskName = toTaskName(i);

        builder.startTime(new Date())
               .activityId(taskName + "Id")
               .taskName(taskName)
               .assignee(toAssignee(i))
               .candidateGroup(toGroup(i))
        ;

        processInstance.ifPresent(it -> {
            ProcessDefinition processDefinition = toProcessDefinition(it.getProcessDefinitionId());

            builder.processInstanceId(it.getProcessInstanceId())
                   .processDefinitionId(it.getProcessDefinitionId())
                   .processDefinitionKey(processDefinition.getKey())
                   .processDefinitionName(processDefinition.getName())
                   .processDefinitionVersion(processDefinition.getVersion())
            ;
        });

        return builder.build();
    }

    private static String toOutcome(Long l) {
        Long index = Long.valueOf(l % outcomes.size());

        return outcomes.get(index.intValue());
    }

    private static String toAssignee(Long l) {
        Long index = Long.valueOf(l % users.size());

        return users.get(index.intValue());
    }

    private static String toGroup(Long l) {
        Long index = Long.valueOf(l % groups.size());

        return groups.get(index.intValue());
    }

    private static String toTaskName(Long l) {
        Long index = Long.valueOf(l % names.size());

        return names.get(index.intValue());
    }

    private static Optional<ProcessExecution> toProcessInstance(Long l) {
        Long index = Long.valueOf(l % processInstances.size());

        return Optional.ofNullable(new ArrayList<>(processInstances.entrySet()))
                       .filter(Predicate.not(ArrayList::isEmpty))
                       .map(entries -> entries.get(index.intValue())
                       .getValue()
                       .getKey());
    }

    private static Optional<Map.Entry<Long, Map.Entry<ProcessExecution, CountDownLatch>>> findProcessInstance(String processInstanceId) {
        return processInstances.entrySet()
                               .stream()
                               .filter(it -> it.getValue().getKey().getProcessInstanceId().equals(processInstanceId))
                               .findFirst();
    }

    private static ProcessDefinition toProcessDefinition(String processDefinitionId) {
        return processDefinitions.get(processDefinitionId);
    }

    private static String toProcessDefinitionId(Long l) {
        Long index = Long.valueOf(l % processDefinitions.size());

        return processDefinitions.keySet()
                                 .toArray(new String[] {})[index.intValue()];
    }

    private static ProcessDefinition toProcessDefinition(Long l) {
        String key = toProcessDefinitionId(l);

        return processDefinitions.get(key);
    }
}

@Value
@Builder
class ProcessDefinition {
    String id;
    String key;
    String name;
    String version;
}

