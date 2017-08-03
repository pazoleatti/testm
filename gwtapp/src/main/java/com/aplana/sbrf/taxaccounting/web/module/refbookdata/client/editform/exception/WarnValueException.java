package com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.editform.exception;

import java.util.Map;

public class WarnValueException extends AbstractBadValueException {
    public WarnValueException(){
        super();
    }
    public WarnValueException(Map<String, String> descriptionMap) {
        super(descriptionMap);
    }
}
