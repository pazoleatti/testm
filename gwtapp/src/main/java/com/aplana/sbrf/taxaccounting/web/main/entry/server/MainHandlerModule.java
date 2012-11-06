package com.aplana.sbrf.taxaccounting.web.main.entry.server;

import java.util.Map;

import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.gwtplatform.dispatch.server.actionhandler.ActionHandler;
import com.gwtplatform.dispatch.server.spring.DispatchModule;
import com.gwtplatform.dispatch.server.spring.HandlerModule;
import com.gwtplatform.dispatch.server.spring.configuration.DefaultModule;
import com.gwtplatform.dispatch.shared.Action;
import com.gwtplatform.dispatch.shared.Result;

@Configuration
@Import(value = { DefaultModule.class })
@ComponentScan(basePackageClasses = DispatchModule.class)
public class MainHandlerModule<A extends Action<R>, R extends Result> extends
		HandlerModule {

	@Autowired
	private ListableBeanFactory listableBeanFactory;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected void configureHandlers() {

		for (Map.Entry<String, ActionHandler> entry : listableBeanFactory
				.getBeansOfType(ActionHandler.class).entrySet()) {

			Class<A> actionClass = entry.getValue().getActionType();

			Class<? extends ActionHandler<A, R>> handlerClass = (Class<? extends ActionHandler<A, R>>) entry
					.getValue().getClass();

			bindHandler(actionClass, handlerClass);
		}

	}
}