package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.log.Logger;

import java.util.Date;

/**
 * User: avanteev
 * Основные операции над макетами
 */
public interface MainOperatingService {
    public static int ONE_DAY_MILLISECONDS = 86400000;

    <T> int edit(T template, Date templateActualEndDate, Logger logger);
    <T> int createNewType(T template, Date templateActualEndDate, Logger logger);
    <T> int createNewTemplateVersion(T template, Date templateActualEndDate, Logger logger);
    void deleteTemplate(int typeId, Logger logger);
    void deleteVersionTemplate(int templateId, Date templateActualEndDate, Logger logger);
    void setStatusTemplate(int templateId, Logger logger);
}
