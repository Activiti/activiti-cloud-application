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

import org.activiti.cloud.api.process.model.IntegrationRequest;
import org.activiti.cloud.api.process.model.IntegrationResult;
import org.activiti.cloud.common.messaging.functional.Connector;
import org.activiti.cloud.common.messaging.functional.ConnectorBinding;
import org.activiti.cloud.connectors.starter.channels.IntegrationResultSender;
import org.activiti.cloud.connectors.starter.configuration.ConnectorProperties;
import org.activiti.cloud.connectors.starter.model.IntegrationResultBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@ConnectorBinding(input = RestConnector.Channels.POST, condition = "")
@Component
public class RestConnector implements Connector<IntegrationRequest, Void> {

    private final IntegrationResultSender integrationResultSender;
    private final ConnectorProperties connectorProperties;

    interface Channels {
        public final String POST = "restConnectorPost";

        SubscribableChannel restConnectorPost();
    }

    public RestConnector(IntegrationResultSender integrationResultSender, ConnectorProperties connectorProperties) {
        this.integrationResultSender = integrationResultSender;
        this.connectorProperties = connectorProperties;
    }

    @Override
    public Void apply(IntegrationRequest event) {
        handlePost(event);
        return null;
    }

    public void handlePost(IntegrationRequest integrationRequest) {
        Map<String, Object> result = new HashMap<>();

        result.put("restStatus", 201);

        Message<IntegrationResult> message = IntegrationResultBuilder
            .resultFor(integrationRequest, connectorProperties)
            .withOutboundVariables(result)
            .buildMessage();

        integrationResultSender.send(message);
    }
}
