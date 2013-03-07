package com.aplana.sbrf.taxaccounting.web.main.api.server;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.TaActionException;
import com.gwtplatform.dispatch.shared.Action;
import com.gwtplatform.dispatch.shared.ActionException;

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

	private static final String ERROR_MESSAGE = "Операция %s не выполнена. ";

	private String getErrorMessage(String errName) {
		return String.format(ERROR_MESSAGE, errName);
	}

	@AfterThrowing(pointcut = "target(com.gwtplatform.dispatch.server.actionhandler.ActionHandler) &&"
			+ "args(action,..)", throwing = "e")
	public void handleException(@SuppressWarnings("rawtypes") Action action,
			Exception e) throws ActionException {
		String actionName = "";
		
		if (action instanceof ActionName) {
			actionName = ((ActionName) action).getName();
			if (actionName != null && !actionName.isEmpty()) {
				actionName = "\"" + actionName + "\"";
			}
		}

		if (e instanceof ActionException) {
			throw new ActionException(getErrorMessage(actionName), e);
		} else if (e instanceof ServiceLoggerException) {
			throw new TaActionException(
					getErrorMessage(actionName)
							+ (e.getLocalizedMessage() != null ? e.getLocalizedMessage()
									: ""),
					((ServiceLoggerException) e).getLogEntries());
		} else if (e instanceof ServiceException) {
			throw new TaActionException(
					getErrorMessage(actionName)
							+ (e.getLocalizedMessage() != null ? e
									.getLocalizedMessage() : ""));
		} else {
			throw new TaActionException(
					getErrorMessage(actionName)
							+ (e.getLocalizedMessage() != null ? e.getLocalizedMessage()
									: ""), formatException(e));
		}

	}

	/**
	 * <p>
	 * Метод преобразующий стек трейс исключения в формат строки.
	 * </p>
	 * 
	 * @param e
	 *            брошенное исключение
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
