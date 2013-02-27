package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.DeclarationDao;
import com.aplana.sbrf.taxaccounting.dao.DeclarationTypeDao;
import com.aplana.sbrf.taxaccounting.dao.DepartmentDao;
import com.aplana.sbrf.taxaccounting.dao.DepartmentDeclarationTypeDao;
import com.aplana.sbrf.taxaccounting.dao.TAUserDao;
import com.aplana.sbrf.taxaccounting.log.Logger;
import com.aplana.sbrf.taxaccounting.model.Declaration;
import com.aplana.sbrf.taxaccounting.model.DeclarationFilterAvailableValues;
import com.aplana.sbrf.taxaccounting.model.DeclarationType;
import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.DepartmentDeclarationType;
import com.aplana.sbrf.taxaccounting.model.TARole;
import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.model.exception.AccessDeniedException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.service.DeclarationAccessService;
import com.aplana.sbrf.taxaccounting.service.DeclarationScriptingService;
import com.aplana.sbrf.taxaccounting.service.DeclarationService;
import com.aplana.sbrf.taxaccounting.service.DeclarationTemplateService;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRXmlDataSource;
import net.sf.jasperreports.engine.export.JRXlsExporter;
import net.sf.jasperreports.engine.export.JRXlsExporterParameter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Сервис для работы с декларациями
 * @author Eugene Stetsenko
 * @author dsultanbekov
 */
@Service
public class DeclarationServiceImpl implements DeclarationService {

	private Log logger = LogFactory.getLog(getClass());

	@Autowired
	private DeclarationDao declarationDao;

	@Autowired
	private DeclarationAccessService declarationAccessService ;
	
	@Autowired
	private DeclarationScriptingService declarationScriptingService;

	@Autowired
	private DeclarationTemplateService declarationTemplateService;
	
	@Autowired
	private DeclarationTypeDao declarationTypeDao;
	
	@Autowired
	private DepartmentDeclarationTypeDao departmentDeclarationTypeDao;
	
	@Autowired
	private DepartmentDao departmentDao;
	
	@Autowired
	private TAUserDao userDao;

	@Override
	public long createDeclaration(Logger logger, int declarationTemplateId, int departmentId, int userId, int reportPeriodId) {
		if (declarationAccessService.canCreate(userId, declarationTemplateId, departmentId, reportPeriodId)) {
			Declaration newDeclaration = new Declaration();
			newDeclaration.setDepartmentId(departmentId);
			newDeclaration.setReportPeriodId(reportPeriodId);
			newDeclaration.setAccepted(false);
			newDeclaration.setDeclarationTemplateId(declarationTemplateId);
			
			long declarationId = declarationDao.saveNew(newDeclaration);
			
			this.logger.debug("New declaration saved, id = " + declarationId);
			String xml = declarationScriptingService.create(logger, departmentId, declarationTemplateId, reportPeriodId);
			declarationDao.setXmlData(declarationId, xml);
			return declarationId;
		} else {
			throw new AccessDeniedException("Недостаточно прав для создания декларации с указанными параметрами");
		}
	}

	@Override
	public Declaration get(long declarationId, int userId) {
		if (declarationAccessService.canRead(userId, declarationId)) {
			Declaration declaration = declarationDao.get(declarationId);
			return declaration;
		} else {
			throw new AccessDeniedException("Недостаточно прав на просмотр данных декларации");
		}
	}

	@Override
	@Transactional
	public void delete(long declarationId, int userId) {
		if (declarationAccessService.canDelete(userId, declarationId)) {
			declarationDao.delete(declarationId);
		} else {
			throw new AccessDeniedException("Недостаточно прав на удаление декларации");
		}
	}

	@Override
	public void setAccepted(long declarationId, boolean accepted, int userId) {
		if (accepted) {
			if (!declarationAccessService.canAccept(userId, declarationId)) {
				throw new AccessDeniedException("Невозможно принять декларацию");
			}
		} else {
			if (!declarationAccessService.canReject(userId, declarationId)) {
				throw new AccessDeniedException("Невозможно отменить принятие декларации");
			}
		}
		declarationDao.setAccepted(declarationId, accepted);
	}

	@Override
	public String getXmlData(long declarationId, int userId) {
		if (declarationAccessService.canDownloadXml(userId, declarationId)) {
			String xmlData = declarationDao.getXmlData(declarationId);
			return xmlData;
		} else {
			throw new AccessDeniedException("Невозможно получить xml");
		}
	}

	@Override
	public byte[] getXlsxData(long declarationId, int userId) {
		if (declarationAccessService.canRead(userId, declarationId)) {
			Declaration declaration = declarationDao.get(declarationId);
			byte[] jasperTemplate = declarationTemplateService.getJasper(declaration.getDeclarationTemplateId());
			String xmlData = declarationDao.getXmlData(declarationId);
			JasperPrint print = null;
			try {
				JRXmlDataSource dataSource = new JRXmlDataSource(new ByteArrayInputStream(xmlData.getBytes()));
				print = JasperFillManager.fillReport(new ByteArrayInputStream(jasperTemplate), new HashMap<String, Object>(), dataSource);
			} catch (JRException e) {
				throw new ServiceException("Невозможно заполнить отчет");
			}
			JRXlsExporter exporter = new JRXlsExporter();
			ByteArrayOutputStream xls = new ByteArrayOutputStream();
			exporter.setParameter(JRXlsExporterParameter.JASPER_PRINT, print);
			exporter.setParameter(JRXlsExporterParameter.OUTPUT_STREAM, xls);
			exporter.setParameter(JRXlsExporterParameter.IS_ONE_PAGE_PER_SHEET, Boolean.TRUE);
			exporter.setParameter(JRXlsExporterParameter.IS_DETECT_CELL_TYPE, Boolean.TRUE);
			exporter.setParameter(JRXlsExporterParameter.IS_WHITE_PAGE_BACKGROUND, Boolean.FALSE);
			exporter.setParameter(JRXlsExporterParameter.IS_REMOVE_EMPTY_SPACE_BETWEEN_ROWS, Boolean.TRUE);
			try {
				exporter.exportReport();
			} catch (JRException e) {
				throw new ServiceException("Невозможно экспортировать отчет");
			}
			return xls.toByteArray();
		} else {
			throw new AccessDeniedException("Невозможно получить xlsx, так как у пользователя нет прав на просмотр декларации");
		}
	}

	@Override
	public void refreshDeclaration(Logger logger, long declarationId, int userId) {
		throw new UnsupportedOperationException("not implemented");
	}
}
