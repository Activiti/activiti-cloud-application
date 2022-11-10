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
package org.activiti.cloud.examples.configurations;

import java.util.Collections;
import org.activiti.cloud.examples.connectors.HeadersConnectorChannels;
import org.activiti.cloud.examples.routers.HeaderNotNullRoutingCallback;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.cloud.function.context.FunctionCatalog;
import org.springframework.cloud.function.context.config.RoutingFunction;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.expression.BeanFactoryResolver;

@Configuration
public class ExampleRoutingConfiguration {

  @Bean(HeadersConnectorChannels.HEADERS_CONNECTOR_ROUTER)
  public RoutingFunction headersConnectorRouter(FunctionCatalog functionCatalog, BeanFactory beanFactory) {
    HeaderNotNullRoutingCallback routingCallback = new HeaderNotNullRoutingCallback("processDefinitionVersion",
        HeadersConnectorChannels.HEADERS_CONNECTOR_CONSUMER);
    return new RoutingFunction(functionCatalog, Collections.emptyMap(), new BeanFactoryResolver(beanFactory), routingCallback);
  }

}
