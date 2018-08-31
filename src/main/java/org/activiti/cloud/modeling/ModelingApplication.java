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
import org.activiti.cloud.organization.EnableActivitiOrganization;
import org.activiti.cloud.process.model.EnableActivitiProcessModel;
import org.activiti.cloud.services.process.model.jpa.version.ExtendedJpaRepositoryFactoryBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;


/**
 * Modeling application
 */
@SpringBootApplication
// We need to remove this..
@EnableJpaRepositories(
        basePackages = {"org.activiti.cloud.services.organization.jpa", "org.activiti.cloud.services.process.model.jpa" },
        repositoryFactoryBeanClass = ExtendedJpaRepositoryFactoryBean.class
)
@EntityScan(basePackages = {"org.activiti.cloud.services.process.model.core.model", "org.activiti.cloud.services.organization.entity"})

// We need to definitely remove this
@ComponentScan("org.activiti.cloud")
@EnableActivitiOrganization
@EnableActivitiProcessModel
public class ModelingApplication {

    public static void main(String[] args) {
        SpringApplication.run(ModelingApplication.class,
                              args);
    }
}
