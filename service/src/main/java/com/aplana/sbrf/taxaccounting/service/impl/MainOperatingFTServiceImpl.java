package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.model.FormTemplate;
import com.aplana.sbrf.taxaccounting.model.FormType;
import com.aplana.sbrf.taxaccounting.model.VersionedObjectStatus;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * User: avanteev
 */
@Service
@Transactional
public class MainOperatingFTServiceImpl implements MainOperatingService {

    private static int ONE_DAY_MILLISECONDS = 86400000;

    private static String ERROR_MESSAGE = "Версия макета не сохранена, обнаружены фатальные ошибки!";

    @Autowired
    private LogEntryService logEntryService;

    @Autowired
    @Qualifier("formTemplateOperatingService")
    private VersionOperatingService<FormTemplate> versionOperatingService;

    @Autowired
    private FormTemplateService formTemplateService;

    @Autowired
    private FormTypeService formTypeService;

    @Override
    public int edit(int templateId, Date templateActualEndDate, Logger logger) {
        FormTemplate template = formTemplateService.get(templateId);
        /*versionOperatingService.isCorrectVersion(action.getForm(), action.getVersionEndDate(), logger);*/
        Date dbVersionBeginDate = formTemplateService.get(template.getId()).getVersion();
        Date dbVersionEndDate = formTemplateService.getNearestFTRight(template) != null ?
                new Date(formTemplateService.getNearestFTRight(template).getVersion().getTime() - ONE_DAY_MILLISECONDS) : null;
        if ((dbVersionEndDate != null && (dbVersionBeginDate.compareTo(template.getVersion()) !=0 ||
                dbVersionEndDate.compareTo(templateActualEndDate) !=0)) || templateActualEndDate != null || dbVersionBeginDate.compareTo(template.getVersion()) !=0 ){
            System.out.println("Старт проверки.");
            versionOperatingService.isIntersectionVersion(template, templateActualEndDate, logger);
            checkError(logger);
        }

        versionOperatingService.isUsedVersion(template, templateActualEndDate, logger);
        checkError(logger);
        return formTemplateService.save(template);
    }

    @Override
    public <T> int createNewFormType(T template, Date templateActualEndDate, Logger logger) {
        FormTemplate formTemplate = (FormTemplate)template;
        /*versionOperatingService.isCorrectVersion(template, templateActualEndDate, logger);
        checkError(logger);*/
        FormType type = formTemplate.getType();
        type.setStatus(VersionedObjectStatus.DRAFT);
        type.setName(formTemplate.getName() != null ? formTemplate.getName() : "");
        int formTypeId = formTypeService.save(type);
        formTemplate.getType().setId(formTypeId);
        versionOperatingService.isIntersectionVersion(formTemplate, templateActualEndDate, logger);
        checkError(logger);
        formTemplate.setEdition(1);//т.к. первый
        formTemplate.setStatus(VersionedObjectStatus.DRAFT);
        return formTemplateService.save(formTemplate);
    }

    @Override
    public <T> int createNewTemplateVersion(T template, Date templateActualEndDate, Logger logger) {
        FormTemplate formTemplate = (FormTemplate)template;
        /*versionOperatingService.isCorrectVersion(action.getForm(), action.getVersionEndDate(), logger);*/
        checkError(logger);
        versionOperatingService.isIntersectionVersion(formTemplate, templateActualEndDate, logger);
        checkError(logger);
        formTemplate.setStatus(VersionedObjectStatus.DRAFT);
        formTemplate.setEdition(formTemplateService.versionTemplateCount(formTemplate.getType().getId()));
        return formTemplateService.save(formTemplate);
    }

    @Override
    public void deleteTemplate(int typeId, Logger logger) {
        List<FormTemplate> formTemplates = formTemplateService.getFormTemplateVersionsByStatus(typeId,
                VersionedObjectStatus.NORMAL, VersionedObjectStatus.DRAFT);
        if (formTemplates != null && !formTemplates.isEmpty()){
            for (FormTemplate formTemplate : formTemplates){
                versionOperatingService.isUsedVersion(formTemplate, null, logger);
                if (logger.containsLevel(LogLevel.ERROR))
                    throw new ServiceLoggerException("Удаление невозможно, обнаружено использование макета",
                            logEntryService.save(logger.getEntries()));
                formTemplate.setStatus(VersionedObjectStatus.DELETED);
                formTemplateService.save(formTemplate);
            }
        }
        FormType formType = formTypeService.get(typeId);
        formType.setStatus(VersionedObjectStatus.DELETED);
        formTypeService.save(formType);
    }

    @Override
    public void deleteVersionTemplate(int templateId, Date templateActualEndDate, Logger logger) {
        FormTemplate template = formTemplateService.get(templateId);
        FormTemplate nearestFT = formTemplateService.getNearestFTRight(template);
        Date dateEndActualize = nearestFT != null ? nearestFT.getVersion() : null;
        versionOperatingService.isUsedVersion(template, dateEndActualize, logger);
        if (logger.containsLevel(LogLevel.ERROR))
            throw new ServiceLoggerException("Удаление невозможно, обнаружены ссылки на удаляемую версию макета",
                    logEntryService.save(logger.getEntries()));
        versionOperatingService.cleanVersions(template, dateEndActualize, logger);
        formTemplateService.delete(template);
        if (formTemplateService.getFormTemplateVersionsByStatus(template.getType().getId(),
                VersionedObjectStatus.DRAFT, VersionedObjectStatus.NORMAL).isEmpty()){
            formTypeService.delete(template.getType().getId());
            logger.info("Макет удален в связи с удалением его последней версии");
        }
    }

    @Override
    public void setStatusTemplate(int templateId, Logger logger) {
        FormTemplate formTemplate = formTemplateService.get(templateId);
        if (formTemplate.getStatus() == VersionedObjectStatus.NORMAL){
            versionOperatingService.isUsedVersion(formTemplate, null, logger);
            if (logger.containsLevel(LogLevel.ERROR))
                throw new ServiceLoggerException("Макет используется и не может быть выведен из действия", logEntryService.save(logger.getEntries()));
            formTemplate.setStatus(VersionedObjectStatus.DRAFT);
            formTemplateService.save(formTemplate);
        } else {
            formTemplate.setStatus(VersionedObjectStatus.NORMAL);
            formTemplateService.save(formTemplate);
        }
    }


    private void checkError(Logger logger){
        if (logger.containsLevel(LogLevel.ERROR))
            throw new ServiceLoggerException(ERROR_MESSAGE, logEntryService.save(logger.getEntries()));
    }
}
