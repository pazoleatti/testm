package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.dao.DeclarationTemplateDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.AccessDeniedException;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.*;
import com.aplana.sbrf.taxaccounting.util.TransactionHelper;
import com.aplana.sbrf.taxaccounting.util.TransactionLogic;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
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
    private static final String JRXML_NOT_FOUND = "Не удалось получить jrxml-шаблон декларации!";
    private static final SimpleDateFormat SDF_DD_MM_YYYY = new SimpleDateFormat("dd.MM.yyyy");
    private static final String DEC_DATA_EXIST_IN_TASK =
            "%s в подразделении \"%s\" в периоде \"%s %d%s\", налоговый орган %s, КПП %s";

	@Autowired
	DeclarationTemplateDao declarationTemplateDao;

    @Autowired
    BlobDataService blobDataService;

    @Autowired
    TransactionHelper tx;
    @Autowired
    LockDataService lockDataService;
    @Autowired
    DepartmentService departmentService;
    @Autowired
    DeclarationDataService declarationDataService;
    @Autowired
    PeriodService periodService;
    @Autowired
    ReportService reportService;
    @Autowired
    DepartmentReportPeriodService departmentReportPeriodService;

    @Override
	public List<DeclarationTemplate> listAll() {
		return declarationTemplateDao.listAll();
	}

	@Override
	public DeclarationTemplate get(int declarationTemplateId) {
        try{
            return declarationTemplateDao.get(declarationTemplateId);
        } catch (DaoException e){
            throw new ServiceException("Ошибка получения шаблона декларации.", e);
        }
	}

	@Override
	public int save(DeclarationTemplate declarationTemplate) {
        if (declarationTemplate.getId() == null){
            return declarationTemplateDao.create(declarationTemplate);
        }
        DeclarationTemplate declarationTemplateBase = declarationTemplateDao.get(declarationTemplate.getId());
        int savedId = declarationTemplateDao.save(declarationTemplate);
        if (declarationTemplate.getXsdId() != null && !declarationTemplate.getXsdId().equals(declarationTemplateBase.getXsdId()))
            blobDataService.delete(declarationTemplateBase.getXsdId());
        if (declarationTemplate.getJrxmlBlobId() != null && !declarationTemplate.getJrxmlBlobId().equals(declarationTemplateBase.getJrxmlBlobId()))
            blobDataService.delete(declarationTemplateBase.getJrxmlBlobId());
        return savedId;
	}

    @Override
    public void update(List<DeclarationTemplate> declarationTemplates) {
        try {
            if (ArrayUtils.contains(declarationTemplateDao.update(declarationTemplates), 0))
                throw new ServiceException("Не все записи макета обновились.");
        } catch (DaoException e){
            throw new ServiceException("Ошибки при обновлении списка версий макета.",e);
        }
    }

    @Override
	public int getActiveDeclarationTemplateId(int declarationTypeId, int reportPeriodId) {
        try {
            return declarationTemplateDao.getActiveDeclarationTemplateId(declarationTypeId, reportPeriodId);
        } catch (DaoException e){
            throw new ServiceException(e.getMessage(), e);
        }

	}


	@Override
	public String getJrxml(int declarationTemplateId) {
        BlobData jrxmlBlobData = blobDataService.get(this.get(declarationTemplateId).getJrxmlBlobId());
        if (jrxmlBlobData == null) {
            return null;
        }
        try {
            //TODO: Лучше все через потоки делать
            StringWriter writer = new StringWriter();
            IOUtils.copy(jrxmlBlobData.getInputStream(), writer, ENCODING);
            return writer.toString();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            throw new ServiceException(JRXML_NOT_FOUND);
        }
	}

    @Override
	public InputStream getJasper(int declarationTemplateId) {
        ByteArrayOutputStream  compiledReport = new ByteArrayOutputStream();
        String jrxml = getJrxml(declarationTemplateId);
        if (jrxml == null) {
            throw new ServiceException(JRXML_NOT_FOUND);
        }
        try {
            JasperDesign jasperDesign = JRXmlLoader.load(new ByteArrayInputStream(jrxml.getBytes(ENCODING)));
            JasperCompileManager.compileReportToStream(jasperDesign, compiledReport);
        } catch (JRException e) {
            logger.error(e.getMessage(), e);
            throw new ServiceException("Произошли ошибки во время формирования отчета!");
        } catch (UnsupportedEncodingException e2) {
            logger.error(e2.getMessage(), e2);
            throw new ServiceException("Шаблон отчета имеет неправильную кодировку!");
        }
        return new ByteArrayInputStream(compiledReport.toByteArray());
	}

	@Override
	public void checkLockedByAnotherUser(Integer declarationTemplateId, TAUserInfo userInfo){
		if (declarationTemplateId!=null){
			LockData objectLock = lockDataService.getLock(LockData.LockObjects.DECLARATION_TEMPLATE.name() + "_" + declarationTemplateId);
			if (objectLock != null && objectLock.getUserId() != userInfo.getUser().getId()) {
				throw new AccessDeniedException("Шаблон декларации заблокирован другим пользователем");
			}
		}
	}

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public LockData getObjectLock(final Integer declarationTemplateId, final TAUserInfo userInfo) {
        return tx.executeInNewTransaction(new TransactionLogic<LockData>() {
            @Override
            public LockData execute() {
                return lockDataService.getLock(LockData.LockObjects.DECLARATION_TEMPLATE.name() + "_" + declarationTemplateId);
            }
        });
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
    public List<Integer> getDTVersionIdsByStatus(int formTypeId, VersionedObjectStatus... status) {
        List<Integer> statusList = createStatusList(status);
        return declarationTemplateDao.getDeclarationTemplateVersions(formTypeId, 0, statusList, null, null);
    }

    @Override
    public List<VersionSegment> findFTVersionIntersections(int templateId, int typeId, Date actualBeginVersion, Date actualEndVersion) {
        return declarationTemplateDao.findFTVersionIntersections(typeId, templateId, actualBeginVersion, actualEndVersion);
    }

    @Override
    public int delete(int declarationTemplateId) {
        return declarationTemplateDao.delete(declarationTemplateId);
    }

    @Override
    public void delete(Collection<Integer> templateIds) {
        declarationTemplateDao.delete(templateIds);
    }

    @Override
    public DeclarationTemplate getNearestDTRight(int declarationTemplateId, VersionedObjectStatus... status) {
        DeclarationTemplate declarationTemplate = declarationTemplateDao.get(declarationTemplateId);

        int id = declarationTemplateDao.getNearestDTVersionIdRight(declarationTemplate.getType().getId(), createStatusList(status),
                declarationTemplate.getVersion());
        if (id == 0)
            return null;
        return declarationTemplateDao.get(id);
    }

    @Override
    public Date getDTEndDate(int declarationTemplateId) {
        if (declarationTemplateId == 0)
            return null;
        DeclarationTemplate declarationTemplate = declarationTemplateDao.get(declarationTemplateId);

        return declarationTemplateDao.getDTVersionEndDate(declarationTemplate.getType().getId(), declarationTemplate.getVersion());
    }

    @Override
    public int versionTemplateCount(int typeId, VersionedObjectStatus... status) {
        List<Integer> statusList = createStatusList(status);
        return declarationTemplateDao.versionTemplateCount(typeId, statusList);
    }

    @Override
    public Map<Long, Integer> versionTemplateCountByFormType(Collection<Integer> formTypeIds) {
        Map<Long, Integer> integerMap = new HashMap<Long, Integer>();
        if (formTypeIds.isEmpty())
            return integerMap;
        List<Map<String, Object>> mapList = declarationTemplateDao.versionTemplateCountByType(formTypeIds);
        for (Map<String, Object> map : mapList){
            integerMap.put(((BigDecimal) map.get("type_id")).longValue(), ((BigDecimal)map.get("version_count")).intValue());
        }
        return integerMap;
    }

    @Override
    public int updateVersionStatus(VersionedObjectStatus versionStatus, int declarationTemplateId) {
        try {
            return declarationTemplateDao.updateVersionStatus(versionStatus, declarationTemplateId);
        }catch (DaoException e){
            throw new ServiceException("Обновление статуса декларации.", e);
        }
    }

    @Override
    public void deleteXsd(int dtId) {
        try {
            DeclarationTemplate template = get(dtId);
            declarationTemplateDao.deleteXsd(dtId);
            blobDataService.delete(template.getXsdId());
        } catch (DaoException e){
            throw new ServiceException("Ошибка при удалении xsd", e);
        }
    }

    @Override
    public void deleteJrxml(int dtId) {
        try {
            DeclarationTemplate template = get(dtId);
            declarationTemplateDao.deleteJrxml(dtId);
            blobDataService.delete(template.getJrxmlBlobId());
        } catch (DaoException e){
            throw new ServiceException("Ошибка при удалении jrxml", e);
        }
    }

    @Override
    public boolean existDeclarationTemplate(int declarationTypeId, int reportPeriodId) {
        return declarationTemplateDao.existDeclarationTemplate(declarationTypeId, reportPeriodId);
    }

    @Override
    public boolean checkExistingDataJrxml(int dtId, Logger logger) {
        boolean isExist = false;
        DeclarationTemplate template = declarationTemplateDao.get(dtId);
        TAUserInfo currUser = logger.getTaUserInfo();
        ArrayList<String> existDec = new ArrayList<String>();
        ArrayList<String> existInLockDec = new ArrayList<String>();

        for (Long dataId : declarationDataService.getFormDataListInActualPeriodByTemplate(template.getId(), template.getVersion())){
            DeclarationData data = declarationDataService.get(dataId, currUser);
            String decKeyPDF = declarationDataService.generateAsyncTaskKey(dataId, ReportType.PDF_DEC);
            String decKeyXLSM = declarationDataService.generateAsyncTaskKey(dataId, ReportType.EXCEL_DEC);
            ReportPeriod rp = periodService.getReportPeriod(data.getReportPeriodId());
            DepartmentReportPeriod drp = departmentReportPeriodService.get(data.getDepartmentReportPeriodId());
            if (
                    reportService.getDec(currUser, dataId, ReportType.PDF_DEC) != null
                            ||
                            reportService.getDec(currUser, dataId, ReportType.EXCEL_DEC) != null) {
                existDec.add(String.format(
                        DEC_DATA_EXIST_IN_TASK,
                        template.getName(),
                        departmentService.getDepartment(data.getDepartmentId()).getName(),
                        rp.getName(),
                        rp.getTaxPeriod().getYear(),
                        drp.getCorrectionDate() != null ? String.format("с датой сдачи корректировки %s",
                                SDF_DD_MM_YYYY.format(drp.getCorrectionDate())) : "",
                        data.getTaxOrganCode(),
                        data.getKpp()));

            }  else if(lockDataService.isLockExists(decKeyPDF, false) || lockDataService.isLockExists(decKeyXLSM, false)){
                existInLockDec.add(String.format(
                        DEC_DATA_EXIST_IN_TASK,
                        template.getName(),
                        departmentService.getDepartment(data.getDepartmentId()).getName(),
                        rp.getName(),
                        rp.getTaxPeriod().getYear(),
                        drp.getCorrectionDate() != null ? String.format("с датой сдачи корректировки %s",
                                SDF_DD_MM_YYYY.format(drp.getCorrectionDate())) : "",
                        data.getTaxOrganCode(),
                        data.getKpp()));
            }
        }
        if(!existInLockDec.isEmpty()){
            isExist = true;
            logger.warn("По следующим экземплярам деклараций запущена операция формирования pdf/xlsx отчета:");
            for (String s : existInLockDec){
                logger.warn(s);
            }
        }
        if(!existDec.isEmpty()){
            isExist = true;
            logger.warn("По следующим экземплярам деклараций сформирован pdf/xlsx отчет:");
            for (String s : existDec){
                logger.warn(s);
            }
        }

        return isExist;
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS)
    public Collection<Long> getDataIdsThatUseJrxml(int dtId, TAUserInfo userInfo) {
        HashSet<Long> dataIds = new HashSet<Long>();
        DeclarationTemplate template = declarationTemplateDao.get(dtId);
        for (Long dataId : declarationDataService.getFormDataListInActualPeriodByTemplate(template.getId(), template.getVersion())){
            if(reportService.getDec(userInfo, dataId, ReportType.PDF_DEC) != null
                    ||
                    reportService.getDec(userInfo, dataId, ReportType.EXCEL_DEC) != null){
                dataIds.add(dataId);
            }
        }
        return Collections.unmodifiableCollection(dataIds);
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS)
    public Collection<Long> getLockDataIdsThatUseJrxml(int dtId) {
        HashSet<Long> lockDataIds = new HashSet<Long>();
        DeclarationTemplate template = declarationTemplateDao.get(dtId);
        for (Long dataId : declarationDataService.getFormDataListInActualPeriodByTemplate(template.getId(), template.getVersion())){
            String decKeyPDF = declarationDataService.generateAsyncTaskKey(dataId, ReportType.PDF_DEC);
            String decKeyXLSM = declarationDataService.generateAsyncTaskKey(dataId, ReportType.EXCEL_DEC);
            if (lockDataService.isLockExists(decKeyPDF, false) || lockDataService.isLockExists(decKeyXLSM, false)) {
                lockDataIds.add(dataId);
            }
        }
        return Collections.unmodifiableCollection(lockDataIds);
    }

    @Override
	public boolean lock(int declarationTemplateId, TAUserInfo userInfo){
        DeclarationTemplate declarationTemplate = get(declarationTemplateId);
        Date endVersion = getDTEndDate(declarationTemplateId);
        LockData objectLock = lockDataService.lock(LockData.LockObjects.DECLARATION_TEMPLATE.name() + "_" + declarationTemplateId, userInfo.getUser().getId(),
                String.format(
                        LockData.DescriptionTemplate.DECLARATION_TEMPLATE.getText(),
                        declarationTemplate.getName(),
                        declarationTemplate.getType().getTaxType().getName(),
                        SDF_DD_MM_YYYY.format(declarationTemplate.getVersion()),
                        endVersion != null ? SDF_DD_MM_YYYY.format(endVersion) : "-"
                ),
                lockDataService.getLockTimeout(LockData.LockObjects.DECLARATION_TEMPLATE));
        return !(objectLock != null && objectLock.getUserId() != userInfo.getUser().getId());
    }

	@Override
	public boolean unlock(int declarationTemplateId, TAUserInfo userInfo){
        lockDataService.unlock(LockData.LockObjects.DECLARATION_TEMPLATE.name() + "_" + declarationTemplateId,
                userInfo.getUser().getId());
        return true;
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
}
