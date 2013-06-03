package com.aplana.sbrf.taxaccounting.service.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.aplana.sbrf.taxaccounting.dao.ReportPeriodDao;
import com.aplana.sbrf.taxaccounting.model.DepartmentType;
import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.aplana.sbrf.taxaccounting.dao.DeclarationDataDao;
import com.aplana.sbrf.taxaccounting.dao.DeclarationTemplateDao;
import com.aplana.sbrf.taxaccounting.dao.DepartmentDao;
import com.aplana.sbrf.taxaccounting.dao.TAUserDao;
import com.aplana.sbrf.taxaccounting.model.DeclarationData;
import com.aplana.sbrf.taxaccounting.model.DeclarationTemplate;
import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.DepartmentDeclarationType;
import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
import com.aplana.sbrf.taxaccounting.model.TARole;
import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.model.exception.AccessDeniedException;
import com.aplana.sbrf.taxaccounting.service.DeclarationDataAccessService;

/**
 * Реализация сервиса для проверки прав на доступ к декларациям
 * 
 * @author dsultanbekov
 */
@Service
public class DeclarationDataAccessServiceImpl implements
		DeclarationDataAccessService {
	@Autowired
	private TAUserDao userDao;

	@Autowired
	private DeclarationTemplateDao declarationTemplateDao;

	@Autowired
	private DepartmentDao departmentDao;

	@Autowired
	private DeclarationDataDao declarationDataDao;

	@Autowired
	private ReportPeriodDao reportPeriodDao;

	/**
	 * В сущности эта функция проверяет наличие прав на просмотр декларации,
	 * логика вынесена в отдельный метод, так как используется в нескольких
	 * местах данного сервиса
	 * 
	 * @param userId
	 *            идентификатор пользователя
	 * @param declarationDepartmentId
	 *            идентификатор подразделения, к которому относится декларация
	 * @param reportPeriodId
	 *            идентификатор отчетного периода
	 * @return true - права есть, false - прав нет
	 */
	private void checkRolesForReading(int userId,
			int declarationDepartmentId, int reportPeriodId) {
		TAUser user = userDao.getUser(userId);
		Department declarationDepartment = departmentDao
				.getDepartment(declarationDepartmentId);
		ReportPeriod reportPeriod = reportPeriodDao.get(reportPeriodId);
		checkRolesForReading(user, declarationDepartment, reportPeriod);
	}

	/**
	 * Перегруженный вариант {@link #checkRolesForReading(int, int, int)},
	 * принимающий объекты вместо идентификаторов
	 * 
	 * В сущности функция проверка проверяет наличие прав на просмотр
	 * декларации, логика вынесена в отдельный метод, так как используется в
	 * нескольких местах данного сервиса
	 * 
	 * @param user
	 *            пользователь системы
	 * @param declarationDepartment
	 *            подразделение, к которому относится декларация
	 * @param reportPeriod
	 *            отчетный период
	 * @return true - права есть, false - прав нет
	 */
	private void checkRolesForReading(TAUser user,
			Department declarationDepartment, ReportPeriod reportPeriod) {
		// Нельзя работать с декларациями в отчетном периоде вида
		// "ввод остатков"
		if (reportPeriod.isBalancePeriod()) {
			throw new AccessDeniedException("Декларациями в отчетном периоде вида 'ввод остатков'");
		}

		// Контролёр УНП может просматривать все декларации
		if (user.hasRole(TARole.ROLE_CONTROL_UNP)) {
			return;
		}

		// Обычный контролёр может просматривать декларации только в своём
		// обособленном подразделении
		if (user.hasRole(TARole.ROLE_CONTROL)
				&& user.getDepartmentId() == declarationDepartment.getId()
				&& !DepartmentType.ROOT_BANK.equals(declarationDepartment
						.getType())) {
			return;
		}
		throw new AccessDeniedException("Нет прав на доступ к декларации");
	}

	private void canRead(int userId, long declarationDataId) {
		DeclarationData declaration = declarationDataDao.get(declarationDataId);
		// Просматривать декларацию может только контролёр УНП и контролёр
		// текущего уровня для обособленных подразделений
		checkRolesForReading(userId, declaration.getDepartmentId(),
				declaration.getReportPeriodId());
	}

	private void canCreate(int userId, int declarationTemplateId,
			int departmentId, int reportPeriodId) {
		// Для начала проверяем, что в данном подразделении вообще можно
		// работать с декларациями данного вида
		DeclarationTemplate declarationTemplate = declarationTemplateDao
				.get(declarationTemplateId);
		int declarationTypeId = declarationTemplate.getDeclarationType()
				.getId();

		Department department = departmentDao.getDepartment(departmentId);
		List<DepartmentDeclarationType> ddts = department
				.getDepartmentDeclarationTypes();
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
		checkRolesForReading(userId, departmentId, reportPeriodId);
	}

	private void canAccept(int userId, long declarationDataId) {
		DeclarationData declaration = declarationDataDao.get(declarationDataId);
		// Принять декларацию можно только если она еще не принята
		if (declaration.isAccepted()) {
			throw new AccessDeniedException("Декларация не должна быть принята");
		}
		// Принять декларацию могут только контолёр текущего уровня
		// обособленного подразделения и контролёр УНП
		checkRolesForReading(userId, declaration.getDepartmentId(),
				declaration.getReportPeriodId());
	}

	private void canReject(int userId, long declarationDataId) {
		DeclarationData declaration = declarationDataDao.get(declarationDataId);
		// Отменить принятие декларации можно только если она принята
		if (!declaration.isAccepted()) {
			throw new AccessDeniedException("Декларация должна быть принята");
		}
		// Отменить принятие декларацию могут только контолёр текущего уровня и
		// контролёр УНП
		checkRolesForReading(userId, declaration.getDepartmentId(),
				declaration.getReportPeriodId());
	}

	private void canDelete(int userId, long declarationDataId) {
		DeclarationData declaration = declarationDataDao.get(declarationDataId);
		// Удалять декларацию можно только если она не принята
		if (declaration.isAccepted()) {
			throw new AccessDeniedException("Декларация не должна быть принята");
		}
		// Удалять могут только контолёр текущего уровня и контролёр УНП
		checkRolesForReading(userId, declaration.getDepartmentId(),
				declaration.getReportPeriodId());
	}

	private void canRefresh(int userId, long declarationDataId) {
		DeclarationData declaration = declarationDataDao.get(declarationDataId);
		// Обновлять декларацию можно только если она не принята
		if (declaration.isAccepted()) {
			throw new AccessDeniedException("Декларация не должна быть принята");
		}
		// Обновлять декларацию могут только контолёр текущего уровня и
		// контролёр УНП
		checkRolesForReading(userId, declaration.getDepartmentId(),
				declaration.getReportPeriodId());
	}

	private void canDownloadXml(int userId, long declarationDataId) {
		DeclarationData declaration = declarationDataDao.get(declarationDataId);
		// Скачивать файл в формате законодателя можно только для принятых
		// деклараций
		if (!declaration.isAccepted()) {
			throw new AccessDeniedException("Декларация должна быть принята");
		}
		// Скачивать файл в формате законодателя могут только контолёр текущего
		// уровня и контролёр УНП
		checkRolesForReading(userId, declaration.getDepartmentId(),
				declaration.getReportPeriodId());
	}

	@Override
	public void checkEvents(Integer userId, Long declarationDataId,
			FormDataEvent... scriptEvents) {
		for (FormDataEvent scriptEvent : scriptEvents) {
			switch (scriptEvent) {
			case MOVE_CREATED_TO_ACCEPTED:
				canAccept(userId, declarationDataId);
				break;
			case MOVE_ACCEPTED_TO_CREATED:
				canReject(userId, declarationDataId);
				break;
			case GET_LEVEL0:
				canRead(userId, declarationDataId);
				break;
			case GET_LEVEL1:
				canDownloadXml(userId, declarationDataId);
				break;
			case DELETE:
				canDelete(userId, declarationDataId);
				break;
			case CALCULATE:
				canRefresh(userId, declarationDataId);
				break;
			default:
				throw new AccessDeniedException(
						"Операция не предусмотрена в системе");
			}
		}
	}

	@Override
	public void checkEvents(Integer userId,
			Integer declarationTemplateId, Integer departmentId,
			Integer reportPeriodId, FormDataEvent... scriptEvents) {
		for (FormDataEvent scriptEvent : scriptEvents) {
			switch (scriptEvent) {
			case CREATE:
				canCreate(userId, declarationTemplateId, departmentId, reportPeriodId);
				break;
			default:
				throw new AccessDeniedException(
						"Операция не предусмотрена в системе");
			}
		}
	}

	@Override
	public Set<FormDataEvent> getPermittedEvents(Integer userId,
			Long declarationDataId) {
		Set<FormDataEvent> result = new HashSet<FormDataEvent>();
		for (FormDataEvent scriptEvent : FormDataEvent.values()) {
			try{
				checkEvents(userId, declarationDataId, new FormDataEvent[]{scriptEvent});
				result.add(scriptEvent);
			} catch (Exception e) {
				// Nothink
			}
		}
		return result;
	}

	@Override
	public Set<FormDataEvent> getPermittedEvents(Integer userId,
			Integer declarationTemplateId, Integer departmentId,
			Integer reportPeriodId) {
		Set<FormDataEvent> result = new HashSet<FormDataEvent>();
		for (FormDataEvent scriptEvent : FormDataEvent.values()) {
			try{
				checkEvents(userId, declarationTemplateId, departmentId, reportPeriodId, new FormDataEvent[]{scriptEvent});
				result.add(scriptEvent);
			} catch (Exception e) {
				// Nothink
			}
		}
		return result;
	}

}
