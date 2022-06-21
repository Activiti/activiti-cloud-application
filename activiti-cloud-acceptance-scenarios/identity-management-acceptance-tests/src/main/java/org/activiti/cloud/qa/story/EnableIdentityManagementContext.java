package org.activiti.cloud.qa.story;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.activiti.cloud.acc.shared.rest.feign.EnableFeignContext;
import org.springframework.test.context.ContextConfiguration;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@ContextConfiguration(classes = {IdentityManagementTestsConfigurationProperties.class, IdentityManagementConfiguration.class})
@EnableFeignContext
public @interface EnableIdentityManagementContext {

}

