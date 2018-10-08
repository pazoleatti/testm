package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.TAUser;

import java.util.List;

/**
 * Методы работы с ДУЛ.
 */
public interface IdDocService {

    /**
     * Удаление ДУЛ-ов.
     */
    void deleteByIds(List<Long> ids, TAUser requestingUser);
}
