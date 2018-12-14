package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.DeclarationType;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;

import java.util.List;

/**
 * User: avanteev
 */
public interface DeclarationTypeService {
    int save(DeclarationType type);

    DeclarationType get(int typeId);

    /**
     * Возвращяет список типов макетов
     */
    List<DeclarationType> fetchAll(TAUserInfo userInfo);
}
