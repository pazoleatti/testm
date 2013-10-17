package com.aplana.sbrf.taxaccounting.scheduler.api.exception;

/**
 * Ошибки планировщика при работе с пользовательскими параметрами
 * @author dloshkarev
 */
public class InvalidTaskParamException extends TaskSchedulingException {
    private static final long serialVersionUID = -6571207652425527859L;

    public InvalidTaskParamException(String errorStr, Throwable cause) {
        super(errorStr, cause);
    }

    public InvalidTaskParamException(String errorStr) {
        super(errorStr);
    }

    public InvalidTaskParamException(Throwable cause) {
        super(cause);
    }
}
