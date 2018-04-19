package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.DeclarationFormKind;
import com.aplana.sbrf.taxaccounting.model.DeclarationType;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.model.TemplateFilter;

import java.util.List;

/**
 * User: avanteev
 */
public interface DeclarationTypeService {
    int save(DeclarationType type);

    void updateDT(DeclarationType type);

    DeclarationType get(int typeId);

    void delete(int typeId);

    /**
     * Возвращяет список типов макетов
     */
    List<DeclarationType> fetchAll(TAUserInfo userInfo);

    List<DeclarationType> getByFilter(TemplateFilter filter);

    List<DeclarationType> getTypes(int departmentId, int reportPeriod, TaxType taxType, List<DeclarationFormKind> declarationFormKinds);
}
