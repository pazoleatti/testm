package com.aplana.sbrf.taxaccounting.service.impl;

import java.util.List;

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
import com.aplana.sbrf.taxaccounting.model.TARole;
import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.service.DeclarationDataAccessService;

/**
 * Реализация сервиса для проверки прав на доступ к декларациям
 * @author dsultanbekov
 */
@Service
public class DeclarationDataAccessServiceImpl implements DeclarationDataAccessService {
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
	 * В сущности эта функция проверяет наличие прав на просмотр декларации, логика вынесена в отдельный метод,
	 * так как используется в нескольких местах данного сервиса
	 * 
	 * @param userId идентификатор пользователя
	 * @param declarationDepartmentId идентификатор подразделения, к которому относится декларация
	 * @param reportPeriodId идентификатор отчетного периода
	 * @return true - права есть, false - прав нет
	 */
	private boolean checkRolesForReading(int userId, int declarationDepartmentId, int reportPeriodId) {
		TAUser user = userDao.getUser(userId);
		Department declarationDepartment = departmentDao.getDepartment(declarationDepartmentId);
		ReportPeriod reportPeriod = reportPeriodDao.get(reportPeriodId);
		return checkRolesForReading(user, declarationDepartment, reportPeriod);
	}
	
	/**
	 * Перегруженный вариант {@link #checkRolesForReading(int, int, int)}, принимающий
	 * объекты вместо идентификаторов
	 * 
	 * В сущности функция проверка проверяет наличие прав на просмотр декларации, логика вынесена в отдельный метод,
	 * так как используется в нескольких местах данного сервиса
	 * @param user пользователь системы
	 * @param declarationDepartment подразделение, к которому относится декларация
	 * @param reportPeriod отчетный период
	 * @return true - права есть, false - прав нет
	 */
	private boolean checkRolesForReading(TAUser user, Department declarationDepartment, ReportPeriod reportPeriod) {
		// Нельзя работать с декларациями в отчетном периоде вида "ввод остатков"
		if (reportPeriod.isBalancePeriod()) {
			return false;
		}

		// Контролёр УНП может просматривать все декларации
		if (user.hasRole(TARole.ROLE_CONTROL_UNP)) {
			return true;
		}

		// Обычный контролёр может просматривать декларации только в своём обособленном подразделении
		if (user.hasRole(TARole.ROLE_CONTROL) && user.getDepartmentId() == declarationDepartment.getId() &&
				!DepartmentType.ROOT_BANK.equals(declarationDepartment.getType())) {
			return true;
		}
		return false;		
	}
	
	@Override
	public boolean canRead(int userId, long declarationDataId) {
		DeclarationData declaration = declarationDataDao.get(declarationDataId);

		// Если отчетный период для ввода остатков то кидаем исключение
		if (reportPeriodDao.get(declaration.getReportPeriodId()).isBalancePeriod()) {
			return false;
		}

		// Просматривать декларацию может только контролёр УНП и контролёр текущего уровня для обособленных подразделений
		return checkRolesForReading(userId, declaration.getDepartmentId(), declaration.getReportPeriodId());
	}

	@Override
	public boolean canCreate(int userId, int declarationTemplateId, int departmentId, int reportPeriodId) {
		// Для начала проверяем, что в данном подразделении вообще можно работать с декларациями данного вида
		DeclarationTemplate declarationTemplate = declarationTemplateDao.get(declarationTemplateId);
		int declarationTypeId = declarationTemplate.getDeclarationType().getId();
		
		Department department = departmentDao.getDepartment(departmentId);
		List<DepartmentDeclarationType> ddts = department.getDepartmentDeclarationTypes();
		boolean found = false;
		for (DepartmentDeclarationType ddt: ddts) {
			if (ddt.getDeclarationTypeId() == declarationTypeId) {
				found = true;
				break;
			}
		}
		if (!found) {
			return false;
		}
		// Создавать декларацию могут только контролёры УНП и контролёры текущего уровня обособленного подразделения
		return checkRolesForReading(userId, departmentId, reportPeriodId);
	}

	@Override
	public boolean canAccept(int userId, long declarationDataId) {
		DeclarationData declaration = declarationDataDao.get(declarationDataId);
		// Принять декларацию можно только если она еще не принята
		if (declaration.isAccepted()) {
			return false;
		}
		// Принять декларацию могут только контолёр текущего уровня обособленного подразделения и контролёр УНП
		return checkRolesForReading(userId, declaration.getDepartmentId(), declaration.getReportPeriodId());
	}

	@Override
	public boolean canReject(int userId, long declarationDataId) {
		DeclarationData declaration = declarationDataDao.get(declarationDataId);
		// Отменить принятие декларации можно только если она принята
		if (!declaration.isAccepted()) {
			return false;
		}
		// Отменить принятие декларацию могут только контолёр текущего уровня и контролёр УНП
		return checkRolesForReading(userId, declaration.getDepartmentId(), declaration.getReportPeriodId());
	}

	@Override
	public boolean canDelete(int userId, long declarationDataId) {
		DeclarationData declaration = declarationDataDao.get(declarationDataId);
		// Удалять декларацию можно только если она не принята 
		if (declaration.isAccepted()) {
			return false;
		}
		// Удалять могут только контолёр текущего уровня и контролёр УНП
		return checkRolesForReading(userId, declaration.getDepartmentId(), declaration.getReportPeriodId());
	}

	@Override
	public boolean canRefresh(int userId, long declarationDataId) {
		DeclarationData declaration = declarationDataDao.get(declarationDataId);
		// Обновлять декларацию можно только если она не принята 
		if (declaration.isAccepted()) {
			return false;
		}
		// Обновлять декларацию могут только контолёр текущего уровня и контролёр УНП
		return checkRolesForReading(userId, declaration.getDepartmentId(), declaration.getReportPeriodId());
	}

	@Override
	public boolean canDownloadXml(int userId, long declarationDataId) {
		DeclarationData declaration = declarationDataDao.get(declarationDataId);
		// Скачивать файл в формате законодателя можно только для принятых деклараций
		if (!declaration.isAccepted()) {
			return false;
		}
		// Скачивать файл в формате законодателя могут только контолёр текущего уровня и контролёр УНП
		return checkRolesForReading(userId, declaration.getDepartmentId(), declaration.getReportPeriodId());
	}
}
