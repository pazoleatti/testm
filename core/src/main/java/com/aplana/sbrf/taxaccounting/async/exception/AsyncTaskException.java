package com.aplana.sbrf.taxaccounting.async.exception;

/**
 * Общее исключение, выбрасываемое при работе с асихнронными задачами
 * @author dloshkarev
 */
public class AsyncTaskException extends Exception {
    private static final long serialVersionUID = 8993699486853641401L;

    public AsyncTaskException(String errorStr, Throwable cause) {
        super(errorStr, cause);
    }

    public AsyncTaskException(String errorStr) {
        super(errorStr);
    }

    public AsyncTaskException(Throwable cause) {
        super(cause);
    }
}
