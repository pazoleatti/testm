package com.aplana.sbrf.taxaccounting.async.exception;

/**
 * Эксепшен, выбрасываемый если один из параметров, переданных в асинхронную задачу не поддерживает сериализацию
 * @author dloshkarev
 */
public class AsyncTaskSerializationException extends AsyncTaskException {
    private static final long serialVersionUID = 6645267581078108804L;

    public AsyncTaskSerializationException(String errorStr, Throwable cause) {
        super(errorStr, cause);
    }

    public AsyncTaskSerializationException(String errorStr) {
        super(errorStr);
    }

    public AsyncTaskSerializationException(Throwable cause) {
        super(cause);
    }
}
