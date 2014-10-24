package com.aplana.sbrf.taxaccounting.common.service;

/**
 * Общее исключение, выбрасываемое при работе с common-api
 * @author aivanov
 */
public class CommonServiceException extends Exception {

    private static final long serialVersionUID = 3668701744115263174L;

    public CommonServiceException(String errorStr, Throwable cause) {
        super(errorStr, cause);
    }

    public CommonServiceException(String errorStr) {
        super(errorStr);
    }

    public CommonServiceException(Throwable cause) {
        super(cause);
    }
}
