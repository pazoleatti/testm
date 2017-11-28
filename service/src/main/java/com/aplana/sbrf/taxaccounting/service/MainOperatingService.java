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
    String FORM_EXIST =
            "Версия макета не сохранена. Период актуальности версии не может быть изменен, пока существуют экземпляры форм в отчетных периодах, в течение которых версия макета более не должна действовать";

    <T> boolean edit(T template, Date templateActualEndDate, Logger logger, TAUserInfo user);
    <T> boolean edit(T template, List<DeclarationTemplateCheck> checks, Date templateActualEndDate, Logger logger, TAUserInfo user);
    <T> boolean edit(T template, List<DeclarationTemplateCheck> checks, Date templateActualEndDate, Logger logger, TAUserInfo user, Boolean force);
    <T> int createNewType(T template, List<DeclarationTemplateCheck> checks, Date templateActualEndDate, Logger logger, TAUserInfo user);
    <T> int createNewTemplateVersion(T template, List<DeclarationTemplateCheck> checks, Date templateActualEndDate, Logger logger, TAUserInfo user);
    void deleteTemplate(int typeId, Logger logger, TAUserInfo user);
    boolean deleteVersionTemplate(int templateId, Logger logger, TAUserInfo user);
    boolean setStatusTemplate(int templateId, Logger logger, TAUserInfo user, boolean force);

    /**
     * Просто делегирует к другому методу
     */
    void isInUsed(int templateId, int typeId, VersionedObjectStatus status, Date versionActualDateStart, Date versionActualDateEnd, Logger logger);
    void logging(int id, FormDataEvent event, TAUser user);
}
