package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.model.DeclarationTemplate;
import com.aplana.sbrf.taxaccounting.model.DeclarationType;
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
@Service("declarationTemplateMainOperatingService")
@Transactional
public class MainOperatingDTServiceImpl implements MainOperatingService {

    private static int ONE_DAY_MILLISECONDS = 86400000;
    private static String ERROR_MESSAGE = "Версия макета не сохранена, обнаружены фатальные ошибки!";

    @Autowired
    private LogEntryService logEntryService;

    @Autowired
    private DeclarationTemplateService declarationTemplateService;

    @Autowired
    private DeclarationTypeService declarationTypeService;

    @Autowired
    @Qualifier("declarationTemplateOperatingService")
    private VersionOperatingService<DeclarationTemplate> versionOperatingService;

    @Override
    public int edit(int templateId, Date templateActualEndDate, Logger logger) {
        DeclarationTemplate template = declarationTemplateService.get(templateId);
        /*versionOperatingService.isCorrectVersion(action.getForm(), action.getVersionEndDate(), logger);*/
        Date dbVersionBeginDate = declarationTemplateService.get(template.getId()).getVersion();
        Date dbVersionEndDate = declarationTemplateService.getNearestDTRight(template) != null ?
                new Date(declarationTemplateService.getNearestDTRight(template).getVersion().getTime() - ONE_DAY_MILLISECONDS) : null;
        if ((dbVersionEndDate != null && (dbVersionBeginDate.compareTo(template.getVersion()) !=0 ||
                dbVersionEndDate.compareTo(templateActualEndDate) !=0)) || templateActualEndDate != null || dbVersionBeginDate.compareTo(template.getVersion()) !=0 ){
            versionOperatingService.isIntersectionVersion(template, templateActualEndDate, logger);
            checkError(logger);
        }

        versionOperatingService.isUsedVersion(template, templateActualEndDate, logger);
        checkError(logger);
        return declarationTemplateService.save(template);
    }

    @Override
    public <T> int createNewType(T template, Date templateActualEndDate, Logger logger) {
        DeclarationTemplate declarationTemplate = (DeclarationTemplate)template;
        /*versionOperatingService.isCorrectVersion(template, templateActualEndDate, logger);
        checkError(logger);*/
        DeclarationType type = declarationTemplate.getType();
        type.setStatus(VersionedObjectStatus.DRAFT);
        type.setName("Declaration name");
        int formTypeId = declarationTypeService.save(type);
        declarationTemplate.getType().setId(formTypeId);
        versionOperatingService.isIntersectionVersion(declarationTemplate, templateActualEndDate, logger);
        checkError(logger);
        declarationTemplate.setEdition(1);//т.к. первый
        declarationTemplate.setStatus(VersionedObjectStatus.NORMAL);
        return declarationTemplateService.save(declarationTemplate);
    }

    @Override
    public <T> int createNewTemplateVersion(T template, Date templateActualEndDate, Logger logger) {
        DeclarationTemplate declarationTemplate = (DeclarationTemplate)template;
        /*versionOperatingService.isCorrectVersion(action.getForm(), action.getVersionEndDate(), logger);*/
        checkError(logger);
        versionOperatingService.isIntersectionVersion(declarationTemplate, templateActualEndDate, logger);
        checkError(logger);
        declarationTemplate.setStatus(VersionedObjectStatus.DRAFT);
        declarationTemplate.setEdition(declarationTemplateService.versionTemplateCount(declarationTemplate.getType().getId()));
        return declarationTemplateService.save(declarationTemplate);
    }

    @Override
    public void deleteTemplate(int typeId, Logger logger) {
        List<DeclarationTemplate> templates = declarationTemplateService.getDecTemplateVersionsByStatus(typeId,
                VersionedObjectStatus.NORMAL, VersionedObjectStatus.DRAFT);
        if (templates != null && !templates.isEmpty()){
            for (DeclarationTemplate declarationTemplate : templates){
                versionOperatingService.isUsedVersion(declarationTemplate, null, logger);
                if (logger.containsLevel(LogLevel.ERROR))
                    throw new ServiceLoggerException("Удаление невозможно, обнаружено использование макета",
                            logEntryService.save(logger.getEntries()));
                declarationTemplate.setStatus(VersionedObjectStatus.DELETED);
                declarationTemplateService.save(declarationTemplate);
            }
        }
        DeclarationType decType = declarationTypeService.get(typeId);
        decType.setStatus(VersionedObjectStatus.DELETED);
        declarationTypeService.save(decType);
    }

    @Override
    public void deleteVersionTemplate(int templateId, Date templateActualEndDate, Logger logger) {
        DeclarationTemplate template = declarationTemplateService.get(templateId);
        DeclarationTemplate nearestDT = declarationTemplateService.getNearestDTRight(template);
        Date dateEndActualize = nearestDT != null ? nearestDT.getVersion() : null;
        versionOperatingService.isUsedVersion(template, dateEndActualize, logger);
        if (logger.containsLevel(LogLevel.ERROR))
            throw new ServiceLoggerException("Удаление невозможно, обнаружены ссылки на удаляемую версию макета",
                    logEntryService.save(logger.getEntries()));
        versionOperatingService.cleanVersions(template, dateEndActualize, logger);
        declarationTemplateService.delete(template);
        if (declarationTemplateService.getDecTemplateVersionsByStatus(template.getType().getId(),
                VersionedObjectStatus.DRAFT, VersionedObjectStatus.NORMAL).isEmpty()){
            declarationTypeService.delete(template.getType().getId());
            logger.info("Макет удален в связи с удалением его последней версии");
        }
    }

    @Override
    public void setStatusTemplate(int templateId, Logger logger) {
        DeclarationTemplate declarationTemplate = declarationTemplateService.get(templateId);
        if (declarationTemplate.getStatus() == VersionedObjectStatus.NORMAL){
            versionOperatingService.isUsedVersion(declarationTemplate, null, logger);
            if (logger.containsLevel(LogLevel.ERROR))
                throw new ServiceLoggerException("Макет используется и не может быть выведен из действия", logEntryService.save(logger.getEntries()));
            declarationTemplate.setStatus(VersionedObjectStatus.DRAFT);
            declarationTemplateService.save(declarationTemplate);
        } else {
            declarationTemplate.setStatus(VersionedObjectStatus.NORMAL);
            declarationTemplateService.save(declarationTemplate);
        }
    }

    private void checkError(Logger logger){
        if (logger.containsLevel(LogLevel.ERROR))
            throw new ServiceLoggerException(ERROR_MESSAGE, logEntryService.save(logger.getEntries()));
    }
}
