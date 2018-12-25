package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
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

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * User: avanteev
 */
@Service("declarationTemplateMainOperatingService")
@Transactional
public class MainOperatingDTServiceImpl implements MainOperatingService {

    private static final String CHECK_ROLE_MESSAGE = "Нет прав доступа к данному виду налогу \"%s\"!";

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
    @Autowired
    private TAUserService userService;

    @Override
    public <T> boolean edit(T template, Date templateActualEndDate, Logger logger, TAUserInfo user) {
        return edit(template, null, templateActualEndDate, logger, user, null);
    }

    @Override
    public <T> boolean edit(T template, List<DeclarationTemplateCheck> checks, Date templateActualEndDate, Logger logger, TAUserInfo user) {
        return edit(template, checks, templateActualEndDate, logger, user, null);
    }

    @Override
    public <T> boolean edit(T template, List<DeclarationTemplateCheck> checks, Date templateActualEndDate, Logger logger, TAUserInfo user, Boolean force) {
        DeclarationTemplate declarationTemplate = (DeclarationTemplate)template;
        checkRole(TaxType.NDFL, user.getUser());
        declarationTemplateService.validateDeclarationTemplate(declarationTemplate, logger);
        checkError(logger, SAVE_MESSAGE);
        Date dbVersionBeginDate = declarationTemplateService.get(declarationTemplate.getId()).getVersion();
        Date dbVersionEndDate = declarationTemplateService.getDTEndDate(declarationTemplate.getId());
        if (dbVersionBeginDate.compareTo(declarationTemplate.getVersion()) !=0
                || (dbVersionEndDate != null && templateActualEndDate == null)
                || (dbVersionEndDate == null && templateActualEndDate != null)
                || (dbVersionEndDate != null && dbVersionEndDate.compareTo(templateActualEndDate) != 0) ){
            versionOperatingService.isIntersectionVersion(declarationTemplate.getId(), declarationTemplate.getType().getId(),
                    declarationTemplate.getStatus(), declarationTemplate.getVersion(), templateActualEndDate, logger, user);
            checkError(logger, SAVE_MESSAGE);
            //Выполенение шага 5.А.1.1
            Pair<Date, Date> beginRange = null;
            Pair<Date, Date> endRange = null;
            if (dbVersionBeginDate.compareTo(declarationTemplate.getVersion()) < 0) {
                Calendar c = Calendar.getInstance();
                c.setTime(declarationTemplate.getVersion());
                c.add(Calendar.DATE, -1);
                beginRange = new Pair<>(dbVersionBeginDate, c.getTime());
            }
            if (
                    (dbVersionEndDate == null && templateActualEndDate != null)
                ||
                    (dbVersionEndDate != null && templateActualEndDate != null && dbVersionEndDate.compareTo(templateActualEndDate) > 0)) {
                Calendar c = Calendar.getInstance();
                c.setTime(templateActualEndDate);
                c.add(Calendar.DATE, 1);
                endRange = new Pair<>(c.getTime(), dbVersionEndDate);
            }
            versionOperatingService.checkDestinationsSources(declarationTemplate.getType().getId(), beginRange, endRange, logger);
            checkError(logger, SAVE_MESSAGE);
            declarationDataService.findDDIdsByRangeInReportPeriod(declarationTemplate.getId(), declarationTemplate.getVersion(), templateActualEndDate, logger);
            checkError(logger, SAVE_MESSAGE);
        }

        if ((force == null || !force) && declarationTemplate.getStatus().equals(VersionedObjectStatus.NORMAL)){
            boolean isUsedVersion = versionOperatingService.isUsedVersion(declarationTemplate.getId(), declarationTemplate.getType().getId(),
                    declarationTemplate.getStatus(), declarationTemplate.getVersion(), templateActualEndDate, logger);
            if (force == null)
                checkError(logger, SAVE_MESSAGE);
            else {
                if (isUsedVersion)
                    return false;
            }
        }

        List<Long> ddIds = declarationDataService.getFormDataListInActualPeriodByTemplate(declarationTemplate.getId(), declarationTemplate.getVersion());
        for (long declarationId : ddIds) {
            // Отменяем задачи формирования спец отчетов/удаляем спец отчеты
            declarationDataService.interruptAsyncTask(declarationId, user, AsyncTaskType.UPDATE_TEMPLATE_DEC, TaskInterruptCause.DECLARATION_TEMPLATE_UPDATE);
        }

        declarationTemplateService.save(declarationTemplate, user);
        logger.info("Изменения сохранены");
        int id = declarationTemplate.getId();

        List<DeclarationTemplateCheck> oldChecks = declarationTemplateService.getChecks(declarationTemplate.getType().getId(), declarationTemplate.getId());
        if (checks != null && !oldChecks.containsAll(checks) && !checks.containsAll(oldChecks)) {
            declarationTemplateService.updateChecks(checks, declarationTemplate.getId());
            auditService.add(FormDataEvent.TEMPLATE_MODIFIED, user, declarationTemplate.getVersion(),
                    declarationTemplateService.getDTEndDate(id), null, declarationTemplate.getName(), "Обновлена информация о фатальности проверок НФ", null);
        }

        auditService.add(FormDataEvent.TEMPLATE_MODIFIED, user, declarationTemplate.getVersion(),
                declarationTemplateService.getDTEndDate(id), null, declarationTemplate.getName(), null, null);
        logging(id, FormDataEvent.TEMPLATE_MODIFIED, user.getUser());

        return true;
    }

    @Override
    public boolean setStatusTemplate(int templateId, Logger logger, TAUserInfo user, boolean force) {
        DeclarationTemplate declarationTemplate = declarationTemplateService.get(templateId);
        checkRole(TaxType.NDFL, user.getUser());
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

    @Override
    public void logging(int id, FormDataEvent event, TAUser user){
        TemplateChanges changes = new TemplateChanges();
        changes.setEvent(event);
        changes.setEventDate(new Date());
        changes.setDeclarationTemplateId(id);
        changes.setAuthor(user);
        templateChangesService.save(changes);
    }

    private void checkRole(TaxType taxType, TAUser user) {
        if (!user.hasRole(taxType, TARole.N_ROLE_CONF)) {
            throw new ServiceException(CHECK_ROLE_MESSAGE, taxType.getName());
        }
    }
}
