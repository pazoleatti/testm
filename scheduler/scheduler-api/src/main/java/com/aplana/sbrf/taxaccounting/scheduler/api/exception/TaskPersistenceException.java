package com.aplana.sbrf.taxaccounting.scheduler.api.exception;

/**
 * Ошибки планировщика при работе с БД
 * @author dloshkarev
 */
public class TaskPersistenceException extends TaskSchedulingException {
    private static final long serialVersionUID = -1418911391851949855L;

    public TaskPersistenceException(String errorStr, Throwable cause) {
        super(errorStr, cause);
    }

    public TaskPersistenceException(String errorStr) {
        super(errorStr);
    }

    public TaskPersistenceException(Throwable cause) {
        super(cause);
    }
}
