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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * User: avanteev
 */
@Service("declarationTemplateMainOperatingService")
@Transactional
public class MainOperatingDTServiceImpl implements MainOperatingService {

    private static String SAVE_MESSAGE = "Версия макета не сохранена, обнаружены фатальные ошибки!";
    private static String DELETE_TEMPLATE_MESSAGE = "Версия макета не сохранена, обнаружены фатальные ошибки!";
    private static String DELETE_TEMPLATE_VERSION_MESSAGE = "Удаление невозможно, обнаружены ссылки на удаляемую версию макета!";
    private static String HAVE_DDT_MESSAGE = "Существует назначение налоговой формы подразделению %s!";

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
    @Autowired
    private SourceService sourceService;
    @Autowired
    private DepartmentService departmentService;

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
            checkError(logger, SAVE_MESSAGE);
            versionOperatingService.checkDestinationsSources(declarationTemplate.getType().getId(), declarationTemplate.getVersion(), templateActualEndDate, logger);
            checkError(logger, SAVE_MESSAGE);
        }

        switch (declarationTemplate.getStatus()){
            case NORMAL:
                versionOperatingService.isUsedVersion(declarationTemplate.getId(), declarationTemplate.getType().getId(),
                        declarationTemplate.getStatus(), declarationTemplate.getVersion(), templateActualEndDate, logger);
                checkError(logger, SAVE_MESSAGE);
                //Что то с нумерацией строк
                checkError(logger, SAVE_MESSAGE);
                break;
            case DRAFT:
                //Что то с нумерацией строк
                checkError(logger, SAVE_MESSAGE);
                break;
        }

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
        checkError(logger, SAVE_MESSAGE);
        declarationTemplate.setStatus(VersionedObjectStatus.DRAFT);
        int id = declarationTemplateService.save(declarationTemplate);

        logging(id, TemplateChangesEvent.CREATED, user);
        return id;
    }

    @Override
    public <T> int createNewTemplateVersion(T template, Date templateActualEndDate, Logger logger, TAUser user) {
        DeclarationTemplate declarationTemplate = (DeclarationTemplate)template;
        /*versionOperatingService.isCorrectVersion(action.getForm(), action.getVersionEndDate(), logger);*/
        checkError(logger, SAVE_MESSAGE);
        declarationTemplate.setStatus(VersionedObjectStatus.DRAFT);
        versionOperatingService.isIntersectionVersion(0, declarationTemplate.getType().getId(),
                declarationTemplate.getStatus(), declarationTemplate.getVersion(), templateActualEndDate, logger);
        checkError(logger, SAVE_MESSAGE);
        int id = declarationTemplateService.save(declarationTemplate);

        logging(id, TemplateChangesEvent.CREATED, user);
        return id;
    }

    @Override
    public void deleteTemplate(int typeId, Logger logger, TAUser user) {
        List<DeclarationTemplate> templates = declarationTemplateService.getDecTemplateVersionsByStatus(typeId,
                VersionedObjectStatus.NORMAL, VersionedObjectStatus.DRAFT);
        if (templates != null && !templates.isEmpty()){
            ArrayList<Integer> ids = new ArrayList<Integer>(templates.size());
            for (DeclarationTemplate declarationTemplate : templates){
                versionOperatingService.isUsedVersion(declarationTemplate.getId(), declarationTemplate.getType().getId(),
                        declarationTemplate.getStatus(), declarationTemplate.getVersion(), null, logger);
                checkError(logger, DELETE_TEMPLATE_MESSAGE);
                //declarationTemplate.setStatus(VersionedObjectStatus.DELETED);
                ids.add(declarationTemplate.getId());
            }
            //Все версии теперь каскадом удаляю, т.к. есть все необходимые проверки
            declarationTemplateService.delete(ids);
        }
        versionOperatingService.checkDestinationsSources(typeId, null, null, logger);
        checkError(logger, DELETE_TEMPLATE_MESSAGE);
        //Проверка назначений деклараций
        for (DepartmentDeclarationType departmentFormType : sourceService.getDDTByDeclarationType(typeId))
            logger.error(
                    String.format(HAVE_DDT_MESSAGE,
                            departmentService.getDepartment(departmentFormType.getDepartmentId())));
        declarationTypeService.delete(typeId);

        /*logging(typeId, TemplateChangesEvent.DELETED, user);*/
    }

    @Override
    public boolean deleteVersionTemplate(int templateId, Date templateActualEndDate, Logger logger, TAUser user) {
        boolean isDeleteAll = false;//переменная определяющая, удалена ли все версии макета
        DeclarationTemplate template = declarationTemplateService.get(templateId);
        Date dateEndActualize = declarationTemplateService.getDTEndDate(templateId);
        versionOperatingService.isUsedVersion(template.getId(), template.getType().getId(),
                template.getStatus(), template.getVersion(), dateEndActualize, logger);
        checkError(logger, DELETE_TEMPLATE_VERSION_MESSAGE);
        versionOperatingService.checkDestinationsSources(template.getType().getId(), template.getVersion(), templateActualEndDate, logger);
        checkError(logger, DELETE_TEMPLATE_VERSION_MESSAGE);

        versionOperatingService.cleanVersions(template.getId(), template.getType().getId(),
                template.getStatus(), template.getVersion(), dateEndActualize, logger);
        declarationTemplateService.delete(template.getId());
        List<DeclarationTemplate> declarationTemplates = declarationTemplateService.getDecTemplateVersionsByStatus(template.getType().getId(),
                VersionedObjectStatus.DRAFT, VersionedObjectStatus.NORMAL);
        if (declarationTemplates.isEmpty()){
            for (DepartmentFormType departmentFormType : sourceService.getDFTByFormType(template.getType().getId())){
                logger.error(
                        String.format(HAVE_DDT_MESSAGE,
                                departmentService.getDepartment(departmentFormType.getDepartmentId())));
                checkError(logger, DELETE_TEMPLATE_VERSION_MESSAGE);
            }
        }

        //Если нет версий макетов, то можно удалить весь макет
        if (declarationTemplates.isEmpty()){
            declarationTypeService.delete(template.getType().getId());
            logger.info("Макет удален в связи с удалением его последней версии");
            isDeleteAll = true;
        }
        logging(templateId, TemplateChangesEvent.DELETED, user);
        return isDeleteAll;
    }

    @Override
    public boolean setStatusTemplate(int templateId, Logger logger, TAUser user, boolean force) {
        DeclarationTemplate declarationTemplate = declarationTemplateService.get(templateId);

        if (declarationTemplate.getStatus() == VersionedObjectStatus.NORMAL){
            versionOperatingService.isUsedVersion(declarationTemplate.getId(), declarationTemplate.getType().getId(),
                    declarationTemplate.getStatus(), declarationTemplate.getVersion(), null, logger);
            if (!force && logger.containsLevel(LogLevel.ERROR)) return false;
            declarationTemplate.setStatus(VersionedObjectStatus.DRAFT);
            declarationTemplateService.updateVersionStatus(VersionedObjectStatus.DRAFT, templateId);
            logging(templateId, TemplateChangesEvent.DEACTIVATED, user);
        } else {
            declarationTemplate.setStatus(VersionedObjectStatus.NORMAL);
            declarationTemplateService.updateVersionStatus(VersionedObjectStatus.NORMAL, templateId);
            logging(templateId, TemplateChangesEvent.ACTIVATED, user);
        }
        return true;
    }

    private void checkError(Logger logger, String errorMsg){
        if (logger.containsLevel(LogLevel.ERROR))
            throw new ServiceLoggerException(errorMsg, logEntryService.save(logger.getEntries()));
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
