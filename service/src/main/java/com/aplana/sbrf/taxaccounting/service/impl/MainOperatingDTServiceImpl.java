package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.*;
import com.aplana.sbrf.taxaccounting.templateversion.VersionOperatingService;
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

    private static String ERROR_MESSAGE = "Версия макета не сохранена, обнаружены фатальные ошибки!";

    @Autowired
    private LogEntryService logEntryService;

    @Autowired
    private DeclarationTemplateService declarationTemplateService;

    @Autowired
    private DeclarationTypeService declarationTypeService;

    @Autowired
    @Qualifier("declarationTemplateOperatingService")
    private VersionOperatingService versionOperatingService;

    @Autowired
    private TemplateChangesService templateChangesService;

    @Override
    public <T> int edit(T template, Date templateActualEndDate, Logger logger, TAUser user) {
        DeclarationTemplate declarationTemplate = (DeclarationTemplate)template;
        /*versionOperatingService.isCorrectVersion(action.getForm(), action.getVersionEndDate(), logger);*/
        Date dbVersionBeginDate = declarationTemplateService.get(declarationTemplate.getId()).getVersion();
        Date dbVersionEndDate = declarationTemplateService.getDTEndDate(declarationTemplate.getId());
        if (dbVersionBeginDate.compareTo(declarationTemplate.getVersion()) !=0
                || (dbVersionEndDate != null && templateActualEndDate == null)
                || (dbVersionEndDate == null && templateActualEndDate != null)
                || (dbVersionEndDate != null && dbVersionEndDate.compareTo(templateActualEndDate) != 0) ){
            versionOperatingService.isIntersectionVersion(declarationTemplate.getId(), declarationTemplate.getType().getId(),
                    declarationTemplate.getStatus(), declarationTemplate.getVersion(), templateActualEndDate, logger);
            checkError(logger);
        }

        versionOperatingService.isUsedVersion(declarationTemplate.getId(), declarationTemplate.getType().getId(),
                declarationTemplate.getStatus(), declarationTemplate.getVersion(), templateActualEndDate, logger);
        checkError(logger);
        int id = declarationTemplateService.save(declarationTemplate);

        logging(id, TemplateChangesEvent.MODIFIED, user);
        return id;
    }

    @Override
    public <T> int createNewType(T template, Date templateActualEndDate, Logger logger, TAUser user) {
        DeclarationTemplate declarationTemplate = (DeclarationTemplate)template;
        /*versionOperatingService.isCorrectVersion(template, templateActualEndDate, logger);
        checkError(logger);*/
        DeclarationType type = declarationTemplate.getType();
        type.setStatus(VersionedObjectStatus.NORMAL);
        type.setName(declarationTemplate.getName() != null && !declarationTemplate.getName().isEmpty()?declarationTemplate.getName():"");
        int formTypeId = declarationTypeService.save(type);
        declarationTemplate.getType().setId(formTypeId);
        versionOperatingService.isIntersectionVersion(0, formTypeId, VersionedObjectStatus.NORMAL, declarationTemplate.getVersion(), templateActualEndDate, logger);
        checkError(logger);
        declarationTemplate.setEdition(1);//т.к. первый
        declarationTemplate.setStatus(VersionedObjectStatus.NORMAL);
        int id = declarationTemplateService.save(declarationTemplate);

        logging(id, TemplateChangesEvent.CREATED, user);
        return id;
    }

    @Override
    public <T> int createNewTemplateVersion(T template, Date templateActualEndDate, Logger logger, TAUser user) {
        DeclarationTemplate declarationTemplate = (DeclarationTemplate)template;
        /*versionOperatingService.isCorrectVersion(action.getForm(), action.getVersionEndDate(), logger);*/
        checkError(logger);
        declarationTemplate.setStatus(VersionedObjectStatus.DRAFT);
        versionOperatingService.isIntersectionVersion(0, declarationTemplate.getType().getId(),
                declarationTemplate.getStatus(), declarationTemplate.getVersion(), templateActualEndDate, logger);
        checkError(logger);
        int id = declarationTemplateService.save(declarationTemplate);

        logging(id, TemplateChangesEvent.CREATED, user);
        return id;
    }

    @Override
    public void deleteTemplate(int typeId, Logger logger, TAUser user) {
        List<DeclarationTemplate> templates = declarationTemplateService.getDecTemplateVersionsByStatus(typeId,
                VersionedObjectStatus.NORMAL, VersionedObjectStatus.DRAFT);
        if (templates != null && !templates.isEmpty()){
            for (DeclarationTemplate declarationTemplate : templates){
                versionOperatingService.isUsedVersion(declarationTemplate.getId(), declarationTemplate.getType().getId(),
                        declarationTemplate.getStatus(), declarationTemplate.getVersion(), null, logger);
                if (logger.containsLevel(LogLevel.ERROR))
                    throw new ServiceLoggerException("Удаление невозможно, обнаружено использование макета",
                            logEntryService.save(logger.getEntries()));
                declarationTemplate.setStatus(VersionedObjectStatus.DELETED);
            }
            declarationTemplateService.update(templates);
        }
        declarationTypeService.delete(typeId);

        /*logging(typeId, TemplateChangesEvent.DELETED, user);*/
    }

    @Override
    public void deleteVersionTemplate(int templateId, Date templateActualEndDate, Logger logger, TAUser user) {
        DeclarationTemplate template = declarationTemplateService.get(templateId);
        Date dateEndActualize = declarationTemplateService.getDTEndDate(templateId);
        versionOperatingService.isUsedVersion(template.getId(), template.getType().getId(),
                template.getStatus(), template.getVersion(), dateEndActualize, logger);
        if (logger.containsLevel(LogLevel.ERROR))
            throw new ServiceLoggerException("Удаление невозможно, обнаружены ссылки на удаляемую версию макета",
                    logEntryService.save(logger.getEntries()));
        versionOperatingService.cleanVersions(template.getId(), template.getType().getId(),
                template.getStatus(), template.getVersion(), dateEndActualize, logger);
        template.setStatus(VersionedObjectStatus.DELETED);
        declarationTemplateService.save(template);
        if (declarationTemplateService.getDecTemplateVersionsByStatus(template.getType().getId(),
                VersionedObjectStatus.DRAFT, VersionedObjectStatus.NORMAL).isEmpty()){
            declarationTypeService.delete(template.getType().getId());
            logger.info("Макет удален в связи с удалением его последней версии");
        }
        logging(templateId, TemplateChangesEvent.DELETED, user);
    }

    @Override
    public boolean setStatusTemplate(int templateId, Logger logger, TAUser user, boolean force) {
        DeclarationTemplate declarationTemplate = declarationTemplateService.get(templateId);

        if (declarationTemplate.getStatus() == VersionedObjectStatus.NORMAL){
            versionOperatingService.isUsedVersion(declarationTemplate.getId(), declarationTemplate.getType().getId(),
                    declarationTemplate.getStatus(), declarationTemplate.getVersion(), null, logger);
            if (!force && logger.containsLevel(LogLevel.ERROR)) return false;
            declarationTemplate.setStatus(VersionedObjectStatus.DRAFT);
            declarationTemplateService.save(declarationTemplate);
            logging(templateId, TemplateChangesEvent.DEACTIVATED, user);
        } else {
            declarationTemplate.setStatus(VersionedObjectStatus.NORMAL);
            declarationTemplateService.save(declarationTemplate);
            logging(templateId, TemplateChangesEvent.ACTIVATED, user);
        }
        return true;
    }

    private void checkError(Logger logger){
        if (logger.containsLevel(LogLevel.ERROR))
            throw new ServiceLoggerException(ERROR_MESSAGE, logEntryService.save(logger.getEntries()));
    }

    private void logging(int id, TemplateChangesEvent event, TAUser user){
        TemplateChanges changes = new TemplateChanges();
        changes.setEvent(event);
        changes.setEventDate(new Date());
        changes.setDeclarationTemplateId(id);
        changes.setAuthor(user);
        templateChangesService.save(changes);
    }
}
