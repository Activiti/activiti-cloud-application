/*
 * Copyright 2018 Alfresco, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.cloud.modeling;

import org.activiti.cloud.org.config.EnableActivitiOrganization;
import org.activiti.cloud.process.model.EnableActivitiProcessModel;
import org.activiti.cloud.services.process.model.jpa.version.ExtendedJpaRepositoryFactoryBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.web.client.RestTemplate;

/**
 * Modeling application
 */
@SpringBootApplication
//TODO: to remove EnableJpaRepositories and EntityScan from here as they should be in EnableActivitiProcessModel and EnableActivitiOrganization annotation (this is just a tempoarary fix).
@EnableJpaRepositories(
        basePackages = {"org.activiti.cloud.services.process.model.jpa", "org.activiti.cloud.services.organization.jpa"},
        repositoryFactoryBeanClass = ExtendedJpaRepositoryFactoryBean.class)
@EntityScan({"org.activiti.cloud.services.process.model.core.model", "org.activiti.cloud.services.organization.entity"})
@EnableActivitiProcessModel
@EnableActivitiOrganization
@ComponentScan("org.activiti.cloud")
public class ModelingApplication {

    public static void main(String[] args) {
        SpringApplication.run(ModelingApplication.class,
                              args);
    }
}
