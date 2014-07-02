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
@Service("formTemplateMainOperatingService")
@Transactional
public class MainOperatingFTServiceImpl implements MainOperatingService {

    private static String SAVE_MESSAGE = "Версия макета не сохранена, обнаружены фатальные ошибки!";
    private static String DELETE_TEMPLATE_MESSAGE = "Версия макета не сохранена, обнаружены фатальные ошибки!";
    private static String DELETE_TEMPLATE_VERSION_MESSAGE = "Удаление невозможно, обнаружены ссылки на удаляемую версию макета!";
    private static String HAVE_DFT_MESSAGE = "Существует назначение налоговой формы подразделению %s!";

    @Autowired
    private LogEntryService logEntryService;
    @Autowired
    @Qualifier("formTemplateOperatingService")
    private VersionOperatingService versionOperatingService;
    @Autowired
    private FormTemplateService formTemplateService;
    @Autowired
    private FormTypeService formTypeService;
    @Autowired
    private TemplateChangesService templateChangesService;
    @Autowired
    private SourceService sourceService;
    @Autowired
    private DepartmentService departmentService;

    @Override
    public <T> int edit(T template, Date templateActualEndDate, Logger logger, TAUser user) {
        FormTemplate formTemplate = (FormTemplate)template;
        /*versionOperatingService.isCorrectVersion(action.getForm(), action.getVersionEndDate(), logger);*/
        Date dbVersionBeginDate = formTemplateService.get(formTemplate.getId()).getVersion();
        Date dbVersionEndDate = formTemplateService.getFTEndDate(formTemplate.getId());

        if (dbVersionBeginDate.compareTo(formTemplate.getVersion()) !=0
                || (dbVersionEndDate != null && templateActualEndDate == null)
                || (dbVersionEndDate == null && templateActualEndDate != null)
                || (dbVersionEndDate != null && dbVersionEndDate.compareTo(templateActualEndDate) != 0) ){
            versionOperatingService.isIntersectionVersion(formTemplate.getId(), formTemplate.getType().getId(),
                    formTemplate.getStatus(), formTemplate.getVersion(), templateActualEndDate, logger);
            checkError(logger, SAVE_MESSAGE);
            versionOperatingService.checkDestinationsSources(formTemplate.getType().getId(), formTemplate.getVersion(), templateActualEndDate, logger);
            checkError(logger, SAVE_MESSAGE);
        }

        switch (formTemplate.getStatus()){
            case NORMAL:
                versionOperatingService.isUsedVersion(formTemplate.getId(), formTemplate.getType().getId(),
                        formTemplate.getStatus(), formTemplate.getVersion(), templateActualEndDate, logger);
                checkError(logger, SAVE_MESSAGE);
                //Что то с нумерацией строк
                checkError(logger, SAVE_MESSAGE);
                break;
            case DRAFT:
                //Что то с нумерацией строк
                checkError(logger, SAVE_MESSAGE);
                break;
        }

        int id = formTemplateService.save(formTemplate);

        logging(id, TemplateChangesEvent.MODIFIED, user);
        return id;
    }

    @Override
    public <T> int createNewType(T template, Date templateActualEndDate, Logger logger, TAUser user) {
        FormTemplate formTemplate = (FormTemplate)template;
        /*versionOperatingService.isCorrectVersion(template, templateActualEndDate, logger);
        checkError(logger);*/
        FormType type = formTemplate.getType();
        type.setStatus(VersionedObjectStatus.NORMAL);
        type.setName(formTemplate.getName() != null ? formTemplate.getName() : "");
        int formTypeId = formTypeService.save(type);
        formTemplate.getType().setId(formTypeId);

        versionOperatingService.isIntersectionVersion(0, formTypeId, VersionedObjectStatus.NORMAL,
                formTemplate.getVersion(), templateActualEndDate, logger);
        checkError(logger, SAVE_MESSAGE);
        int id = formTemplateService.save(formTemplate);

        logging(id, TemplateChangesEvent.CREATED, user);
        return id;
    }

    @Override
    public <T> int createNewTemplateVersion(T template, Date templateActualEndDate, Logger logger, TAUser user) {
        FormTemplate formTemplate = (FormTemplate)template;
        /*versionOperatingService.isCorrectVersion(action.getForm(), action.getVersionEndDate(), logger);
        checkError(logger);*/
        versionOperatingService.isIntersectionVersion(0, formTemplate.getType().getId(), VersionedObjectStatus.DRAFT,
                formTemplate.getVersion(), templateActualEndDate, logger);
        checkError(logger, SAVE_MESSAGE);
        int id = formTemplateService.save(formTemplate);

        logging(id, TemplateChangesEvent.CREATED, user);
        return id;
    }

    @Override
    public void deleteTemplate(int typeId, Logger logger, TAUser user) {
        List<FormTemplate> formTemplates = formTemplateService.getFormTemplateVersionsByStatus(typeId,
                VersionedObjectStatus.NORMAL, VersionedObjectStatus.DRAFT);
        //Проверка использования
        if (formTemplates != null && !formTemplates.isEmpty()){
            for (FormTemplate formTemplate : formTemplates){
                versionOperatingService.isUsedVersion(formTemplate.getId(), typeId, formTemplate.getStatus(), formTemplate.getVersion(), null, logger);
                checkError(logger, DELETE_TEMPLATE_MESSAGE);
                formTemplate.setStatus(VersionedObjectStatus.DELETED);
            }
            formTemplateService.update(formTemplates);
        }
        versionOperatingService.checkDestinationsSources(typeId, null, null, logger);
        checkError(logger, DELETE_TEMPLATE_MESSAGE);
        //Проверка назначений НФ
        for (DepartmentFormType departmentFormType : sourceService.getDFTByFormType(typeId))
            logger.error(
                    String.format(HAVE_DFT_MESSAGE,
                            departmentService.getDepartment(departmentFormType.getDepartmentId())));
        formTypeService.delete(typeId);
        /*logging(typeId, TemplateChangesEvent.DELETED, user);*/
    }

    @Override
    public void deleteVersionTemplate(int templateId, Date templateActualEndDate, Logger logger, TAUser user) {
        FormTemplate template = formTemplateService.get(templateId);
        Date dateEndActualize = formTemplateService.getFTEndDate(templateId);
        versionOperatingService.isUsedVersion(templateId, template.getType().getId(), template.getStatus(), template.getVersion(), dateEndActualize, logger);
        checkError(logger, DELETE_TEMPLATE_VERSION_MESSAGE);
        versionOperatingService.checkDestinationsSources(template.getType().getId(), template.getVersion(), templateActualEndDate, logger);
        checkError(logger, DELETE_TEMPLATE_VERSION_MESSAGE);

        template.setStatus(VersionedObjectStatus.DELETED);
        formTemplateService.save(template);
        List<FormTemplate> formTemplates = formTemplateService.getFormTemplateVersionsByStatus(template.getType().getId(),
                VersionedObjectStatus.DRAFT, VersionedObjectStatus.NORMAL);
        //Проверка существуют ли еще версии со статусом 0 или 1
        if (formTemplates.isEmpty()){
            for (DepartmentFormType departmentFormType : sourceService.getDFTByFormType(template.getType().getId())){
                logger.error(
                        String.format(HAVE_DFT_MESSAGE,
                                departmentService.getDepartment(departmentFormType.getDepartmentId())));
                checkError(logger, DELETE_TEMPLATE_VERSION_MESSAGE);
            }
        }

        versionOperatingService.cleanVersions(templateId, template.getType().getId(), template.getStatus(), template.getVersion(), dateEndActualize, logger);
        //Если нет версий макетов, то можно удалить весь макет
        if (formTemplates.isEmpty()){
            formTypeService.delete(template.getType().getId());
            logger.info("Макет удален в связи с удалением его последней версии");
        }
        logging(templateId, TemplateChangesEvent.DELETED, user);
    }

    @Override
    public boolean setStatusTemplate(int templateId, Logger logger, TAUser user, boolean force) {
        FormTemplate template = formTemplateService.get(templateId);

        if (template.getStatus() == VersionedObjectStatus.NORMAL){
            versionOperatingService.isUsedVersion(template.getId(), template.getType().getId(), template.getStatus(),
                    template.getVersion(), null, logger);
            if (!force && logger.containsLevel(LogLevel.ERROR)) return false;
            formTemplateService.updateVersionStatus(VersionedObjectStatus.DRAFT, templateId);
            logging(templateId, TemplateChangesEvent.DEACTIVATED, user);
        } else {
            formTemplateService.updateVersionStatus(VersionedObjectStatus.NORMAL, templateId);
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
        changes.setFormTemplateId(id);
        changes.setAuthor(user);
        templateChangesService.save(changes);
    }
}
