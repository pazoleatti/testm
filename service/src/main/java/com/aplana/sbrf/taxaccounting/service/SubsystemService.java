package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.Subsystem;

public interface SubsystemService {
    PagingResult<Subsystem> findByName(String name);
}
