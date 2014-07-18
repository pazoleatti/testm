package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.DeclarationDataDao;
import com.aplana.sbrf.taxaccounting.dao.DeclarationTemplateDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.AccessDeniedException;
import com.aplana.sbrf.taxaccounting.service.DeclarationDataAccessService;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.service.PeriodService;
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
	private PeriodService reportPeriodService;
	
	@Autowired
	private SourceService sourceService;

	/**
	 * В сущности эта функция проверяет наличие прав на просмотр декларации,
	 * логика вынесена в отдельный метод, так как используется в нескольких
	 * местах данного сервиса
	 * 
	 * @param userInfo
	 *            информация о пользователе
	 * @param declarationDepartmentId
	 *            идентификатор подразделения, к которому относится декларация
	 * @param reportPeriodId
	 *            идентификатор отчетного периода
     * @param checkedSet
     *            необязательный параметр — набор проверенных наборов параметров, используется для оптимизации
	 */
	private void checkRolesForReading(TAUserInfo userInfo,
			int declarationDepartmentId, int reportPeriodId, Set<String> checkedSet) {

        if (checkedSet != null) {
            String key = userInfo.getUser().getId() + "_" + declarationDepartmentId + "_" + reportPeriodId;
            if (checkedSet.contains(key)) {
                return;
            }
            checkedSet.add(key);
        }

		Department declarationDepartment = departmentService.getDepartment(declarationDepartmentId);

		// Нельзя работать с декларациями в отчетном периоде вида "ввод остатков"
		if (reportPeriodService.isBalancePeriod(reportPeriodId, Long.valueOf(declarationDepartment.getId()))) {
			throw new AccessDeniedException("Декларация в отчетном периоде вида для ввода остатков");
		}

        // Выборка для доступа к экземплярам деклараций
        // http://conf.aplana.com/pages/viewpage.action?pageId=11380670

		// Контролёр УНП может просматривать все декларации
		if (userInfo.getUser().hasRole(TARole.ROLE_CONTROL_UNP)) {
			return;
		}

        // Контролёр или Контролёр НС
        if (userInfo.getUser().hasRole(TARole.ROLE_CONTROL_NS) || userInfo.getUser().hasRole(TARole.ROLE_CONTROL)) {
            ReportPeriod reportPeriod = reportPeriodService.getReportPeriod(reportPeriodId);
			if (reportPeriod != null && departmentService.getTaxFormDepartments(userInfo.getUser(),
					asList(reportPeriod.getTaxPeriod().getTaxType()), reportPeriod.getCalendarStartDate(), reportPeriod.getEndDate()).contains(declarationDepartment.getId())) {
				return;
			}
        }

        // Прочие
        throw new AccessDeniedException("Нет прав на доступ к декларации");
	}

	private void canRead(TAUserInfo userInfo, long declarationDataId, Set<String> checkedSet) {
		DeclarationData declaration = declarationDataDao.get(declarationDataId);
		// Просматривать декларацию может только контролёр УНП и контролёр
		// текущего уровня для обособленных подразделений
		checkRolesForReading(userInfo, declaration.getDepartmentId(),
				declaration.getReportPeriodId(), checkedSet);
	}

	private void canCreate(TAUserInfo userInfo, int declarationTemplateId,
			int departmentId, int reportPeriodId, Set<String> checkedSet) {
		// Для начала проверяем, что в данном подразделении вообще можно
		// работать с декларациями данного вида
		if (!reportPeriodService.isActivePeriod(reportPeriodId, departmentId)) {
			throw new AccessDeniedException("Выбранный период закрыт");
		}
		DeclarationTemplate declarationTemplate = declarationTemplateDao
				.get(declarationTemplateId);
		int declarationTypeId = declarationTemplate.getType()
				.getId();

        ReportPeriod reportPeriod = reportPeriodService.getReportPeriod(reportPeriodId);
		List<DepartmentDeclarationType> ddts = sourceService.getDDTByDepartment(departmentId, declarationTemplate.getType().getTaxType(),
                reportPeriod.getCalendarStartDate(), reportPeriod.getEndDate());
		boolean found = false;
		for (DepartmentDeclarationType ddt : ddts) {
			if (ddt.getDeclarationTypeId() == declarationTypeId) {
				found = true;
				break;
			}
		}
		if (!found) {
			throw new AccessDeniedException("Выбранный вид декларации не назначен подразделению");
		}
		// Создавать декларацию могут только контролёры УНП и контролёры
		// текущего уровня обособленного подразделения
		checkRolesForReading(userInfo, departmentId, reportPeriodId, checkedSet);
	}

	private void canAccept(TAUserInfo userInfo, long declarationDataId, Set<String> checkedSet) {
		DeclarationData declaration = declarationDataDao.get(declarationDataId);
		// Принять декларацию можно только если она еще не принята
		if (declaration.isAccepted()) {
			throw new AccessDeniedException("Декларация уже принята");
		}

        // Нельзя принимать декларацию в закрытом периоде
        if (!reportPeriodService.isActivePeriod(declaration.getReportPeriodId(), declaration.getDepartmentId())) {
            throw new AccessDeniedException("Период закрыт");
        }
		// Принять декларацию могут только контолёр текущего уровня
		// обособленного подразделения и контролёр УНП
		checkRolesForReading(userInfo, declaration.getDepartmentId(),
				declaration.getReportPeriodId(), checkedSet);
	}

	private void canReject(TAUserInfo userInfo, long declarationDataId, Set<String> checkedSet) {
		DeclarationData declaration = declarationDataDao.get(declarationDataId);
		// Отменить принятие декларации можно только если она принята
		if (!declaration.isAccepted()) {
			throw new AccessDeniedException("Декларация не принята");
		}
        // Нельзя возвращать декларацию в закрытом периоде
        if (!reportPeriodService.isActivePeriod(declaration.getReportPeriodId(), declaration.getDepartmentId())) {
            throw new AccessDeniedException("Период закрыт");
        }
		// Отменить принятие декларацию могут только контолёр текущего уровня и
		// контролёр УНП
		checkRolesForReading(userInfo, declaration.getDepartmentId(),
				declaration.getReportPeriodId(), checkedSet);
	}

	private void canDelete(TAUserInfo userInfo, long declarationDataId, Set<String> checkedSet) {
		DeclarationData declaration = declarationDataDao.get(declarationDataId);
		// Удалять декларацию можно только если она не принята
		if (declaration.isAccepted()) {
			throw new AccessDeniedException("Декларация принята");
		}
        // Нельзя удалить декларацию в закрытом периоде
        if (!reportPeriodService.isActivePeriod(declaration.getReportPeriodId(), declaration.getDepartmentId())) {
            throw new AccessDeniedException("Период закрыт");
        }
		// Удалять могут только контолёр текущего уровня и контролёр УНП
		checkRolesForReading(userInfo, declaration.getDepartmentId(),
				declaration.getReportPeriodId(), checkedSet);
	}

	private void canRefresh(TAUserInfo userInfo, long declarationDataId, Set<String> checkedSet) {
		DeclarationData declaration = declarationDataDao.get(declarationDataId);
		// Обновлять декларацию можно только если она не принята
		if (declaration.isAccepted()) {
			throw new AccessDeniedException("Декларация принята");
		}

        // Нельзя обновить декларацию в закрытом периоде
        if (!reportPeriodService.isActivePeriod(declaration.getReportPeriodId(), declaration.getDepartmentId())) {
            throw new AccessDeniedException("Период закрыт");
        }
        // Обновлять декларацию могут только контолёр текущего уровня и
		// контролёр УНП
		checkRolesForReading(userInfo, declaration.getDepartmentId(),
                declaration.getReportPeriodId(), checkedSet);
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
    public void checkEvents(TAUserInfo userInfo,
                            Integer declarationTemplateId, Integer departmentId,
                            Integer reportPeriodId, FormDataEvent scriptEvent) {
        checkEvents(userInfo, declarationTemplateId, departmentId, reportPeriodId, scriptEvent, null);
    }

    private void checkEvents(TAUserInfo userInfo,
                            Integer declarationTemplateId, Integer departmentId,
                            Integer reportPeriodId, FormDataEvent scriptEvent, Set<String> checkedSet) {
        switch (scriptEvent) {
            case CREATE:
                canCreate(userInfo, declarationTemplateId, departmentId, reportPeriodId, checkedSet);
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
	public Set<FormDataEvent> getPermittedEvents(TAUserInfo userInfo,
			Integer declarationTemplateId, Integer departmentId,
			Integer reportPeriodId) {
		Set<FormDataEvent> result = new HashSet<FormDataEvent>();
        Set<String> checkedSet = new HashSet<String>();
		for (FormDataEvent scriptEvent : FormDataEvent.values()) {
			try{
				checkEvents(userInfo, declarationTemplateId, departmentId, reportPeriodId, scriptEvent, checkedSet);
				result.add(scriptEvent);
			} catch (Exception e) {
				// Nothink
			}
		}
		return result;
	}
}
