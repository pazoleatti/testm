package com.aplana.sbrf.taxaccounting.service.impl;

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
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * User: avanteev
 */
@Service("declarationTemplateMainOperatingService")
@Transactional
public class MainOperatingDTServiceImpl implements MainOperatingService {

    private static final String DELETE_TEMPLATE_MESSAGE = "Удаление невозможно, обнаружено использование макета!";
    private static final String DELETE_TEMPLATE_VERSION_MESSAGE = "Удаление невозможно, обнаружены ссылки на удаляемую версию макета!";
    private static final String HAVE_DDT_MESSAGE = "Существует назначение декларации подразделению \"%s\"!";

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
    @Autowired
    private DeclarationDataService declarationDataService;
    @Autowired
    private AuditService auditService;

    @Override
    public <T> int edit(T template, Date templateActualEndDate, Logger logger, TAUserInfo user) {
        DeclarationTemplate declarationTemplate = (DeclarationTemplate)template;
        declarationTemplateService.validateDeclarationTemplate(declarationTemplate, logger);
        checkError(logger, SAVE_MESSAGE);
        Date dbVersionBeginDate = declarationTemplateService.get(declarationTemplate.getId()).getVersion();
        Date dbVersionEndDate = declarationTemplateService.getDTEndDate(declarationTemplate.getId());
        if (dbVersionBeginDate.compareTo(declarationTemplate.getVersion()) !=0
                || (dbVersionEndDate != null && templateActualEndDate == null)
                || (dbVersionEndDate == null && templateActualEndDate != null)
                || (dbVersionEndDate != null && dbVersionEndDate.compareTo(templateActualEndDate) != 0) ){
            versionOperatingService.isIntersectionVersion(declarationTemplate.getId(), declarationTemplate.getType().getId(),
                    declarationTemplate.getStatus(), declarationTemplate.getVersion(), templateActualEndDate, logger);
            checkError(logger, SAVE_MESSAGE);
            //Выполенение шага 5.А.1.1
            Pair<Date, Date> beginRange = null;
            Pair<Date, Date> endRange = null;
            if (dbVersionBeginDate.compareTo(declarationTemplate.getVersion()) < 0) {
                Calendar c = Calendar.getInstance();
                c.setTime(declarationTemplate.getVersion());
                c.add(Calendar.DATE, -1);
                beginRange = new Pair<Date, Date>(dbVersionBeginDate, c.getTime());
            }
            if (
                    (dbVersionEndDate == null && templateActualEndDate != null)
                ||
                    (dbVersionEndDate != null && templateActualEndDate != null && dbVersionEndDate.compareTo(templateActualEndDate) > 0)) {
                Calendar c = Calendar.getInstance();
                c.setTime(templateActualEndDate);
                c.add(Calendar.DATE, 1);
                endRange = new Pair<Date, Date>(c.getTime(), dbVersionEndDate);
            }
            versionOperatingService.checkDestinationsSources(declarationTemplate.getType().getId(), beginRange, endRange, logger);
            checkError(logger, SAVE_MESSAGE);
            declarationDataService.findDDIdsByRangeInReportPeriod(declarationTemplate.getId(), declarationTemplate.getVersion(), templateActualEndDate, logger);
            checkError(logger, SAVE_MESSAGE);
        }

        if (declarationTemplate.getStatus().equals(VersionedObjectStatus.NORMAL)){
            versionOperatingService.isUsedVersion(declarationTemplate.getId(), declarationTemplate.getType().getId(),
                    declarationTemplate.getStatus(), declarationTemplate.getVersion(), templateActualEndDate, logger);
            checkError(logger, SAVE_MESSAGE);
        }

        List<Long> ddIds = declarationDataService.getFormDataListInActualPeriodByTemplate(declarationTemplate.getId(), declarationTemplate.getVersion());
        for (long declarationId : ddIds) {
            // Отменяем задачи формирования спец отчетов/удаляем спец отчеты
            declarationDataService.interruptTask(declarationId, user.getUser().getId(), ReportType.UPDATE_TEMPLATE_DEC, "Обновление макета");
        }

        int id = declarationTemplateService.save(declarationTemplate);

        auditService.add(FormDataEvent.TEMPLATE_MODIFIED, user, declarationTemplate.getVersion(),
                declarationTemplateService.getDTEndDate(id), declarationTemplate.getName(), null, null, null);
        logging(id, FormDataEvent.TEMPLATE_MODIFIED, user.getUser());
        return id;
    }

    @Override
    public <T> int createNewType(T template, Date templateActualEndDate, Logger logger, TAUserInfo user) {
        DeclarationTemplate declarationTemplate = (DeclarationTemplate)template;
        declarationTemplateService.validateDeclarationTemplate(declarationTemplate, logger);
        checkError(logger, SAVE_MESSAGE);
        DeclarationType type = declarationTemplate.getType();
        type.setStatus(VersionedObjectStatus.NORMAL);
        type.setName(declarationTemplate.getName() != null && !declarationTemplate.getName().isEmpty()?declarationTemplate.getName():"");
        int formTypeId = declarationTypeService.save(type);
        declarationTemplate.getType().setId(formTypeId);
        versionOperatingService.isIntersectionVersion(0, formTypeId, VersionedObjectStatus.NORMAL, declarationTemplate.getVersion(), templateActualEndDate, logger);
        checkError(logger, SAVE_MESSAGE);
        declarationTemplate.setStatus(VersionedObjectStatus.DRAFT);
        int id = declarationTemplateService.save(declarationTemplate);

        auditService.add(FormDataEvent.TEMPLATE_CREATED, user, declarationTemplate.getVersion(),
                declarationTemplateService.getDTEndDate(id), declarationTemplate.getName(), null, null, null);
        logging(id, FormDataEvent.TEMPLATE_CREATED, user.getUser());
        return id;
    }

    @Override
    public <T> int createNewTemplateVersion(T template, Date templateActualEndDate, Logger logger, TAUserInfo user) {
        DeclarationTemplate declarationTemplate = (DeclarationTemplate)template;
        declarationTemplateService.validateDeclarationTemplate(declarationTemplate, logger);
        checkError(logger, SAVE_MESSAGE);
        declarationTemplate.setStatus(VersionedObjectStatus.DRAFT);
        versionOperatingService.isIntersectionVersion(0, declarationTemplate.getType().getId(),
                declarationTemplate.getStatus(), declarationTemplate.getVersion(), templateActualEndDate, logger);
        checkError(logger, SAVE_MESSAGE);
        int id = declarationTemplateService.save(declarationTemplate);

        auditService.add(FormDataEvent.TEMPLATE_CREATED, user, declarationTemplate.getVersion(),
                declarationTemplateService.getDTEndDate(id), declarationTemplate.getName(), null, null, null);
        logging(id, FormDataEvent.TEMPLATE_CREATED, user.getUser());
        return id;
    }

    @Override
    public void deleteTemplate(int typeId, Logger logger, TAUserInfo user) {
        List<DeclarationTemplate> templates = declarationTemplateService.getDecTemplateVersionsByStatus(typeId,
                VersionedObjectStatus.NORMAL, VersionedObjectStatus.DRAFT);
        if (templates != null && !templates.isEmpty()){

            for (DeclarationTemplate declarationTemplate : templates){
                versionOperatingService.isUsedVersion(declarationTemplate.getId(), declarationTemplate.getType().getId(),
                        declarationTemplate.getStatus(), declarationTemplate.getVersion(), null, logger);
            }
            checkError(logger, DELETE_TEMPLATE_MESSAGE);
        }
        versionOperatingService.checkDestinationsSources(typeId, (Date) null, null, logger);
        checkError(logger, DELETE_TEMPLATE_MESSAGE);
        //Проверка назначений деклараций
        for (DepartmentDeclarationType departmentFormType : sourceService.getDDTByDeclarationType(typeId))
            logger.error(
                    String.format(HAVE_DDT_MESSAGE,
                            departmentService.getDepartment(departmentFormType.getDepartmentId()).getName()));
        checkError(logger, DELETE_TEMPLATE_MESSAGE);
        declarationTypeService.delete(typeId);
    }

    @Override
    public boolean deleteVersionTemplate(int templateId, Logger logger, TAUserInfo user) {
        boolean isDeleteAll = false;//переменная определяющая, удалена ли все версии макета
        DeclarationTemplate template = declarationTemplateService.get(templateId);
        Date dateEndActualize = declarationTemplateService.getDTEndDate(templateId);
        versionOperatingService.isUsedVersion(template.getId(), template.getType().getId(),
                template.getStatus(), template.getVersion(), dateEndActualize, logger);
        checkError(logger, DELETE_TEMPLATE_VERSION_MESSAGE);
        versionOperatingService.checkDestinationsSources(template.getType().getId(), template.getVersion(), dateEndActualize, logger);
        checkError(logger, DELETE_TEMPLATE_VERSION_MESSAGE);

        versionOperatingService.cleanVersions(template.getId(), template.getType().getId(),
                template.getStatus(), template.getVersion(), dateEndActualize, logger);
        Date endDate = declarationTemplateService.getDTEndDate(templateId);
        int deletedFTid = declarationTemplateService.delete(template.getId());
        List<DeclarationTemplate> declarationTemplates = declarationTemplateService.getDecTemplateVersionsByStatus(template.getType().getId(),
                VersionedObjectStatus.DRAFT, VersionedObjectStatus.NORMAL);
        if (declarationTemplates.isEmpty()){
            for (DepartmentFormType departmentFormType : sourceService.getDFTByFormType(template.getType().getId())){
                logger.error(
                        String.format(HAVE_DDT_MESSAGE,
                                departmentService.getDepartment(departmentFormType.getDepartmentId()).getName()));
                checkError(logger, DELETE_TEMPLATE_VERSION_MESSAGE);
            }
        }

        //Если нет версий макетов, то можно удалить весь макет
        if (declarationTemplates.isEmpty()){
            templateChangesService.deleteByTemplateIds(null, Arrays.asList(deletedFTid));
            declarationTypeService.delete(template.getType().getId());
            logger.info("Макет удален в связи с удалением его последней версии");
            isDeleteAll = true;
        }
        auditService.add(FormDataEvent.TEMPLATE_DELETED, user, template.getVersion(),
                endDate, template.getName(), null, null, null);
        logging(templateId, FormDataEvent.TEMPLATE_DELETED, user.getUser());
        return isDeleteAll;
    }

    @Override
    public boolean setStatusTemplate(int templateId, Logger logger, TAUserInfo user, boolean force) {
        DeclarationTemplate declarationTemplate = declarationTemplateService.get(templateId);

        if (declarationTemplate.getStatus() == VersionedObjectStatus.NORMAL){
            versionOperatingService.isUsedVersion(declarationTemplate.getId(), declarationTemplate.getType().getId(),
                    declarationTemplate.getStatus(), declarationTemplate.getVersion(), null, logger);
            if (!force && logger.containsLevel(LogLevel.ERROR)) return false;
            declarationTemplate.setStatus(VersionedObjectStatus.DRAFT);
            declarationTemplateService.updateVersionStatus(VersionedObjectStatus.DRAFT, templateId);
            logging(templateId, FormDataEvent.TEMPLATE_DEACTIVATED, user.getUser());
        } else {
            declarationTemplate.setStatus(VersionedObjectStatus.NORMAL);
            declarationTemplateService.updateVersionStatus(VersionedObjectStatus.NORMAL, templateId);
            logging(templateId, FormDataEvent.TEMPLATE_ACTIVATED, user.getUser());
        }
        return true;
    }

    @Override
    public void isInUsed(int templateId, int typeId, VersionedObjectStatus status, Date versionActualDateStart, Date versionActualDateEnd, Logger logger) {
        versionOperatingService.isUsedVersion(templateId, typeId, status, versionActualDateStart, versionActualDateEnd, logger);
    }

    private void checkError(Logger logger, String errorMsg){
        if (logger.containsLevel(LogLevel.ERROR))
            throw new ServiceLoggerException(errorMsg, logEntryService.save(logger.getEntries()));
    }

    private void logging(int id, FormDataEvent event, TAUser user){
        TemplateChanges changes = new TemplateChanges();
        changes.setEvent(event);
        changes.setEventDate(new Date());
        changes.setDeclarationTemplateId(id);
        changes.setAuthor(user);
        templateChangesService.save(changes);
    }
}
