package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.FormType;
import com.aplana.sbrf.taxaccounting.model.TemplateFilter;

import java.util.List;

/**
 * User: avanteev
 */
public interface FormTypeService {
    int save(FormType formType);
    FormType get(int formTypeId);
    void delete(int formTypeId);
    List<FormType> getByFilter(TemplateFilter filter);
}
