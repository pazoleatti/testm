package com.aplana.sbrf.taxaccounting.web.main.api.server;

import com.aplana.sbrf.taxaccounting.model.LockData;
import com.aplana.sbrf.taxaccounting.model.exception.AccessDeniedException;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.TaActionException;
import com.gwtplatform.dispatch.shared.Action;
import com.gwtplatform.dispatch.shared.ActionException;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import javax.ejb.EJBException;
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

    private String getErrorMessage(String errName, String msg) {
        if (msg == null || msg.isEmpty()) {
            return String.format(ERROR_MESSAGE, errName.isEmpty() ? "" : " \"" + errName + "\"");
        } else {
            return msg;
        }
    }

    @AfterThrowing(pointcut = "target(com.gwtplatform.dispatch.server.actionhandler.ActionHandler) && args(action,..)", throwing = "e")
    public void handleException(@SuppressWarnings("rawtypes") Action action, Throwable e) throws ActionException {

        LOG.error(e.getMessage(), e);

        String actionName = "";

        if (action instanceof ActionName)
            actionName = ((ActionName) action).getName();

        if (e instanceof TaActionException) {
            throw (TaActionException)e;
        } else if (e instanceof ServiceLoggerException) {
            TaActionException tae = new TaActionException(getErrorMessage(actionName, e.getLocalizedMessage()));
            tae.setUuid(((ServiceLoggerException) e).getUuid());
            //Сделал на случай если все таки надо будет отображать стек трейс
            tae.setNeedStackTrace(false);
            throw tae;
        } else if (e instanceof AccessDeniedException) {
            throw new TaActionException(getErrorMessage(actionName, e.getLocalizedMessage()));
        } else if (e instanceof org.springframework.security.access.AccessDeniedException) {
            throw new TaActionException(getErrorMessage(actionName, "Доступ запрещен"));
        } else if (e instanceof DaoException) {
            Throwable rootCause = ExceptionUtils.getRootCause(e);
            if (rootCause instanceof java.sql.SQLSyntaxErrorException && rootCause.getLocalizedMessage().contains("ORA-02049")) {
                throw new TaActionException(LockData.STANDARD_LOCK_MSG, formatException(e));
            } else {
                throw new TaActionException(getErrorMessage(actionName, null), formatException(e));
            }
        } if (e instanceof EJBException) {
            throw new TaActionException(getErrorMessage(actionName, e.getCause().getLocalizedMessage()), formatException(e));
        } else {
            throw new TaActionException(getErrorMessage(actionName, e.getLocalizedMessage()), formatException(e));
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