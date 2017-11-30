package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.DeclarationDataDao;
import com.aplana.sbrf.taxaccounting.dao.DeclarationTemplateDao;
import com.aplana.sbrf.taxaccounting.dao.api.DepartmentReportPeriodDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.AccessDeniedException;
import com.aplana.sbrf.taxaccounting.model.exception.TAInterruptedException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.DeclarationDataAccessService;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.service.SourceService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Реализация сервиса для проверки прав на доступ к декларациям
 *
 * @author dsultanbekov
 */
@Service
public class DeclarationDataAccessServiceImpl implements DeclarationDataAccessService {
    private static final Log LOG = LogFactory.getLog(DeclarationDataAccessServiceImpl.class);

    @Autowired
    private DeclarationTemplateDao declarationTemplateDao;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private DeclarationDataDao declarationDataDao;

    @Autowired
    private SourceService sourceService;

    @Autowired
    private DepartmentReportPeriodDao departmentReportPeriodDao;

    @Autowired
    private RefBookFactory refBookFactory;

    private static final String ACCESS_ERR_MSG_FMT = "Нет прав на доступ к налоговой форме. Проверьте назначение формы РНУ НДФЛ (первичная) для подразделения «%s» в «Назначении налоговых форм»%s.";

    /**
     * В сущности эта функция проверяет наличие прав на просмотр декларации,
     * логика вынесена в отдельный метод, так как используется в нескольких
     * местах данного сервиса
     *
     * @param userInfo               информация о пользователе
     * @param declarationTemplate    шаблон декларации
     * @param departmentReportPeriod Отчетный период подразделения
     * @param asnuId                 id АСНУ ТФ
     * @param checkedSet             необязательный параметр — набор проверенных наборов параметров, используется для оптимизации
     */
    private void checkRolesForReading(TAUserInfo userInfo, DeclarationTemplate declarationTemplate, DepartmentReportPeriod departmentReportPeriod, Long asnuId, Set<String> checkedSet, Logger logger) {
        if (checkedSet != null) {
            String key = userInfo.getUser().getId() + "_" + departmentReportPeriod.getId();
            if (checkedSet.contains(key)) {
                return;
            }
            checkedSet.add(key);
        }

        //Подразделение формы
        Department declDepartment = departmentService.getDepartment(departmentReportPeriod.getDepartmentId());

        TaxType taxType = TaxType.NDFL;

        // Выборка для доступа к экземплярам деклараций
        // http://conf.aplana.com/pages/viewpage.action?pageId=11380670

        // Контролёр УНП может просматривать все декларации
        if (userInfo.getUser().hasRoles(taxType, TARole.N_ROLE_CONTROL_UNP)) {
            return;
        }

        // Контролёр НС
        if (userInfo.getUser().hasRoles(taxType, TARole.N_ROLE_CONTROL_NS)) {
            //ТБ формы
            int declarationTB = departmentService.getParentTB(declDepartment.getId()).getId();
            //Подразделение и ТБ пользователя
            int userTB = departmentService.getParentTB(userInfo.getUser().getDepartmentId()).getId();

            //Подразделение формы и подразделение пользователя должны относиться к одному ТБ или
            if (userTB == declarationTB) {
                return;
            }

            //ТБ подразделений, для которых подразделение пользователя является исполнителем макетов
            List<Integer> tbDepartments = departmentService.getAllTBPerformers(userTB, declarationTemplate.getType());

            //Подразделение формы относится к одному из ТБ подразделений, для которых подразделение пользователя является исполнителем
            if (tbDepartments.contains(declarationTB)) {
                return;
            }
        }

        // Оператор (НДФЛ или Сборы)
        if (userInfo.getUser().hasRoles(taxType, TARole.N_ROLE_OPER)) {
            if (asnuId != null && !checkUserAsnu(userInfo, asnuId)) {
                throw new AccessDeniedException("Нет прав на доступ к форме");
            }

            List<Integer> executors = departmentService.getTaxDeclarationDepartments(userInfo.getUser(), declarationTemplate.getType());
            if (executors.contains(declDepartment.getId())) {
                if (!declarationTemplate.getDeclarationFormKind().equals(DeclarationFormKind.CONSOLIDATED)) {
                    return;
                }
            }
        }

        // Прочие
        String asnuMsgPart = "";
        if (asnuId != null) {
            RefBookDataProvider asnuProvider = refBookFactory.getDataProvider(RefBook.Id.ASNU.getId());
            String asnuName = asnuProvider.getRecordData(asnuId).get("NAME").getStringValue();
            asnuMsgPart = String.format(" и наличие доступа к АСНУ «%s»", asnuName);
        }
        error(String.format(
                ACCESS_ERR_MSG_FMT,
                declDepartment.getName(),
                asnuMsgPart
        ), logger);
    }

    private void canRead(TAUserInfo userInfo, long declarationDataId, Set<String> checkedSet) {
        DeclarationData declaration = declarationDataDao.get(declarationDataId);
        DeclarationTemplate declarationTemplate = declarationTemplateDao.get(declaration.getDeclarationTemplateId());

        // Просматривать декларацию может только контролёр УНП и контролёр
        // текущего уровня для обособленных подразделений
        checkRolesForReading(userInfo, declarationTemplate, departmentReportPeriodDao.get(declaration.getDepartmentReportPeriodId()), declaration.getAsnuId(), checkedSet, null);
    }

    private void canCreate(TAUserInfo userInfo, int declarationTemplateId, DepartmentReportPeriod departmentReportPeriod, Long asnuId,
                           Set<String> checkedSet, Logger logger) {
        // Для начала проверяем, что в данном подразделении вообще можно
        // работать с декларациями данного вида
        if (!departmentReportPeriod.isActive()) {
            error("Выбранный период закрыт", logger);
        }
        DeclarationTemplate declarationTemplate = declarationTemplateDao.get(declarationTemplateId);
        int declarationTypeId = declarationTemplate.getType().getId();

        ReportPeriod reportPeriod = departmentReportPeriod.getReportPeriod();
        List<DepartmentDeclarationType> ddts = sourceService.getDDTByDepartment(departmentReportPeriod.getDepartmentId(),
                TaxType.NDFL, reportPeriod.getCalendarStartDate(), reportPeriod.getEndDate());
        boolean found = false;
        for (DepartmentDeclarationType ddt : ddts) {
            if (ddt.getDeclarationTypeId() == declarationTypeId) {
                found = true;
                break;
            }
        }
        if (!found) {
            error("Выбранный вид налоговой формы не назначен подразделению", logger);
        }
        // Создавать декларацию могут только контролёры УНП и контролёры
        // текущего уровня обособленного подразделения
        checkRolesForReading(userInfo, declarationTemplate, departmentReportPeriod, asnuId, checkedSet, logger);
    }

    private void canAccept(TAUserInfo userInfo, long declarationDataId, Set<String> checkedSet) {
        DeclarationData declaration = declarationDataDao.get(declarationDataId);
        // Принять декларацию можно только если она еще не принята
        if (declaration.getState().equals(State.ACCEPTED)) {
            throw new AccessDeniedException("Налоговая форма уже принята");
        }

        // Принять декларацию можно только из состояния подготовлено
        if (declaration.getState().equals(State.CREATED)) {
            throw new AccessDeniedException("Переход в состояние \"" + State.ACCEPTED.getTitle() + "\" из текущего состояния невозможен");
        }

        DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodDao.get(declaration.getDepartmentReportPeriodId());

        // Нельзя принимать декларацию в закрытом периоде
        if (!departmentReportPeriod.isActive()) {
            throw new AccessDeniedException("Период закрыт");
        }

        DeclarationTemplate declarationTemplate = declarationTemplateDao.get(declaration.getDeclarationTemplateId());

        if (!userInfo.getUser().hasRoles(TaxType.NDFL, TARole.N_ROLE_CONTROL_NS, TARole.N_ROLE_CONTROL_UNP)) {
            throw new AccessDeniedException("Нет прав на принятие налоговой формы");
        }

        // Принять декларацию могут только контолёр текущего уровня
        // обособленного подразделения и контролёр УНП
        checkRolesForReading(userInfo, declarationTemplate, departmentReportPeriod, declaration.getAsnuId(), checkedSet, null);
    }

    private void canReject(TAUserInfo userInfo, long declarationDataId, Set<String> checkedSet) {
        DeclarationData declaration = declarationDataDao.get(declarationDataId);
        // Отменить принятие декларации можно только если она принята
        if (declaration.getState().equals(State.CREATED)) {
            throw new AccessDeniedException("Налоговая форма уже находиться в статусе \"Создана\"");
        }

        DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodDao.get(declaration.getDepartmentReportPeriodId());

        // Нельзя возвращать декларацию в закрытом периоде
        if (!departmentReportPeriod.isActive()) {
            throw new AccessDeniedException("Период закрыт");
        }

        DeclarationTemplate declarationTemplate = declarationTemplateDao.get(declaration.getDeclarationTemplateId());

        if (!userInfo.getUser().hasRoles(TaxType.NDFL, TARole.N_ROLE_CONTROL_NS, TARole.F_ROLE_CONTROL_NS, TARole.N_ROLE_CONTROL_UNP, TARole.F_ROLE_CONTROL_UNP)) {
            throw new AccessDeniedException("Нет прав на отмену принятия налоговой формы");
        }

        // Отменить принятие декларацию могут только контолёр текущего уровня и
        // контролёр УНП
        checkRolesForReading(userInfo, declarationTemplate, departmentReportPeriod, declaration.getAsnuId(), checkedSet, null);
    }

    private void canDelete(TAUserInfo userInfo, long declarationDataId, Set<String> checkedSet) {
        DeclarationData declaration = declarationDataDao.get(declarationDataId);
        // Удалять декларацию можно только если она не принята
        if (!declaration.getState().equals(State.CREATED)) {
            throw new AccessDeniedException("Налоговая форма должна находиться в статусе \"Создана\"");
        }

        DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodDao.get(declaration.getDepartmentReportPeriodId());

        DeclarationTemplate declarationTemplate = declarationTemplateDao.get(declaration.getDeclarationTemplateId());

        // Удалять могут только контролёр текущего уровня и контролёр УНП
        checkRolesForReading(userInfo, declarationTemplate, departmentReportPeriod, declaration.getAsnuId(), checkedSet, null);
    }

    private void canRefresh(TAUserInfo userInfo, long declarationDataId, Set<String> checkedSet) {
        DeclarationData declaration = declarationDataDao.get(declarationDataId);
        // Обновлять декларацию можно только если она не принята
        if (declaration.getState().equals(State.ACCEPTED)) {
            throw new AccessDeniedException("Налоговая форма принята");
        }

        DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodDao.get(declaration.getDepartmentReportPeriodId());

        // Нельзя обновить декларацию в закрытом периоде
        if (!departmentReportPeriod.isActive()) {
            throw new AccessDeniedException("Период закрыт");
        }

        DeclarationTemplate declarationTemplate = declarationTemplateDao.get(declaration.getDeclarationTemplateId());

        // Обновлять декларацию могут только контолёр текущего уровня и
        // контролёр УНП
        checkRolesForReading(userInfo, declarationTemplate, departmentReportPeriod, declaration.getAsnuId(), checkedSet, null);
    }

    private void canChangeStatus(TAUserInfo userInfo, long declarationDataId, Set<String> checkedSet) {
        DeclarationData declaration = declarationDataDao.get(declarationDataId);
        // Обновлять декларацию можно только если она не принята
        if (!declaration.getState().equals(State.ACCEPTED)) {
            throw new AccessDeniedException("Налоговая форма должна находиться в статусе \"Принята\"");
        }

        DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodDao.get(declaration.getDepartmentReportPeriodId());

        DeclarationTemplate declarationTemplate = declarationTemplateDao.get(declaration.getDeclarationTemplateId());

        if (!userInfo.getUser().hasRoles(TaxType.NDFL, TARole.N_ROLE_CONTROL_NS, TARole.F_ROLE_CONTROL_NS, TARole.N_ROLE_CONTROL_UNP, TARole.F_ROLE_CONTROL_UNP)) {
            throw new AccessDeniedException("Нет прав на изменение состояния ЭД");
        }

        if (!declarationTemplate.getDeclarationFormKind().equals(DeclarationFormKind.REPORTS)) {
            throw new AccessDeniedException("Для данной формы нельзя измененить состояния ЭД");
        }

        // Обновлять декларацию могут только контолёр текущего уровня и
        // контролёр УНП
        checkRolesForReading(userInfo, declarationTemplate, departmentReportPeriod, declaration.getAsnuId(), checkedSet, null);
    }

    private void canCheck(TAUserInfo userInfo, long declarationDataId, Set<String> checkedSet) {
        DeclarationData declaration = declarationDataDao.get(declarationDataId);
        // Обновлять декларацию можно только если она не принята
        if (declaration.getState().equals(State.ACCEPTED)) {
            throw new AccessDeniedException("Налоговая форма должна находиться в статусе отличной от \"Принята\"");
        }

        DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodDao.get(declaration.getDepartmentReportPeriodId());

        DeclarationTemplate declarationTemplate = declarationTemplateDao.get(declaration.getDeclarationTemplateId());

        // Обновлять декларацию могут только контолёр текущего уровня и
        // контролёр УНП
        checkRolesForReading(userInfo, declarationTemplate, departmentReportPeriod, declaration.getAsnuId(), checkedSet, null);
    }

    @Override
    public void checkEvents(TAUserInfo userInfo, Long declarationDataId, FormDataEvent scriptEvent) {
        checkEvents(userInfo, declarationDataId, scriptEvent, null);
    }

    private void checkEvents(TAUserInfo userInfo, Long declarationDataId, FormDataEvent scriptEvent, Set<String> checkedSet) {
        if (Thread.currentThread().isInterrupted()) {
            LOG.info("Thread " + Thread.currentThread().getName() + " was interrupted");
            throw new TAInterruptedException();
        }
        switch (scriptEvent) {
            case MOVE_PREPARED_TO_ACCEPTED:
                canAccept(userInfo, declarationDataId, checkedSet);
                break;
            case MOVE_ACCEPTED_TO_CREATED:
                canReject(userInfo, declarationDataId, checkedSet);
                break;
            case GET_LEVEL0:
            case GET_LEVEL1:
                canRead(userInfo, declarationDataId, checkedSet);
                break;
            case DELETE:
                canDelete(userInfo, declarationDataId, checkedSet);
                break;
            case CALCULATE:
                canRefresh(userInfo, declarationDataId, checkedSet);
                break;
            case CHANGE_STATUS_ED:
                canChangeStatus(userInfo, declarationDataId, checkedSet);
                break;
            case CHECK:
                canCheck(userInfo, declarationDataId, checkedSet);
                break;
            default:
                throw new AccessDeniedException("Операция не предусмотрена в системе");
        }
    }

    @Override
    public void checkEvents(TAUserInfo userInfo, int declarationTemplateId, DepartmentReportPeriod departmentReportPeriod, Long asnuID,
                            FormDataEvent scriptEvent, Logger logger) {
        checkEvents(userInfo, declarationTemplateId, departmentReportPeriod, asnuID, scriptEvent, null, logger);
    }

    private void checkEvents(TAUserInfo userInfo, int declarationTemplateId, DepartmentReportPeriod departmentReportPeriod, Long asnuID,
                             FormDataEvent scriptEvent, Set<String> checkedSet, Logger logger) {
        if (Thread.currentThread().isInterrupted()) {
            LOG.info("Thread " + Thread.currentThread().getName() + " was interrupted");
            throw new TAInterruptedException();
        }
        switch (scriptEvent) {
            case CREATE:
                canCreate(userInfo, declarationTemplateId, departmentReportPeriod, asnuID, checkedSet, logger);
                break;
            default:
                throw new AccessDeniedException("Операция не предусмотрена в системе");
        }
    }

    @Override
    public Set<FormDataEvent> getPermittedEvents(TAUserInfo userInfo, Long declarationDataId) {
        Set<FormDataEvent> result = new HashSet<FormDataEvent>();
        Set<String> checkedSet = new HashSet<String>();
        for (FormDataEvent scriptEvent : FormDataEvent.values()) {
            try {
                checkEvents(userInfo, declarationDataId, scriptEvent, checkedSet);
                result.add(scriptEvent);
            } catch (Exception e) {
                // Nothink
            }
        }
        return result;
    }

    @Override
    public Set<FormDataEvent> getPermittedEvents(TAUserInfo userInfo, int declarationTemplateId,
                                                 DepartmentReportPeriod departmentReportPeriod) {
        Set<FormDataEvent> result = new HashSet<FormDataEvent>();
        Set<String> checkedSet = new HashSet<String>();
        for (FormDataEvent scriptEvent : FormDataEvent.values()) {
            try {
                checkEvents(userInfo, declarationTemplateId, departmentReportPeriod, null, scriptEvent, checkedSet, null);
                result.add(scriptEvent);
            } catch (Exception e) {
                // Nothink
            }
        }
        return result;
    }

    /**
     * Выбросить исключение или записать в лог.
     *
     * @param msg    текст ошибки
     * @param logger логгер
     */
    private void error(String msg, Logger logger) {
        if (logger == null) {
            throw new AccessDeniedException(msg);
        } else {
            logger.error(msg);
        }
    }

    /**
     * Проверяет есть у пользователя права на АСНУ декларации.
     *
     * @param userInfo пользователь
     * @param asnuId   АСНУ НФ, для ПНФ значение должно быть задано, для остальных форм null
     */
    private boolean checkUserAsnu(TAUserInfo userInfo, Long asnuId) {
        if (userInfo.getUser().hasRole(TARole.N_ROLE_OPER_ALL)) {
            return true;
        }

        return userInfo.getUser().getAsnuIds().contains(asnuId);
    }
}
