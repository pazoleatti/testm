package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.FormType;

/**
 * User: avanteev
 */
public interface FormTypeService {
    int save(FormType formType);
    FormType get(int formTypeId);
    void delete(int formTypeId);
}
