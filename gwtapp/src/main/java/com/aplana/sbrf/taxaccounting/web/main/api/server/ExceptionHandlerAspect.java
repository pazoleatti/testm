package com.aplana.sbrf.taxaccounting.web.main.api.server;

import com.aplana.sbrf.taxaccounting.model.exception.AccessDeniedException;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.TaActionException;
import com.gwtplatform.dispatch.shared.Action;
import com.gwtplatform.dispatch.shared.ActionException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Обработчик исключений для ActionHandler. Преобразует сервисные исключения в
 * клиентские.
 * 
 * @author sgoryachkin
 * 
 */
@Aspect
@Component
public class ExceptionHandlerAspect {

	private static final String ERROR_MESSAGE = "Операция%s не выполнена. ";

	private static final Log LOG = LogFactory.getLog(ExceptionHandlerAspect.class);

	private String getErrorMessage(String errName) {
		return String.format(ERROR_MESSAGE, errName.isEmpty() ? "" : " \"" + errName + "\"");
	}

	@AfterThrowing(pointcut = "target(com.gwtplatform.dispatch.server.actionhandler.ActionHandler) && args(action,..)", throwing = "e")
	public void handleException(@SuppressWarnings("rawtypes") Action action, Throwable e) throws ActionException {

		LOG.error(e);

		String actionName = "";
		
		if (action instanceof ActionName)
			actionName = ((ActionName) action).getName();
		
		if (e instanceof TaActionException) {
			throw (TaActionException)e;
		} else if (e instanceof ServiceLoggerException) {
            TaActionException tae = new TaActionException(getErrorMessage(actionName) + (e.getLocalizedMessage() != null ? e.getLocalizedMessage() : ""));
            tae.setUuid(((ServiceLoggerException) e).getUuid());
            //Сделал на случай если все таки надо будет отображать стек трейс
            tae.setNeedStackTrace(false);
            throw tae;
        } else if (e instanceof AccessDeniedException) {
			throw new TaActionException(getErrorMessage(actionName) + (e.getLocalizedMessage() != null ? e.getLocalizedMessage() : ""));
		} else if (e instanceof org.springframework.security.access.AccessDeniedException) {
			throw new TaActionException(getErrorMessage(actionName) + "Доступ запрещен");
		} else if (e instanceof DaoException) {
			throw new TaActionException(getErrorMessage(actionName), formatException(e));
		} else {
			throw new TaActionException(getErrorMessage(actionName) + (e.getLocalizedMessage() != null ? e.getLocalizedMessage() : ""), formatException(e));
		}
	}

	/**
	 * Обернуть необработанные исключения в DaoException
	 * @param e
	 * @throws DaoException
	 */
	@AfterThrowing(pointcut = "execution(* com.aplana.sbrf.taxaccounting.dao.*.*(..))", throwing = "e")
	public void handleDaoException(Throwable e) throws DaoException {
		if (e instanceof DaoException) {
			throw (DaoException) e;
		} else {
			throw new DaoException(e.getLocalizedMessage(), e);
		}
	}

	/**
	 * Метод преобразующий стек трейс исключения в формат строки.
	 *
	 * @param e брошенное исключение
	 * @return строка содержащая в себе стектрейс исключения
	 */
	private static String formatException(Throwable e) {
		if (e != null) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			return sw.getBuffer().toString();
		} else {
			return null;
		}
	}
}
