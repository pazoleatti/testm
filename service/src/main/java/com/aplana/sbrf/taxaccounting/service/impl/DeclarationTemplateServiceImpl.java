package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.DeclarationTemplateDao;
import com.aplana.sbrf.taxaccounting.dao.ObjectLockDao;
import com.aplana.sbrf.taxaccounting.model.BlobData;
import com.aplana.sbrf.taxaccounting.model.DeclarationTemplate;
import com.aplana.sbrf.taxaccounting.model.ObjectLock;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.exception.AccessDeniedException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.service.BlobDataService;
import com.aplana.sbrf.taxaccounting.service.DeclarationTemplateService;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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

	private static final Log logger = LogFactory.getLog(DeclarationTemplateServiceImpl.class);
    private final static String ENCODING = "UTF-8";

	@Autowired
	DeclarationTemplateDao declarationTemplateDao;

	@Autowired
	private ObjectLockDao lockDao;

    @Autowired
    BlobDataService blobDataService;

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
	public int getActiveDeclarationTemplateId(int declarationTypeId) {
		return declarationTemplateDao.getActiveDeclarationTemplateId(declarationTypeId);
	}

	@Override
	public void setJrxml(int declarationTemplateId, String jrxml) {
		ByteArrayOutputStream  compiledReport = new ByteArrayOutputStream();
		try {
			JasperDesign jasperDesign = JRXmlLoader.load(new ByteArrayInputStream(jrxml.getBytes(ENCODING)));
			JasperCompileManager.compileReportToStream(jasperDesign, compiledReport);
		} catch (JRException e) {
			logger.error(e.getMessage(), e);
			throw new ServiceException("Произошли ошибки во время формирования отчета");
		} catch (UnsupportedEncodingException e2) {
			logger.error(e2.getMessage(), e2);
			throw new ServiceException("Шаблон отчета имеет неправильную кодировку");
		}

        DeclarationTemplate declarationTemplate = this.get(declarationTemplateId);

        String jrxmBlobId = blobDataService.create(
                new ByteArrayInputStream(jrxml.getBytes()),
                declarationTemplate.getDeclarationType().getName() +"_jrxml");
        String jasperBlobId = blobDataService.create(
                new ByteArrayInputStream(compiledReport.toByteArray()),
                declarationTemplate.getDeclarationType().getName() + "_jasper");

        declarationTemplateDao.setJrxmlAndJasper(declarationTemplateId, jrxmBlobId, jasperBlobId);
	}

	@Override
	public String getJrxml(int declarationTemplateId) {
        BlobData jrxmlBlobData = blobDataService.get(this.get(declarationTemplateId).getJrxmlBlobId());
        try {
            StringWriter writer = new StringWriter();
            IOUtils.copy(jrxmlBlobData.getInputStream(), writer, ENCODING);
            return writer.toString();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            throw new ServiceException("Не удалось получить jrxml-шаблон декларации");
        }
	}

	@Override
	public InputStream getJasper(int declarationTemplateId) {
        BlobData jasperBlobData = blobDataService.get(this.get(declarationTemplateId).getJasperBlobId());
        return jasperBlobData.getInputStream();
	}

	@Override
	public void checkLockedByAnotherUser(Integer declarationTemplateId, TAUserInfo userInfo){
		if (declarationTemplateId!=null){
			ObjectLock<Integer> objectLock = lockDao.getObjectLock(declarationTemplateId, DeclarationTemplate.class);
			if(objectLock != null && objectLock.getUserId() != userInfo.getUser().getId()){
				throw new AccessDeniedException("Шаблон декларации заблокирован другим пользователем");
			}
		}
	}

	@Override
	public boolean lock(int declarationTemplateId, TAUserInfo userInfo){
		ObjectLock<Integer> objectLock = lockDao.getObjectLock(declarationTemplateId, DeclarationTemplate.class);
		if(objectLock != null && objectLock.getUserId() != userInfo.getUser().getId()){
			return false;
		} else {
			lockDao.lockObject(declarationTemplateId, DeclarationTemplate.class ,userInfo.getUser().getId());
			return true;
		}
	}

	@Override
	public boolean unlock(int declarationTemplateId, TAUserInfo userInfo){
		ObjectLock<Integer> objectLock = lockDao.getObjectLock(declarationTemplateId, DeclarationTemplate.class);
		if(objectLock != null && objectLock.getUserId() != userInfo.getUser().getId()){
			return false;
		} else {
			lockDao.unlockObject(declarationTemplateId, DeclarationTemplate.class, userInfo.getUser().getId());
			return true;
		}
	}
}
