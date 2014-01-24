package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.DeclarationType;

import java.util.List;

/**
 * User: avanteev
 */
public interface DeclarationTypeService {
    int save(DeclarationType type);
    DeclarationType get(int typeId);
    void delete(int typeId);
    List<DeclarationType> listAll();
}
