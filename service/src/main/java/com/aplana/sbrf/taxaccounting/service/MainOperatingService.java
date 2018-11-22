package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.Logger;

import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * User: avanteev
 * Основные операции над макетами
 */
public interface MainOperatingService {
    String SAVE_MESSAGE = "Версия макета не сохранена. Обнаружены фатальные ошибки!";

    <T> boolean edit(T template, Date templateActualEndDate, Logger logger, TAUserInfo user);
    <T> boolean edit(T template, List<DeclarationTemplateCheck> checks, Date templateActualEndDate, Logger logger, TAUserInfo user);
    <T> boolean edit(T template, List<DeclarationTemplateCheck> checks, Date templateActualEndDate, Logger logger, TAUserInfo user, Boolean force);
    boolean setStatusTemplate(int templateId, Logger logger, TAUserInfo user, boolean force);

    /**
     * Просто делегирует к другому методу
     */
    void isInUsed(int templateId, int typeId, VersionedObjectStatus status, Date versionActualDateStart, Date versionActualDateEnd, Logger logger);
    void logging(int id, FormDataEvent event, TAUser user);
}
