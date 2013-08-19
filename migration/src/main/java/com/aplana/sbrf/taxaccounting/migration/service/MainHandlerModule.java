package com.aplana.sbrf.taxaccounting.migration.service;

import com.aplana.sbrf.taxaccounting.migration.web.server.StartHandler;
import com.aplana.sbrf.taxaccounting.migration.web.shared.StartAction;
import com.gwtplatform.dispatch.server.actionvalidator.ActionValidator;
import com.gwtplatform.dispatch.server.spring.DispatchModule;
import com.gwtplatform.dispatch.server.spring.HandlerModule;
import com.gwtplatform.dispatch.server.spring.actionvalidator.DefaultActionValidator;
import com.gwtplatform.dispatch.server.spring.configuration.DefaultModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(value = { DefaultModule.class })
@ComponentScan(basePackageClasses = DispatchModule.class)
public class MainHandlerModule extends HandlerModule {

    @Bean
    public ActionValidator getDefaultActionValidator() {
        return new DefaultActionValidator();
    }

    @Override
	protected void configureHandlers() {
        bindHandler(StartAction.class, StartHandler.class);
    }
}