package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.*;

import java.util.List;

/**
 * User: avanteev
 */
public interface FormTypeService {
    int save(FormType formType);
    void updateFormTypeName(int formTypeId, String newName);
    FormType get(int formTypeId);
    void delete(int formTypeId);
    List<FormType> getByFilter(TemplateFilter filter);
	List<FormType> getFormTypes(int departmentId, int reportPeriod, TaxType taxType, List<FormDataKind> kind);
    /**
     * Вид НФ по его уникальному коду
     */
    FormType getByCode(String code);
}
