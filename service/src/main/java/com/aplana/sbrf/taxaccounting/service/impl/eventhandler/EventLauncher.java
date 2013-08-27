package com.aplana.sbrf.taxaccounting.service.impl.eventhandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.log.Logger;

@Component
public class EventLauncher {
	
	@Autowired
	private ListableBeanFactory beanFactory;
	
	public void  process(TAUserInfo userInfo, FormData formData, FormDataEvent event, Logger logger,  Map<String, Object> additionalParameters){
		Map<String, EventHandler> handlers = beanFactory.getBeansOfType(EventHandler.class);
		List<EventHandler> handlersOrd = new ArrayList<EventHandler>();
		handlersOrd.addAll(handlers.values());
		Collections.sort(handlersOrd, new Comparator<EventHandler>() {
			@Override
			public int compare(EventHandler o1, EventHandler o2) {
				return Integer.valueOf(o2.priority()).compareTo(o1.priority());
			}
		});
		
		for (EventHandler handler : handlersOrd) {
			handler.handle(userInfo, formData, event, logger, additionalParameters);
		}
	}

}
