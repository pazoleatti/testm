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
	 * @return true - права есть, false - прав нет
	 */
	private void checkRolesForReading(TAUserInfo userInfo,
			int declarationDepartmentId, int reportPeriodId) {
		Department declarationDepartment = departmentService.getDepartment(declarationDepartmentId);
		checkRolesForReading(userInfo, declarationDepartment, reportPeriodId);
	}

	/**
	 * Перегруженный вариант {@link #checkRolesForReading(TAUserInfo, int, int)},
	 * принимающий объекты вместо идентификаторов
	 * 
	 * В сущности функция проверка проверяет наличие прав на просмотр
	 * декларации, логика вынесена в отдельный метод, так как используется в
	 * нескольких местах данного сервиса
	 * 
	 * @param userInfo
	 *            информация о пользователе
	 *
	 * @param declarationDepartment
	 *            подразделение, к которому относится декларация
	 * @param reportPeriodId
	 *            код отчетного периода
	 */
	private void checkRolesForReading(TAUserInfo userInfo, Department declarationDepartment, int reportPeriodId) {
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
					asList(reportPeriod.getTaxPeriod().getTaxType())).contains(declarationDepartment.getId())) {
				return;
			}
        }

        // Прочие
        throw new AccessDeniedException("Нет прав на доступ к декларации");
	}

	private void canRead(TAUserInfo userInfo, long declarationDataId) {
		DeclarationData declaration = declarationDataDao.get(declarationDataId);
		// Просматривать декларацию может только контролёр УНП и контролёр
		// текущего уровня для обособленных подразделений
		checkRolesForReading(userInfo, declaration.getDepartmentId(),
				declaration.getReportPeriodId());
	}

	private void canCreate(TAUserInfo userInfo, int declarationTemplateId,
			int departmentId, int reportPeriodId) {
		// Для начала проверяем, что в данном подразделении вообще можно
		// работать с декларациями данного вида
		DeclarationTemplate declarationTemplate = declarationTemplateDao
				.get(declarationTemplateId);
		int declarationTypeId = declarationTemplate.getType()
				.getId();

		List<DepartmentDeclarationType> ddts = sourceService.getDDTByDepartment(departmentId, declarationTemplate.getType().getTaxType());
		boolean found = false;
		for (DepartmentDeclarationType ddt : ddts) {
			if (ddt.getDeclarationTypeId() == declarationTypeId) {
				found = true;
				break;
			}
		}
		if (!found) {
			throw new AccessDeniedException("В данном подразделении нельзя работать с декларациями данного вида");
		}
		// Создавать декларацию могут только контролёры УНП и контролёры
		// текущего уровня обособленного подразделения
		checkRolesForReading(userInfo, departmentId, reportPeriodId);
	}

	private void canAccept(TAUserInfo userInfo, long declarationDataId) {
		DeclarationData declaration = declarationDataDao.get(declarationDataId);
		// Принять декларацию можно только если она еще не принята
		if (declaration.isAccepted()) {
			throw new AccessDeniedException("Декларация не должна быть принята");
		}
		// Принять декларацию могут только контолёр текущего уровня
		// обособленного подразделения и контролёр УНП
		checkRolesForReading(userInfo, declaration.getDepartmentId(),
				declaration.getReportPeriodId());
	}

	private void canReject(TAUserInfo userInfo, long declarationDataId) {
		DeclarationData declaration = declarationDataDao.get(declarationDataId);
		// Отменить принятие декларации можно только если она принята
		if (!declaration.isAccepted()) {
			throw new AccessDeniedException("Декларация должна быть принята");
		}
		// Отменить принятие декларацию могут только контолёр текущего уровня и
		// контролёр УНП
		checkRolesForReading(userInfo, declaration.getDepartmentId(),
				declaration.getReportPeriodId());
	}

	private void canDelete(TAUserInfo userInfo, long declarationDataId) {
		DeclarationData declaration = declarationDataDao.get(declarationDataId);
		// Удалять декларацию можно только если она не принята
		if (declaration.isAccepted()) {
			throw new AccessDeniedException("Декларация не должна быть принята");
		}
		// Удалять могут только контолёр текущего уровня и контролёр УНП
		checkRolesForReading(userInfo, declaration.getDepartmentId(),
				declaration.getReportPeriodId());
	}

	private void canRefresh(TAUserInfo userInfo, long declarationDataId) {
		DeclarationData declaration = declarationDataDao.get(declarationDataId);
		// Обновлять декларацию можно только если она не принята
		if (declaration.isAccepted()) {
			throw new AccessDeniedException("Декларация не должна быть принята");
		}

        // Обновлять декларацию могут только контолёр текущего уровня и
		// контролёр УНП
		checkRolesForReading(userInfo, declaration.getDepartmentId(),
                declaration.getReportPeriodId());
	}

	@Override
	public void checkEvents(TAUserInfo userInfo, Long declarationDataId, FormDataEvent... scriptEvents) {
        // TODO Оптимизировать http://jira.aplana.com/browse/SBRFACCTAX-5412
		for (FormDataEvent scriptEvent : scriptEvents) {
			switch (scriptEvent) {
			case MOVE_CREATED_TO_ACCEPTED:
				canAccept(userInfo, declarationDataId);
				break;
			case MOVE_ACCEPTED_TO_CREATED:
				canReject(userInfo, declarationDataId);
				break;
			case GET_LEVEL0:
            case GET_LEVEL1:
				canRead(userInfo, declarationDataId);
				break;
			case DELETE:
				canDelete(userInfo, declarationDataId);
				break;
			case CALCULATE:
				canRefresh(userInfo, declarationDataId);
				break;
			default:
				throw new AccessDeniedException("Операция не предусмотрена в системе");
			}
		}
	}

	@Override
	public void checkEvents(TAUserInfo userInfo,
			Integer declarationTemplateId, Integer departmentId,
			Integer reportPeriodId, FormDataEvent... scriptEvents) {
		for (FormDataEvent scriptEvent : scriptEvents) {
			switch (scriptEvent) {
			case CREATE:
				canCreate(userInfo, declarationTemplateId, departmentId, reportPeriodId);
				break;
			default:
				throw new AccessDeniedException("Операция не предусмотрена в системе");
			}
		}
	}

	@Override
	public Set<FormDataEvent> getPermittedEvents(TAUserInfo userInfo,
			Long declarationDataId) {
		Set<FormDataEvent> result = new HashSet<FormDataEvent>();
		for (FormDataEvent scriptEvent : FormDataEvent.values()) {
			try{
				checkEvents(userInfo, declarationDataId, new FormDataEvent[]{scriptEvent});
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
		for (FormDataEvent scriptEvent : FormDataEvent.values()) {
			try{
				checkEvents(userInfo, declarationTemplateId, departmentId, reportPeriodId, new FormDataEvent[]{scriptEvent});
				result.add(scriptEvent);
			} catch (Exception e) {
				// Nothink
			}
		}
		return result;
	}
}
