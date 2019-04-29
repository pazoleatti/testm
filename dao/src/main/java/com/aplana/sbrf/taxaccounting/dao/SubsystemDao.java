package com.aplana.sbrf.taxaccounting.dao;

import com.aplana.sbrf.taxaccounting.model.Subsystem;

/**
 * Доступ к справочнику "Участники информационного обмена" SUBSYSTEM.
 */
public interface SubsystemDao {
    Subsystem findById(long id);
}
