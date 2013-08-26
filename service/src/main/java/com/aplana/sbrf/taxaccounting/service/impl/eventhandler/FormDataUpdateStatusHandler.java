package com.aplana.sbrf.taxaccounting.service.impl.eventhandler;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.aplana.sbrf.taxaccounting.dao.FormDataDao;
import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.WorkflowMove;
import com.aplana.sbrf.taxaccounting.model.log.Logger;

/**
 * Обновление статуса
 * 
 * @author sgoryachkin
 */
@Component
public class FormDataUpdateStatusHandler implements EventHandler {

	@Autowired
	FormDataDao formDataDao;

	@Override
	public int priority() {
		return Integer.MAX_VALUE;
	}

	@Override
	public void handle(TAUserInfo userInfo, FormData formData,
			FormDataEvent event, Logger logger,
			Map<String, Object> additionalParameters) {

		for (WorkflowMove workflowMove : WorkflowMove.values()) {
			if (workflowMove.getEvent().equals(event)) {
				formDataDao.updateState(formData.getId(), workflowMove.getToState());
			}
		}
	}

}
