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

import java.util.concurrent.TimeUnit;


/**
 * Utility class for publishing Activiti Cloud Task specific metrics via Micrometer.
 *
 */
public class ProcessMetrics {

    private final static Logger log = LoggerFactory.getLogger(ProcessMetrics.class);

    public static final String ACTIVITI_PROCESS_INSTANCE_TIMER = "activiti.process.instance";
    public static final String ACTIVITI_PROCESS_INSTANCE_STARTED = "activiti.process.instance.started";
    public static final String ACTIVITI_PROCESS_INSTANCE_COMPLETED = "activiti.process.instance.completed";
    public static final String ACTIVITI_PROCESS_INSTANCE_CANCELLED = "activiti.process.instance.cancelled";

    public static final String STATUS_COMPLETED = "completed";
    public static final String STATUS_CANCELLED = "cancelled";

    public static final String PROCESS_NAME_TAG = "process.name";
    public static final String PROCESS_DEFINITION_NAME_TAG = "process.definition.name";
    public static final String PROCESS_DEFINITION_ID_TAG = "process.definition.id";
    public static final String PROCESS_DEFINITION_KEY_TAG = "process.definition.key";
    public static final String PROCESS_DEFINITION_VERSION_TAG = "process.definition.version";
    public static final String PROCESS_INSTANCE_ID_TAG = "process.instance.id";
    public static final String PROCESS_STATUS_TAG = "process.status";

    private final MeterRegistry meterRegistry;

    public ProcessMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public void onProcessStart(ProcessExecution processExecution) {
        Counter.builder(ACTIVITI_PROCESS_INSTANCE_STARTED)
               .tags(commonTags(processExecution))
               .register(meterRegistry)
               .increment();
    }

    public void onProcessCancel(ProcessExecution processExecution, Throwable throwable) {
        Counter.builder(ACTIVITI_PROCESS_INSTANCE_CANCELLED)
               .tags(commonTags(processExecution))
               .register(meterRegistry)
               .increment();
    }

    public void onProcessComplete(ProcessExecution processExecution) {
        Timer.builder(ACTIVITI_PROCESS_INSTANCE_TIMER)
             .tags(commonTags(processExecution))
             .tag(PROCESS_STATUS_TAG, STATUS_COMPLETED)
             .register(meterRegistry)
             .record(processExecution.getEndTime().getTime()-processExecution.getStartTime().getTime(),
                     TimeUnit.MILLISECONDS);

        Counter.builder(ACTIVITI_PROCESS_INSTANCE_COMPLETED)
               .tags(commonTags(processExecution))
               .register(meterRegistry)
               .increment();
    }

    private Tags commonTags(ProcessExecution processExecution) {
        return Tags.of(PROCESS_NAME_TAG, processExecution.getProcessName())
                   .and(PROCESS_DEFINITION_NAME_TAG, processExecution.getProcessDefinitionName())
                   .and(PROCESS_DEFINITION_KEY_TAG, processExecution.getProcessDefinitionKey())
                   .and(PROCESS_DEFINITION_VERSION_TAG, processExecution.getProcessDefinitionVersion())
                   .and(PROCESS_DEFINITION_ID_TAG, processExecution.getProcessDefinitionId())
//                   .and(PROCESS_INSTANCE_ID_TAG, (processExecution.getProcessInstanceId() == null) ? "unknown"
//                                                            : processExecution.getProcessInstanceId())
        ;
    }

}
