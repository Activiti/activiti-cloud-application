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

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.messaging.SubscribableChannel;

@Configuration
public class ConnectorConfiguration
    implements
        ExampleConnectorChannels,
        HeadersConnectorChannels,
        MoviesDescriptionConnectorChannels,
        MultiInstanceConnector.Channels,
        TestBpmnErrorConnector.Channels,
        TestErrorConnector.Channels{

    @Bean
    @Override
    public SubscribableChannel exampleConnectorConsumer() {
        return MessageChannels.publishSubscribe(ExampleConnectorChannels.EXAMPLE_CONNECTOR_CONSUMER).get();
    }

    @Override
    public SubscribableChannel headersConnectorConsumer() {
        return MessageChannels.publishSubscribe(HeadersConnectorChannels.HEADERS_CONNECTOR_CONSUMER).get();
    }

    @Override
    public SubscribableChannel moviesDescriptionConsumer() {
        return MessageChannels.publishSubscribe(MoviesDescriptionConnectorChannels.MOVIES_DESCRIPTION_CONSUMER).get();
    }

    @Override
    public SubscribableChannel miCloudConnectorInput() {
        return MessageChannels.publishSubscribe(MultiInstanceConnector.Channels.CHANNEL).get();
    }

    @Override
    public SubscribableChannel testBpmnErrorConnectorInput() {
        return MessageChannels.publishSubscribe(TestBpmnErrorConnector.Channels.CHANNEL).get();
    }

    @Override
    public SubscribableChannel testErrorConnectorInput() {
        return MessageChannels.publishSubscribe(TestErrorConnector.Channels.CHANNEL).get();
    }
}
