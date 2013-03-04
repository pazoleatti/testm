package com.aplana.sbrf.taxaccounting.web.main.api.server;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.TaActionException;
import com.gwtplatform.dispatch.shared.ActionException;

/**
 * Обработчик исключений для ActionHandler. Преобразует сервисные исключения в клиентские. 
 * 
 * @author sgoryachkin
 *
 */
@Aspect
@Component
public class ExceptionHandlerAspect {
	
	private final Log log = LogFactory.getLog(getClass());
	
	private String getErrorMessage(){
		return "Операция не выполнена";
	}
	
	@AfterThrowing(pointcut="target(com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler)", throwing="e")
	public void handleException(Exception e) throws ActionException {	
		if (e instanceof ActionException) {
			throw (ActionException)e;
		} else if (e instanceof ServiceLoggerException) {
			throw new TaActionException(getErrorMessage() + ": " + e.getLocalizedMessage(),
					((ServiceLoggerException) e).getLogEntries());
		} else {
			log.error(e);
			throw new TaActionException(getErrorMessage() + ": " + e.getLocalizedMessage());
		}	
	}

}
