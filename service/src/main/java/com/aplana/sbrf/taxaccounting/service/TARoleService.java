package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.TARole;

import java.util.List;

public interface TARoleService {
    /**
     * Получить все роли
     */
	List<TARole> getAll();

    /**
     * Получить роль по алиасу
     */
	TARole getByAlias(String alias);
}
