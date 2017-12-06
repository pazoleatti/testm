package com.aplana.sbrf.taxaccounting.script.service;

import com.aplana.sbrf.taxaccounting.service.ScriptExposed;

import java.util.List;
import java.util.Set;

@ScriptExposed
public interface PersonService {

    List<Long> getDuplicate(Set<Long> originalRecordId);

}
