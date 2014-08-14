package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.model.log.Logger;

import java.util.Date;

/**
 * User: avanteev
 * Основные операции над макетами
 */
public interface MainOperatingService {

    <T> int edit(T template, Date templateActualEndDate, Logger logger, TAUser user);
    <T> int createNewType(T template, Date templateActualEndDate, Logger logger, TAUser user);
    <T> int createNewTemplateVersion(T template, Date templateActualEndDate, Logger logger, TAUser user);
    void deleteTemplate(int typeId, Logger logger, TAUser user);
    boolean deleteVersionTemplate(int templateId, Logger logger, TAUser user);
    boolean setStatusTemplate(int templateId, Logger logger, TAUser user, boolean force);
}
