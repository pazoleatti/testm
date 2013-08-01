package com.aplana.sbrf.taxaccounting.service.impl.eventhandler;

import java.util.Map;

import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.aplana.sbrf.taxaccounting.log.Logger;
import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.service.eventhendler.FormDataEventHandler;

@Component
public class EventHandlerSimpleLauncher {
	
	@Autowired
	private ListableBeanFactory beanFactory;
	
	public void  process(TAUserInfo userInfo, FormData formData, FormDataEvent event, Logger logger,  Map<String, Object> additionalParameters){
		Map<String, FormDataEventHandler> handlers = beanFactory.getBeansOfType(FormDataEventHandler.class);
		for (FormDataEventHandler handler : handlers.values()) {
			handler.handle(userInfo, formData, event, logger, additionalParameters);
		}
	}

}
