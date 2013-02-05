package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.DeclarationTemplateDao;
import com.aplana.sbrf.taxaccounting.dao.ObjectLockDao;
import com.aplana.sbrf.taxaccounting.exception.AccessDeniedException;
import com.aplana.sbrf.taxaccounting.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.DeclarationTemplate;
import com.aplana.sbrf.taxaccounting.model.FormTemplate;
import com.aplana.sbrf.taxaccounting.model.ObjectLock;
import com.aplana.sbrf.taxaccounting.service.DeclarationTemplateService;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.util.List;

/**
 * Сервис для работы с шаблонами деклараций
 * @author Eugene Stetsenko
 */
@Service
@Transactional
public class DeclarationTemplateServiceImpl implements DeclarationTemplateService {

	@Autowired
	DeclarationTemplateDao declarationTemplateDao;

	@Autowired
	private ObjectLockDao lockDao;

	@Override
	public List<DeclarationTemplate> listAll() {
		return declarationTemplateDao.listAll();
	}

	@Override
	public DeclarationTemplate get(int declarationTemplateId) {
		return declarationTemplateDao.get(declarationTemplateId);
	}

	@Override
	public int save(DeclarationTemplate declarationTemplate) {
		return declarationTemplateDao.save(declarationTemplate);
	}

	@Override
	public void setJrxml(int declarationTemplateId, String jrxml) {
		ByteArrayOutputStream  xlsReport = new ByteArrayOutputStream();
		JasperDesign jasperDesign = null;
		try {
			jasperDesign = JRXmlLoader.load(new ByteArrayInputStream(jrxml.getBytes()));
			JasperCompileManager.compileReportToStream(jasperDesign, xlsReport);
		} catch (JRException e) {
			throw new ServiceException("Некорректный файл шаблона");
		}

		declarationTemplateDao.setJrxmlAndJasper(declarationTemplateId, jrxml, xlsReport.toByteArray());
	}

	@Override
	public String getJrxml(int declarationTemplateId) {
		return declarationTemplateDao.getJrxml(declarationTemplateId);
	}

	@Override
	public byte[] getJasper(int declarationTemplateId) {
		return declarationTemplateDao.getJasper(declarationTemplateId);
	}

	@Override
	public void checkLockedByAnotherUser(Integer declarationTemplateId, int userId){
		if (declarationTemplateId!=null){
			ObjectLock<Integer> objectLock = lockDao.getObjectLock(declarationTemplateId, DeclarationTemplate.class);
			if(objectLock != null && objectLock.getUserId() != userId){
				throw new AccessDeniedException("Шаблон декларации заблокирован другим пользователем");
			}
		}
	}

	@Override
	public boolean lock(int declarationTemplateId, int userId){
		ObjectLock<Integer> objectLock = lockDao.getObjectLock(declarationTemplateId, DeclarationTemplate.class);
		if(objectLock != null && objectLock.getUserId() != userId){
			return false;
		} else {
			lockDao.lockObject(declarationTemplateId, DeclarationTemplate.class ,userId);
			return true;
		}
	}

	@Override
	public boolean unlock(int declarationTemplateId, int userId){
		ObjectLock<Integer> objectLock = lockDao.getObjectLock(declarationTemplateId, DeclarationTemplate.class);
		if(objectLock != null && objectLock.getUserId() != userId){
			return false;
		} else {
			lockDao.unlockObject(declarationTemplateId, DeclarationTemplate.class, userId);
			return true;
		}
	}
}
