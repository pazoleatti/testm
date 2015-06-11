package com.aplana.sbrf.taxaccounting.dao.aop;

import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * Обработчик исключений в dao. Обарачивает необработанные исключенияв в DaoException
 */
@Aspect
@Component
public class DaoExceptionHandlerAspect {
    /**
     * Обернуть необработанные исключения в DaoException
     * @param e
     * @throws com.aplana.sbrf.taxaccounting.model.exception.DaoException
     */
    @AfterThrowing(pointcut = "execution(* com.aplana.sbrf.taxaccounting.dao..*.*(..)) " +
            "&& !within(com.aplana.sbrf.taxaccounting.dao.impl.DBInfo)", throwing = "e")
    public void handleDaoException(Throwable e) throws DaoException {
        Throwable rootCause = ExceptionUtils.getRootCause(e);
        if (rootCause instanceof java.sql.SQLSyntaxErrorException && rootCause.getLocalizedMessage().contains("ORA-02049")) {
            throw new DaoException("Объект заблокирован другой операцией. Попробуйте выполнить операцию позже", e);
        }
        if (e instanceof DaoException) {
            throw (DaoException) e;
        } if (e instanceof  IllegalArgumentException) {
            throw (IllegalArgumentException) e;
        } else {
            throw new DaoException(e.getLocalizedMessage(), e);
        }
    }
}
