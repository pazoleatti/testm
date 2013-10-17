package com.aplana.sbrf.taxaccounting.scheduler.api.exception;

/**
 * Общее исключение, выбрасываемое при работе с планировщиком задач
 * @author dloshkarev
 */
public class TaskSchedulingException extends Exception {
    private static final long serialVersionUID = -6744231314510260076L;

    public TaskSchedulingException(String errorStr, Throwable cause) {
        super(errorStr, cause);
    }

    public TaskSchedulingException(String errorStr) {
        super(errorStr);
    }

    public TaskSchedulingException(Throwable cause) {
        super(cause);
    }
}
