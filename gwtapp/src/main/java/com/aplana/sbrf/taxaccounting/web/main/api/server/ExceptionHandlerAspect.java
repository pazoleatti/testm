package com.aplana.sbrf.taxaccounting.web.main.api.server;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.TaActionException;
import com.aplana.sbrf.taxaccounting.web.main.api.shared.icommon.ActionName;
import com.gwtplatform.dispatch.shared.Action;
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
	
	private static final String ERROR_MESSAGE = "Операция %s не выполнена. ";
	
	private String getErrorMessage(String errName){
		return String.format(ERROR_MESSAGE,errName);
	}
	
	@AfterThrowing(pointcut="target(com.gwtplatform.dispatch.server.actionhandler.ActionHandler) &&" +
					"args(action,..)", throwing="e")
	public void handleException(@SuppressWarnings("rawtypes") Action action, Exception e) throws ActionException {	
		String actionName;
		if(action instanceof ActionName){
			actionName = ((ActionName)action).getName();
			if(actionName.length() == 0)
				actionName = "";
			else
				actionName = "\"" +  actionName + "\"";
		}
		else
			actionName = "";
			
		if (e instanceof ActionException) {
			throw new ActionException(getErrorMessage(actionName),e);
		} else if (e instanceof ServiceLoggerException) {
			throw new TaActionException(getErrorMessage(actionName) + (e.getLocalizedMessage() != null?e.getLocalizedMessage():""),
					((ServiceLoggerException) e).getLogEntries());
		} else {
			log.error(e);
			throw new TaActionException(getErrorMessage(actionName) + (e.getLocalizedMessage() != null?e.getLocalizedMessage():""));
		}
			
	}
	
    /**
     * <p>
     * Метод преобразующий стек трейс исключения в формат строки.
     * </p>
     *
     * @param e - брошенное исключение
     * @return - строка содержащая в себе стектрейс исключения
     */
    private static String formatException(final Throwable e) {
        if (e != null) {
            StringBuilder sb = new StringBuilder();
            String[] throwableStrRep = null;//new ThrowableInformation(e).getThrowableStrRep();
            for (String aThrowableStrRep : throwableStrRep) {
                sb.append(aThrowableStrRep).append("\n");
            }
            return sb.toString();
        }
        return null;
    }

}
