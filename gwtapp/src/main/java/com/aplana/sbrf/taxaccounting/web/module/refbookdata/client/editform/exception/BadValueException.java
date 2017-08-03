package com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.editform.exception;

import java.util.Map;

public class BadValueException extends AbstractBadValueException {
    public BadValueException(){
        super();
    }
    public BadValueException(Map<String, String> descriptionMap) {
        super(descriptionMap);
    }
}
