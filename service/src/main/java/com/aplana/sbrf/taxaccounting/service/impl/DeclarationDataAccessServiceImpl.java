package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.DeclarationDataDao;
import com.aplana.sbrf.taxaccounting.dao.DeclarationTemplateDao;
import com.aplana.sbrf.taxaccounting.dao.api.DepartmentReportPeriodDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.AccessDeniedException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.DeclarationDataAccessService;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.service.SourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.Arrays.asList;

/**
 * Реализация сервиса для проверки прав на доступ к декларациям
 * 
 * @author dsultanbekov
 */
@Service
public class DeclarationDataAccessServiceImpl implements DeclarationDataAccessService {

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

	/**
	 * В сущности эта функция проверяет наличие прав на просмотр декларации,
	 * логика вынесена в отдельный метод, так как используется в нескольких
	 * местах данного сервиса
	 * 
	 * @param userInfo
	 *            информация о пользователе
	 * @param departmentReportPeriod Отчетный период подразделения
	 *
     * @param checkedSet
     *            необязательный параметр — набор проверенных наборов параметров, используется для оптимизации
	 */
	private void checkRolesForReading(TAUserInfo userInfo, DepartmentReportPeriod departmentReportPeriod, Set<String> checkedSet, Logger logger) {
        if (checkedSet != null) {
            String key = userInfo.getUser().getId() + "_" + departmentReportPeriod.getId();
            if (checkedSet.contains(key)) {
                return;
            }
            checkedSet.add(key);
        }

		Department declarationDepartment = departmentService.getDepartment(departmentReportPeriod.getDepartmentId());

		// Нельзя работать с декларациями в отчетном периоде вида "ввод остатков"
        if (departmentReportPeriod.isBalance()) {
            String msg = departmentReportPeriod.getReportPeriod().getTaxPeriod().getTaxType() == TaxType.DEAL ?
                    "Уведомление не может быть создано" : "Декларация не может быть создана";
            error(msg + " в периоде ввода остатков!", logger);
        }

        // Выборка для доступа к экземплярам деклараций
        // http://conf.aplana.com/pages/viewpage.action?pageId=11380670

		// Контролёр УНП может просматривать все декларации
		if (userInfo.getUser().hasRole(TARole.ROLE_CONTROL_UNP)) {
			return;
		}

        // Контролёр или Контролёр НС
        if (userInfo.getUser().hasRole(TARole.ROLE_CONTROL_NS) || userInfo.getUser().hasRole(TARole.ROLE_CONTROL)) {
            ReportPeriod reportPeriod = departmentReportPeriod.getReportPeriod();
			List<Integer> executors = departmentService.getTaxFormDepartments(userInfo.getUser(),
					asList(reportPeriod.getTaxPeriod().getTaxType()), reportPeriod.getCalendarStartDate(),
					reportPeriod.getEndDate());
			if (reportPeriod != null && executors.contains(declarationDepartment.getId())) {
				return;
			}
        }

        // Прочие
        error("Нет прав на доступ к декларации", logger);
	}

	private void canRead(TAUserInfo userInfo, long declarationDataId, Set<String> checkedSet) {
		DeclarationData declaration = declarationDataDao.get(declarationDataId);
		// Просматривать декларацию может только контролёр УНП и контролёр
		// текущего уровня для обособленных подразделений
		checkRolesForReading(userInfo, departmentReportPeriodDao.get(declaration.getDepartmentReportPeriodId()), checkedSet, null);
	}

	private void canCreate(TAUserInfo userInfo, int declarationTemplateId, DepartmentReportPeriod departmentReportPeriod,
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
                declarationTemplate.getType().getTaxType(), reportPeriod.getCalendarStartDate(), reportPeriod.getEndDate());
		boolean found = false;
		for (DepartmentDeclarationType ddt : ddts) {
			if (ddt.getDeclarationTypeId() == declarationTypeId) {
                found = true;
				break;
			}
		}
		if (!found) {
            error("Выбранный вид декларации не назначен подразделению", logger);
		}
		// Создавать декларацию могут только контролёры УНП и контролёры
		// текущего уровня обособленного подразделения
		checkRolesForReading(userInfo, departmentReportPeriod, checkedSet, logger);
	}

	private void canAccept(TAUserInfo userInfo, long declarationDataId, Set<String> checkedSet) {
		DeclarationData declaration = declarationDataDao.get(declarationDataId);
		// Принять декларацию можно только если она еще не принята
		if (declaration.getState().equals(State.ACCEPTED)) {
			throw new AccessDeniedException("Налоговая форма уже принята");
		}

        DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodDao.get(declaration.getDepartmentReportPeriodId());

        // Нельзя принимать декларацию в закрытом периоде
        if (!departmentReportPeriod.isActive()) {
            throw new AccessDeniedException("Период закрыт");
        }
		// Принять декларацию могут только контолёр текущего уровня
		// обособленного подразделения и контролёр УНП
		checkRolesForReading(userInfo, departmentReportPeriodDao.get(declaration.getDepartmentReportPeriodId()), checkedSet, null);
	}

	private void canReject(TAUserInfo userInfo, long declarationDataId, Set<String> checkedSet) {
		DeclarationData declaration = declarationDataDao.get(declarationDataId);
		// Отменить принятие декларации можно только если она принята
		if (!declaration.getState().equals(State.ACCEPTED)) {
			throw new AccessDeniedException("Налоговая форма не принята");
		}

        DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodDao.get(declaration.getDepartmentReportPeriodId());

        // Нельзя возвращать декларацию в закрытом периоде
        if (!departmentReportPeriod.isActive()) {
            throw new AccessDeniedException("Период закрыт");
        }
		// Отменить принятие декларацию могут только контолёр текущего уровня и
		// контролёр УНП
		checkRolesForReading(userInfo, departmentReportPeriodDao.get(declaration.getDepartmentReportPeriodId()), checkedSet, null);
	}

	private void canDelete(TAUserInfo userInfo, long declarationDataId, Set<String> checkedSet) {
		DeclarationData declaration = declarationDataDao.get(declarationDataId);
		// Удалять декларацию можно только если она не принята
		if (declaration.getState().equals(State.ACCEPTED)) {
			throw new AccessDeniedException("Налоговая форма принята");
		}

        DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodDao.get(declaration.getDepartmentReportPeriodId());

        // Нельзя удалить декларацию в закрытом периоде
        if (!departmentReportPeriod.isActive()) {
            throw new AccessDeniedException("Период закрыт");
        }
		// Удалять могут только контролёр текущего уровня и контролёр УНП
        checkRolesForReading(userInfo, departmentReportPeriodDao.get(declaration.getDepartmentReportPeriodId()), checkedSet, null);
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
        // Обновлять декларацию могут только контолёр текущего уровня и
		// контролёр УНП
		checkRolesForReading(userInfo, departmentReportPeriodDao.get(declaration.getDepartmentReportPeriodId()), checkedSet, null);
	}

    @Override
    public void checkEvents(TAUserInfo userInfo, Long declarationDataId, FormDataEvent scriptEvent) {
        checkEvents(userInfo, declarationDataId, scriptEvent, null);
    }

    private void checkEvents(TAUserInfo userInfo, Long declarationDataId, FormDataEvent scriptEvent, Set<String> checkedSet) {
        switch (scriptEvent) {
            case MOVE_CREATED_TO_ACCEPTED:
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
            default:
                throw new AccessDeniedException("Операция не предусмотрена в системе");
        }
    }

    @Override
    public void checkEvents(TAUserInfo userInfo, int declarationTemplateId, DepartmentReportPeriod departmentReportPeriod,
                            FormDataEvent scriptEvent, Logger logger) {
        checkEvents(userInfo, declarationTemplateId, departmentReportPeriod, scriptEvent, null, logger);
    }

    private void checkEvents(TAUserInfo userInfo, int declarationTemplateId, DepartmentReportPeriod departmentReportPeriod,
                             FormDataEvent scriptEvent, Set<String> checkedSet, Logger logger) {
        switch (scriptEvent) {
            case CREATE:
                canCreate(userInfo, declarationTemplateId, departmentReportPeriod, checkedSet, logger);
                break;
            default:
                throw new AccessDeniedException("Операция не предусмотрена в системе");
        }
    }

	@Override
	public Set<FormDataEvent> getPermittedEvents(TAUserInfo userInfo,
			Long declarationDataId) {
		Set<FormDataEvent> result = new HashSet<FormDataEvent>();
        Set<String> checkedSet = new HashSet<String>();
		for (FormDataEvent scriptEvent : FormDataEvent.values()) {
			try{
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
			try{
				checkEvents(userInfo, declarationTemplateId, departmentReportPeriod, scriptEvent, checkedSet, null);
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
     * @param msg текст ошибки
     * @param logger логгер
     */
    private void error(String msg, Logger logger) {
        if (logger == null) {
            throw new AccessDeniedException(msg);
        } else {
            logger.error(msg);
        }
    }
}
