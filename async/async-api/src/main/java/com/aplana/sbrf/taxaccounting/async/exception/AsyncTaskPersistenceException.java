package com.aplana.sbrf.taxaccounting.async.exception;

/**
 * Ошибки при обработке асинхронных задач в бд
 * @author dloshkarev
 */
public class AsyncTaskPersistenceException extends AsyncTaskException {
    private static final long serialVersionUID = -5445612126118438237L;

    public AsyncTaskPersistenceException(String errorStr, Throwable cause) {
        super(errorStr, cause);
    }

    public AsyncTaskPersistenceException(String errorStr) {
        super(errorStr);
    }

    public AsyncTaskPersistenceException(Throwable cause) {
        super(cause);
    }
}
