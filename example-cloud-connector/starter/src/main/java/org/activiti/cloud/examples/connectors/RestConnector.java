package org.activiti.cloud.examples.connectors;

import org.activiti.cloud.api.process.model.IntegrationRequest;
import org.activiti.cloud.api.process.model.IntegrationResult;
import org.activiti.cloud.connectors.starter.channels.IntegrationResultSender;
import org.activiti.cloud.connectors.starter.configuration.ConnectorProperties;
import org.activiti.cloud.connectors.starter.model.IntegrationResultBuilder;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.Message;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@EnableBinding(RestConnector.Channels.class)
public class RestConnector {

    interface Channels {
        public final String POST = "restConnectorPost";

        @Input(POST)
        SubscribableChannel restConnectorPost();
    }

    private final IntegrationResultSender integrationResultSender;
    private final ConnectorProperties connectorProperties;


    public RestConnector(IntegrationResultSender integrationResultSender,
                         ConnectorProperties connectorProperties) {
        this.integrationResultSender = integrationResultSender;
        this.connectorProperties = connectorProperties;
    }

    @StreamListener(Channels.POST)
    public void handlePost(IntegrationRequest integrationRequest) {
        Map<String, Object> result = new HashMap<>();

        result.put("restStatus", 201);

        Message<IntegrationResult> message = IntegrationResultBuilder.resultFor(integrationRequest, connectorProperties)
                                                                     .withOutboundVariables(result)
                                                                     .buildMessage();

        integrationResultSender.send(message);
    }

}
