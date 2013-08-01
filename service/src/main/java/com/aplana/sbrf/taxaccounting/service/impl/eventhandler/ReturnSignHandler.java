package com.aplana.sbrf.taxaccounting.service.impl.eventhandler;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.aplana.sbrf.taxaccounting.dao.FormDataDao;
import com.aplana.sbrf.taxaccounting.log.Logger;
import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.WorkflowMove;
import com.aplana.sbrf.taxaccounting.service.eventhendler.FormDataEventHandler;

/**
 * Нужно выставить флаг возврата при событиях указанных в аналитике. Во всех
 * остальных переходах нужно снимать этот флаг. В случае других событий (не
 * переходов WFM) ничего не делать.
 * 
 * http://conf.aplana.com/pages/viewpage.action?pageId=9595258
 * http://jira.aplana.com/browse/SBRFACCTAX-3261
 * 
 * 
 * 
 * @author sgoryachkin
 */
public class ReturnSignHandler implements FormDataEventHandler {

	@Autowired
	FormDataDao formDataDao;

	@Override
	public void handle(TAUserInfo userInfo, FormData formData,
			FormDataEvent event, Logger logger,
			Map<String, Object> additionalParameters) {

		for (WorkflowMove workflowMove : WorkflowMove.values()) {
			if (workflowMove.getEvent().equals(event)) {
				if (FormDataEvent.MOVE_APPROVED_TO_CREATED.equals(event)
						|| FormDataEvent.MOVE_PREPARED_TO_CREATED.equals(event)
						|| FormDataEvent.MOVE_APPROVED_TO_PREPARED
								.equals(event)) {
					formDataDao.updateReturnSign(formData.getId(), true);
				} else {
					formDataDao.updateReturnSign(formData.getId(), true);
				}
			}
		}
	}

}
