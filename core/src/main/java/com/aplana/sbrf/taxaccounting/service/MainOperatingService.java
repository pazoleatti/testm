package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.model.log.Logger;

import java.util.Date;

/**
 * User: avanteev
 * Основные операции над макетами
 */
public interface MainOperatingService {
    public static int ONE_DAY_MILLISECONDS = 86400000;

    <T> int edit(T template, Date templateActualEndDate, Logger logger, TAUser user);
    <T> int createNewType(T template, Date templateActualEndDate, Logger logger, TAUser user);
    <T> int createNewTemplateVersion(T template, Date templateActualEndDate, Logger logger, TAUser user);
    void deleteTemplate(int typeId, Logger logger, TAUser user);
    void deleteVersionTemplate(int templateId, Date templateActualEndDate, Logger logger, TAUser user);
    void setStatusTemplate(int templateId, Logger logger, TAUser user);
}
