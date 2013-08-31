package com.aplana.sbrf.taxaccounting.service;

import javax.ejb.Local;
import javax.ejb.Stateless;

/**
 * EJB-модуль миграции
 */
@Stateless
@Local(MessageServiceLocal.class)
public class MigrationBean implements MessageService {
    @Override
    public String runImport() {
        // Debug
        return "Success";
    }
}