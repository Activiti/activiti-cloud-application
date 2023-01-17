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

package org.activiti.cloud.examples.connectors;

import java.util.function.Consumer;
import org.activiti.cloud.api.process.model.CloudBpmnError;
import org.activiti.cloud.api.process.model.IntegrationRequest;
import org.activiti.cloud.common.messaging.functional.Connector;
import org.activiti.cloud.common.messaging.functional.ConnectorBinding;
import org.activiti.cloud.common.messaging.functional.FunctionBinding;
import org.activiti.cloud.connectors.starter.channels.IntegrationErrorSender;
import org.activiti.cloud.connectors.starter.configuration.ConnectorProperties;
import org.activiti.cloud.connectors.starter.model.IntegrationErrorBuilder;
import org.activiti.cloud.examples.connectors.TestBpmnErrorConnector.Channels;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.stereotype.Component;

@ConnectorBinding(input = Channels.CHANNEL, condition = "", outputHeader = "")
@Component(Channels.CHANNEL + "Connector")
public class TestBpmnErrorConnector implements Connector<IntegrationRequest, Void> {

    private IntegrationErrorSender integrationErrorSender;
    private ConnectorProperties connectorProperties;

    public TestBpmnErrorConnector(
        IntegrationErrorSender integrationErrorSender,
        ConnectorProperties connectorProperties
    ) {
        this.integrationErrorSender = integrationErrorSender;
        this.connectorProperties = connectorProperties;
    }

    public interface Channels {
        String CHANNEL = "testBpmnErrorConnectorInput";

        SubscribableChannel testBpmnErrorConnectorInput();
    }

    @Override
    public Void apply(IntegrationRequest event) {
        handle(event);
        return null;
    }

    public void handle(IntegrationRequest integrationRequest) {
        CloudBpmnError bpmnError = new CloudBpmnError("CLOUD_BPMN_ERROR");
        integrationErrorSender.send(
            IntegrationErrorBuilder.errorFor(integrationRequest, connectorProperties, bpmnError).buildMessage()
        );
    }
}
