package com.aplana.sbrf.taxaccounting.migration.service;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;

@Configuration
@Import(value = {MainHandlerModule.class})
@ComponentScan(basePackageClasses = MainServerModule.class)
@EnableAspectJAutoProxy
public class MainServerModule {
}
