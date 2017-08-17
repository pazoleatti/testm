package com.aplana.sbrf.taxaccounting.service.refbook;

import com.aplana.sbrf.taxaccounting.model.refbook.RefBookDeclarationType;

import java.util.List;

/**
 * Created by aokunev on 09.08.2017.
 */
public interface RefBookDeclarationTypeService {
    List<RefBookDeclarationType> fetchAllDeclarationTypes();
}
