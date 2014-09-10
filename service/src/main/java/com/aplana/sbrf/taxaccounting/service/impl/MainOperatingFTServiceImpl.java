package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.ColumnDao;
import com.aplana.sbrf.taxaccounting.dao.api.DataRowDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.service.*;
import com.aplana.sbrf.taxaccounting.templateversion.VersionOperatingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * User: avanteev
 */
@Service("formTemplateMainOperatingService")
@Transactional
public class MainOperatingFTServiceImpl implements MainOperatingService {

    private static String SAVE_MESSAGE = "Версия макета не сохранена, обнаружены фатальные ошибки!";
    private static String DELETE_TEMPLATE_MESSAGE = "Удаление невозможно, обнаружено использование макета!";
    private static String DELETE_TEMPLATE_VERSION_MESSAGE = "Удаление невозможно, обнаружено использование макета!";
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
    @Autowired
    private DataRowDao dataRowDao;
    @Autowired
    private ColumnDao columnDao;

    @Override
    public <T> int edit(T template, Date templateActualEndDate, Logger logger, TAUser user) {
        FormTemplate formTemplate = (FormTemplate)template;
        FormTemplate oldFormTemplate = formTemplateService.get(formTemplate.getId());
        Date dbVersionBeginDate = oldFormTemplate.getVersion();
        Date dbVersionEndDate = formTemplateService.getFTEndDate(formTemplate.getId());

        if (dbVersionBeginDate.compareTo(formTemplate.getVersion()) !=0
                || (dbVersionEndDate != null && templateActualEndDate == null)
                || (dbVersionEndDate == null && templateActualEndDate != null)
                || (dbVersionEndDate != null && dbVersionEndDate.compareTo(templateActualEndDate) != 0) ){
            versionOperatingService.isIntersectionVersion(formTemplate.getId(), formTemplate.getType().getId(),
                    formTemplate.getStatus(), formTemplate.getVersion(), templateActualEndDate, logger);
            checkError(logger, SAVE_MESSAGE);
            //Выполенение шага 5.А.1.1
            Pair<Date, Date> beginRange = null;
            Pair<Date, Date> endRange = null;
            if (dbVersionBeginDate.compareTo(formTemplate.getVersion()) < 0)
                beginRange = new Pair<Date, Date>(dbVersionBeginDate, formTemplate.getVersion());
            if (
                    (dbVersionEndDate == null && templateActualEndDate != null)
                ||
                    (dbVersionEndDate != null && templateActualEndDate != null && dbVersionEndDate.compareTo(templateActualEndDate) > 0))
                endRange = new Pair<Date, Date>(templateActualEndDate, dbVersionEndDate);
            versionOperatingService.checkDestinationsSources(formTemplate.getType().getId(), beginRange, endRange, logger);
            checkError(logger, SAVE_MESSAGE);
        }

        if (formTemplate.getStatus().equals(VersionedObjectStatus.NORMAL)){
            versionOperatingService.isUsedVersion(formTemplate.getId(), formTemplate.getType().getId(),
                    formTemplate.getStatus(), formTemplate.getVersion(), templateActualEndDate, logger);
            checkError(logger, SAVE_MESSAGE);
        }

        formTemplateService.validateFormAutoNumerationColumn(formTemplate, logger);
        checkError(logger, SAVE_MESSAGE);

        int id = formTemplateService.save(formTemplate);
        cleanData(oldFormTemplate, formTemplate);

        logging(id, FormDataEvent.TEMPLATE_MODIFIED, user);
        return id;
    }

    /**
     * Удаление устаревших данных НФ, при смене типа хранения графы
     * http://jira.aplana.com/browse/SBRFACCTAX-8467
     *
     * @param oldTemplate
     * @param newTemplate
     */
    private void cleanData(FormTemplate oldTemplate, FormTemplate newTemplate) {
        if (oldTemplate.getColumns() == null || oldTemplate.getColumns().isEmpty()
                || newTemplate.getColumns() == null || newTemplate.getColumns().isEmpty()) {
            return;
        }
        // Список граф, данные которых нужно удалить
        List<Integer> cleanColumnList = new LinkedList<Integer>();
        for (Column oldColumn : oldTemplate.getColumns()) {
            boolean found = false;
            for (Column newColumn : newTemplate.getColumns()) {
                if (oldColumn.getAlias().equals(newColumn.getAlias())) {
                    found = true;
                    if (!oldColumn.getClass().equals(newColumn.getClass())) {
                        // Тип графы изменился
                        cleanColumnList.add(columnDao.getColumnIdByAlias(oldTemplate.getId(), oldColumn.getAlias()));
                    }
                }
            }
            if (!found) {
                // Графа была удалена
                cleanColumnList.add(columnDao.getColumnIdByAlias(oldTemplate.getId(), oldColumn.getAlias()));
            }
        }
        if (cleanColumnList.isEmpty()) {
            return;
        }
        dataRowDao.cleanValue(cleanColumnList);
    }

    @Override
    public <T> int createNewType(T template, Date templateActualEndDate, Logger logger, TAUser user) {
        FormTemplate formTemplate = (FormTemplate)template;
        FormType type = formTemplate.getType();
        type.setStatus(VersionedObjectStatus.NORMAL);
        type.setName(formTemplate.getName() != null ? formTemplate.getName() : "");
        int formTypeId = formTypeService.save(type);
        formTemplate.getType().setId(formTypeId);

        versionOperatingService.isIntersectionVersion(0, formTypeId, VersionedObjectStatus.NORMAL,
                formTemplate.getVersion(), templateActualEndDate, logger);
        checkError(logger, SAVE_MESSAGE);
        int id = formTemplateService.save(formTemplate);

        logging(id, FormDataEvent.TEMPLATE_CREATED, user);
        return id;
    }

    @Override
    public <T> int createNewTemplateVersion(T template, Date templateActualEndDate, Logger logger, TAUser user) {
        FormTemplate formTemplate = (FormTemplate)template;
        versionOperatingService.isIntersectionVersion(0, formTemplate.getType().getId(), VersionedObjectStatus.DRAFT,
                formTemplate.getVersion(), templateActualEndDate, logger);
        checkError(logger, SAVE_MESSAGE);
        int id = formTemplateService.save(formTemplate);

        logging(id, FormDataEvent.TEMPLATE_CREATED, user);
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
            }
            //Получение фейковых значений
        }
        versionOperatingService.checkDestinationsSources(typeId, (Date) null, null, logger);
        checkError(logger, DELETE_TEMPLATE_MESSAGE);
        //Проверка назначений НФ
        for (DepartmentFormType departmentFormType : sourceService.getDFTByFormType(typeId))
            logger.error(
                    String.format(HAVE_DFT_MESSAGE,
                            departmentService.getDepartment(departmentFormType.getDepartmentId()).getName()));
        checkError(logger, DELETE_TEMPLATE_MESSAGE);
        formTypeService.delete(typeId);
    }

    @Override
    public boolean deleteVersionTemplate(int templateId, Logger logger, TAUser user) {
        boolean isDeleteAll = false;//переменная определяющая, удалена ли все версии макета
        FormTemplate template = formTemplateService.get(templateId);
        Date dateEndActualize = formTemplateService.getFTEndDate(templateId);
        versionOperatingService.isUsedVersion(templateId, template.getType().getId(), template.getStatus(), template.getVersion(), dateEndActualize, logger);
        checkError(logger, DELETE_TEMPLATE_VERSION_MESSAGE);
        versionOperatingService.checkDestinationsSources(template.getType().getId(), template.getVersion(), dateEndActualize, logger);
        checkError(logger, DELETE_TEMPLATE_VERSION_MESSAGE);

        versionOperatingService.cleanVersions(templateId, template.getType().getId(), template.getStatus(), template.getVersion(), dateEndActualize, logger);
        int deletedFTid = formTemplateService.delete(template.getId());
        List<FormTemplate> formTemplates = formTemplateService.getFormTemplateVersionsByStatus(template.getType().getId(),
                VersionedObjectStatus.DRAFT, VersionedObjectStatus.NORMAL);
        //Проверка существуют ли еще версии со статусом 0 или 1
        if (formTemplates.isEmpty()){
            for (DepartmentFormType departmentFormType : sourceService.getDFTByFormType(template.getType().getId())){
                logger.error(
                        String.format(HAVE_DFT_MESSAGE,
                                departmentService.getDepartment(departmentFormType.getDepartmentId()).getName()));
            }
            checkError(logger, DELETE_TEMPLATE_VERSION_MESSAGE);
        }

        //Если нет версий макетов, то можно удалить весь макет
        if (formTemplates.isEmpty()){
            templateChangesService.deleteByTemplateIds(Arrays.asList(deletedFTid), null);
            formTypeService.delete(template.getType().getId());
            logger.info("Макет удален в связи с удалением его последней версии");
            isDeleteAll = true;
        }
        logging(templateId, FormDataEvent.TEMPLATE_DELETED, user);
        return isDeleteAll;
    }

    @Override
    public boolean setStatusTemplate(int templateId, Logger logger, TAUser user, boolean force) {
        FormTemplate template = formTemplateService.get(templateId);

        if (template.getStatus() == VersionedObjectStatus.NORMAL){
            versionOperatingService.isUsedVersion(template.getId(), template.getType().getId(), template.getStatus(),
                    template.getVersion(), null, logger);
            if (!force && logger.containsLevel(LogLevel.ERROR)) return false;
            formTemplateService.updateVersionStatus(VersionedObjectStatus.DRAFT, templateId);
            logging(templateId, FormDataEvent.TEMPLATE_DEACTIVATED, user);
        } else {
            formTemplateService.updateVersionStatus(VersionedObjectStatus.NORMAL, templateId);
            logging(templateId, FormDataEvent.TEMPLATE_ACTIVATED, user);
        }
        return true;
    }

    private void checkError(Logger logger, String errorMsg){
        if (logger.containsLevel(LogLevel.ERROR))
            throw new ServiceLoggerException(errorMsg, logEntryService.save(logger.getEntries()));
    }

    private void logging(int id, FormDataEvent event, TAUser user){
        TemplateChanges changes = new TemplateChanges();
        changes.setEvent(event);
        changes.setEventDate(new Date());
        changes.setFormTemplateId(id);
        changes.setAuthor(user);
        templateChangesService.save(changes);
    }
}
