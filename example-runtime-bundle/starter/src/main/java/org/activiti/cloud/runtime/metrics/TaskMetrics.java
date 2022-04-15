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

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.concurrent.TimeUnit;


/**
 * Utility class for publishing Activiti Cloud Task specific metrics via Micrometer.
 *
 */
public class TaskMetrics {

    private final static Logger log = LoggerFactory.getLogger(TaskMetrics.class);

    /**
     * Task timer measurements. Records information about task duration and status.
     */
    public static final String ACTIVITI_CLOUD_TASK_METER = "activiti.user.task";

    /**
     * Task counter measurements.
     */
    public static final String ACTIVITI_CLOUD_TASK_CREATED_METER = "activiti.user.task.created";

    /**
     * Task counter measurements.
     */
    public static final String ACTIVITI_CLOUD_TASK_STARTED_METER = "activiti.user.task.started";

    /**
     * Task counter measurements.
     */
    public static final String ACTIVITI_CLOUD_TASK_COMPLETED_METER = "activiti.user.task.completed";

    /**
     * Task counter measurements.
     */
    public static final String ACTIVITI_CLOUD_TASK_CANCELLED_METER = "activiti.user.task.cancelled";

    public static final String ACTIVITI_USER_TASKS_GAUGE = "activiti.user.tasks";

    public static final String STATUS_COMPLETED = "completed";

    public static final String STATUS_CANCELLED = "cancelled";

    public static final String TASK_NAME_TAG = "task.name";

    public static final String TASK_ASSIGNEE_TAG = "task.assignee";

    public static final String TASK_CANDIDATE_GROUPS_TAG = "task.candidate_group";

    public static final String TASK_ACTIVITY_ID_TAG = "task.activity.id";

    public static final String TASK_PROCESS_DEFINITION_NAME_TAG = "task.process.definition.name";

    public static final String TASK_PROCESS_DEFINITION_ID_TAG = "task.process.definition.id";

    public static final String TASK_PROCESS_DEFINITION_KEY_TAG = "task.process.definition.key";

    public static final String TASK_PROCESS_DEFINITION_VERSION_TAG = "task.process.definition.version";

    public static final String TASK_PROCESS_INSTANCE_ID_TAG = "task.process.instance.id";

    public static final String TASK_OUTCOME_TAG = "task.outcome";

    public static final String TASK_STATUS_TAG = "task.status";

    private final MeterRegistry meterRegistry;

    public TaskMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public void onTaskCreate(TaskExecution taskExecution) {
        Counter.builder(ACTIVITI_CLOUD_TASK_CREATED_METER)
               .tags(commonTags(taskExecution))
               .register(meterRegistry)
               .increment();
    }

    public void onTaskStart(TaskExecution taskExecution) {
        Counter.builder(ACTIVITI_CLOUD_TASK_STARTED_METER)
               .tags(commonTags(taskExecution))
               .tag(TASK_ASSIGNEE_TAG, taskExecution.getAssignee())
               .register(meterRegistry)
               .increment();
    }

    public void onTaskCancel(TaskExecution taskExecution, Throwable throwable) {
        Counter.builder(ACTIVITI_CLOUD_TASK_CANCELLED_METER)
               .tags(commonTags(taskExecution))
               .tag(TASK_ASSIGNEE_TAG, taskExecution.getAssignee())
               .register(meterRegistry)
               .increment();
    }

    public void onTaskComplete(TaskExecution taskExecution) {
        Timer.builder(ACTIVITI_CLOUD_TASK_METER)
             .tags(commonTags(taskExecution))
             .tag(TASK_OUTCOME_TAG, Optional.ofNullable(taskExecution.getOutcome())
                                            .orElse(""))
             .tag(TASK_STATUS_TAG, STATUS_COMPLETED)
             .tag(TASK_ASSIGNEE_TAG, taskExecution.getAssignee())
             .register(meterRegistry)
             .record(taskExecution.getEndTime().getTime()-taskExecution.getStartTime().getTime(),
                     TimeUnit.MILLISECONDS);

        Counter.builder(ACTIVITI_CLOUD_TASK_COMPLETED_METER)
               .tags(commonTags(taskExecution))
               .tag(TASK_ASSIGNEE_TAG, taskExecution.getAssignee())
               .register(meterRegistry)
               .increment();
    }

    private Tags commonTags(TaskExecution taskExecution) {
        return Tags.of(TASK_NAME_TAG, taskExecution.getTaskName())
                   .and(TASK_PROCESS_DEFINITION_NAME_TAG, taskExecution.getProcessDefinitionName())
                   .and(TASK_PROCESS_DEFINITION_KEY_TAG, taskExecution.getProcessDefinitionKey())
                   .and(TASK_PROCESS_DEFINITION_VERSION_TAG, taskExecution.getProcessDefinitionVersion())
                   .and(TASK_CANDIDATE_GROUPS_TAG, taskExecution.getCandidateGroup())
                   .and(TASK_ACTIVITY_ID_TAG, taskExecution.getActivityId())
                   .and(TASK_PROCESS_DEFINITION_ID_TAG, taskExecution.getProcessDefinitionId())
//                   .and(TASK_PROCESS_INSTANCE_ID_TAG, (taskExecution.getProcessInstanceId() == null) ? "unknown"
//                                                            : taskExecution.getProcessInstanceId());
        ;
    }

}
