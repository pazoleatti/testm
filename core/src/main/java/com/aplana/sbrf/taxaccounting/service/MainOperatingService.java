package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.VersionedObjectStatus;
import com.aplana.sbrf.taxaccounting.model.log.Logger;

import java.util.Date;

/**
 * User: avanteev
 * Основные операции над макетами
 */
public interface MainOperatingService {
    String SAVE_MESSAGE = "Версия макета не сохранена, обнаружены фатальные ошибки!";

    <T> int edit(T template, Date templateActualEndDate, Logger logger, TAUserInfo user);
    <T> int createNewType(T template, Date templateActualEndDate, Logger logger, TAUserInfo user);
    <T> int createNewTemplateVersion(T template, Date templateActualEndDate, Logger logger, TAUserInfo user);
    void deleteTemplate(int typeId, Logger logger, TAUserInfo user);
    boolean deleteVersionTemplate(int templateId, Logger logger, TAUserInfo user);
    boolean setStatusTemplate(int templateId, Logger logger, TAUserInfo user, boolean force);

    /**
     * Просто делегирует к другому методу
     */
    void isInUsed(int templateId, int typeId, VersionedObjectStatus status, Date versionActualDateStart, Date versionActualDateEnd, Logger logger);
}
