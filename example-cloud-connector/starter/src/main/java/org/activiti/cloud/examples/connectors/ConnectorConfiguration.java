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

import static org.activiti.cloud.common.messaging.utilities.InternalChannelHelper.INTERNAL_CHANNEL_PREFIX;

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
        RestConnector.Channels,
        TestErrorConnector.Channels {

    @Bean(INTERNAL_CHANNEL_PREFIX + ExampleConnectorChannels.EXAMPLE_CONNECTOR_CONSUMER)
    @Override
    public SubscribableChannel exampleConnectorConsumer() {
        return MessageChannels.publishSubscribe(ExampleConnectorChannels.EXAMPLE_CONNECTOR_CONSUMER).get();
    }

    @Bean(INTERNAL_CHANNEL_PREFIX + HeadersConnectorChannels.HEADERS_CONNECTOR_CONSUMER)
    @Override
    public SubscribableChannel headersConnectorConsumer() {
        return MessageChannels.publishSubscribe(HeadersConnectorChannels.HEADERS_CONNECTOR_CONSUMER).get();
    }

    @Bean(INTERNAL_CHANNEL_PREFIX + MoviesDescriptionConnectorChannels.MOVIES_DESCRIPTION_CONSUMER)
    @Override
    public SubscribableChannel moviesDescriptionConsumer() {
        return MessageChannels.publishSubscribe(MoviesDescriptionConnectorChannels.MOVIES_DESCRIPTION_CONSUMER).get();
    }

    @Bean(INTERNAL_CHANNEL_PREFIX + MultiInstanceConnector.Channels.CHANNEL)
    @Override
    public SubscribableChannel miCloudConnectorInput() {
        return MessageChannels.publishSubscribe(MultiInstanceConnector.Channels.CHANNEL).get();
    }

    @Bean(INTERNAL_CHANNEL_PREFIX + TestBpmnErrorConnector.Channels.CHANNEL)
    @Override
    public SubscribableChannel testBpmnErrorConnectorInput() {
        return MessageChannels.publishSubscribe(TestBpmnErrorConnector.Channels.CHANNEL).get();
    }

    @Bean(INTERNAL_CHANNEL_PREFIX + TestErrorConnector.Channels.CHANNEL)
    @Override
    public SubscribableChannel testErrorConnectorInput() {
        return MessageChannels.publishSubscribe(TestErrorConnector.Channels.CHANNEL).get();
    }

    @Bean(INTERNAL_CHANNEL_PREFIX + RestConnector.Channels.POST)
    @Override
    public SubscribableChannel restConnectorPost() {
        return MessageChannels.publishSubscribe(RestConnector.Channels.POST).get();
    }
}
