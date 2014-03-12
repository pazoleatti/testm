package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.DeclarationTemplateDao;
import com.aplana.sbrf.taxaccounting.dao.ObjectLockDao;
import com.aplana.sbrf.taxaccounting.model.*;
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
import java.util.*;

import static com.aplana.sbrf.taxaccounting.model.VersionedObjectStatus.FAKE;

/**
 * Сервис для работы с шаблонами деклараций
 * @author Eugene Stetsenko
 */
@Service
@Transactional
public class DeclarationTemplateServiceImpl implements DeclarationTemplateService {

	private static final Log logger = LogFactory.getLog(DeclarationTemplateServiceImpl.class);
    private final static String ENCODING = "UTF-8";
    private Calendar calendar = Calendar.getInstance();

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
	public int getActiveDeclarationTemplateId(int declarationTypeId, int reportPeriodId) {
		return declarationTemplateDao.getActiveDeclarationTemplateId(declarationTypeId, reportPeriodId);
	}

	@Override
	public void setJrxml(int declarationTemplateId, InputStream jrxmlIO) {
        DeclarationTemplate declarationTemplate = this.get(declarationTemplateId);

        String jrxmBlobId = blobDataService.create(
                jrxmlIO,
                declarationTemplate.getType().getName() +"_jrxml");

        declarationTemplateDao.setJrxml(declarationTemplateId, jrxmBlobId);
	}

	@Override
	public String getJrxml(int declarationTemplateId) {
        BlobData jrxmlBlobData = blobDataService.get(this.get(declarationTemplateId).getJrxmlBlobId());
        if (jrxmlBlobData == null)
            return "";
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
        ByteArrayOutputStream  compiledReport = new ByteArrayOutputStream();
        String jrxml = getJrxml(declarationTemplateId);
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

        return new ByteArrayInputStream(compiledReport.toByteArray());
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
    public String getDeclarationTemplateScript(int declarationTemplateId) {
        return declarationTemplateDao.getDeclarationTemplateScript(declarationTemplateId);
    }

    @Override
    public List<DeclarationTemplate> getByFilter(TemplateFilter filter) {
        List<DeclarationTemplate> templates = new ArrayList<DeclarationTemplate>();
        for (Integer id : declarationTemplateDao.getByFilter(filter)) {
            templates.add(declarationTemplateDao.get(id));
        }
        return templates;
    }

    @Override
    public List<DeclarationTemplate> getDecTemplateVersionsByStatus(int formTypeId, VersionedObjectStatus... status) {
        List<Integer> statusList = createStatusList(status);

        List<Integer> declarationTemplateIds =  declarationTemplateDao.getDeclarationTemplateVersions(formTypeId, 0, statusList, null, null);
        List<DeclarationTemplate> declarationTemplates = new ArrayList<DeclarationTemplate>();
        for (Integer id : declarationTemplateIds)
            declarationTemplates.add(declarationTemplateDao.get(id));
        return declarationTemplates;
    }

    @Override
    public List<IntersectionSegment> findFTVersionIntersections(int templateId, int typeId, Date actualBeginVersion, Date actualEndVersion) {
        return declarationTemplateDao.findFTVersionIntersections(typeId, templateId, actualBeginVersion, actualEndVersion);
    }

    @Override
    public int delete(DeclarationTemplate declarationTemplate) {
        switch (declarationTemplate.getStatus()){
            case FAKE:
                return declarationTemplateDao.delete(declarationTemplate.getId());
            default:
                declarationTemplate.setStatus(VersionedObjectStatus.DELETED);
                return declarationTemplateDao.save(declarationTemplate);
        }
    }

    @Override
    public DeclarationTemplate getNearestDTRight(int declarationTemplateId, VersionedObjectStatus... status) {
        List<Integer> statusList = createStatusList(status);
        DeclarationTemplate declarationTemplate = declarationTemplateDao.get(declarationTemplateId);

        int id = declarationTemplateDao.getNearestDTVersionIdRight(declarationTemplate.getType().getId(), declarationTemplate.getVersion());
        if (id == 0)
            return null;
        return declarationTemplateDao.get(id);
    }

    @Override
    public Date getDTEndDate(int declarationTemplateId) {
        if (declarationTemplateId == 0)
            return null;
        List<Integer> statusList = createStatusList(new VersionedObjectStatus[]{});
        DeclarationTemplate declarationTemplate = declarationTemplateDao.get(declarationTemplateId);

        return declarationTemplateDao.getDTVersionEndDate(declarationTemplateId, declarationTemplate.getType().getId(), declarationTemplate.getVersion());
    }

    @Override
    public int versionTemplateCount(int typeId, VersionedObjectStatus... status) {
        List<Integer> statusList = createStatusList(status);
        return declarationTemplateDao.versionTemplateCount(typeId, statusList);
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

    private List<Integer> createStatusList(VersionedObjectStatus[] status){
        List<Integer> statusList = new ArrayList<Integer>();
        if (status.length == 0){
            statusList.add(VersionedObjectStatus.NORMAL.getId());
            statusList.add(FAKE.getId());
            statusList.add(VersionedObjectStatus.DRAFT.getId());
        }else {
            for (VersionedObjectStatus objectStatus : status)
                statusList.add(objectStatus.getId());
        }

        return statusList;
    }

    private Date addCalendar(int fieldNumber, int numberDays, long actualDate){
        calendar.setTime(new Date(actualDate));
        calendar.add(fieldNumber, numberDays);
        Date time = calendar.getTime();
        calendar.clear();
        return time;
    }
}
