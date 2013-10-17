package com.aplana.sbrf.taxaccounting.scheduler.api.exception;

/**
 * Ошибки выполнения задачи
 * @author dloshkarev
 */
public class TaskExecutionException extends TaskSchedulingException {
    private static final long serialVersionUID = -4808831578181977623L;

    public TaskExecutionException(String errorStr, Throwable cause) {
        super(errorStr, cause);
    }

    public TaskExecutionException(String errorStr) {
        super(errorStr);
    }

    public TaskExecutionException(Throwable cause) {
        super(cause);
    }
}
