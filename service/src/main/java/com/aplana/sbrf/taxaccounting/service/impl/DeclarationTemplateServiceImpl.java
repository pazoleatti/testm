package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.service.LockDataService;
import com.aplana.sbrf.taxaccounting.dao.DeclarationSubreportDao;
import com.aplana.sbrf.taxaccounting.dao.DeclarationTemplateDao;
import com.aplana.sbrf.taxaccounting.dao.DeclarationTemplateEventScriptDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.AccessDeniedException;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.*;
import com.aplana.sbrf.taxaccounting.service.TransactionHelper;
import com.aplana.sbrf.taxaccounting.service.TransactionLogic;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.StringWriter;
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

	private static final Log LOG = LogFactory.getLog(DeclarationTemplateServiceImpl.class);
    private static final String ENCODING = "UTF-8";
    private static final String JRXML_NOT_FOUND = "Не удалось получить jrxml-шаблон налоговой формы!";
    private static final ThreadLocal<SimpleDateFormat> sdf = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("dd.MM.yyyy");
        }
    };
    private static final String DEC_DATA_EXIST_IN_TASK =
            "%s в подразделении \"%s\" в периоде \"%s %d%s\"%s%s";
    private static final int SUBREPORT_NAME_MAX_VALUE = 1000;
    private static final int SUBREPORT_ALIAS_MAX_VALUE = 128;

	@Autowired
	private DeclarationTemplateDao declarationTemplateDao;
    @Autowired
	private BlobDataService blobDataService;
    @Autowired
	private TransactionHelper tx;
    @Autowired
	private LockDataService lockDataService;
    @Autowired
	private DepartmentService departmentService;
    @Autowired
	private DeclarationDataService declarationDataService;
    @Autowired
	private PeriodService periodService;
    @Autowired
	private ReportService reportService;
    @Autowired
	private DepartmentReportPeriodService departmentReportPeriodService;
    @Autowired
    private DeclarationDataScriptingService declarationDataScriptingService;
    @Autowired
    private LogEntryService logEntryService;
	@Autowired
	private TAUserService userService;
    @Autowired
    private DeclarationSubreportDao declarationSubreportDao;
    @Autowired
    @Qualifier("declarationTemplateMainOperatingService")
    MainOperatingService mainOperatingService;
    @Autowired
    private DeclarationTemplateEventScriptDao declarationTemplateEventScriptDao;

    @Override
	public List<DeclarationTemplate> listAll() {
		return declarationTemplateDao.listAll();
	}

	@Override
	public DeclarationTemplate get(int declarationTemplateId) {
        try{
            return declarationTemplateDao.get(declarationTemplateId);
        } catch (DaoException e){
            throw new ServiceException("Ошибка получения шаблона налоговой формы.", e);
        }
	}

	@Override
	public int save(DeclarationTemplate declarationTemplate) {
        if (declarationTemplate.getId() == null){
            int declarationTemplateId = declarationTemplateDao.create(declarationTemplate);
            saveDeclarationTemplateFile((long)declarationTemplateId, new ArrayList<DeclarationTemplateFile>(), declarationTemplate.getDeclarationTemplateFiles());
            return declarationTemplateId;
        }
        checkScript(declarationTemplate, new Logger());
        DeclarationTemplate declarationTemplateBase = declarationTemplateDao.get(declarationTemplate.getId());
        int savedId = declarationTemplateDao.save(declarationTemplate);
        if (declarationTemplate.getXsdId() != null && !declarationTemplate.getXsdId().equals(declarationTemplateBase.getXsdId()))
            blobDataService.delete(declarationTemplateBase.getXsdId());
        if (declarationTemplate.getJrxmlBlobId() != null && !declarationTemplate.getJrxmlBlobId().equals(declarationTemplateBase.getJrxmlBlobId()))
            blobDataService.delete(declarationTemplateBase.getJrxmlBlobId());
        saveDeclarationTemplateFile((long) savedId, declarationTemplateBase.getDeclarationTemplateFiles(), declarationTemplate.getDeclarationTemplateFiles());
        return savedId;
	}

	private void saveDeclarationTemplateFile(Long declarationTemplateId, List<DeclarationTemplateFile> oldFiles, List<DeclarationTemplateFile> newFiles) {
        List<String> deleteBlobIds = new ArrayList<String>();
        List<String> createBlobIds = new ArrayList<String>();

        for (DeclarationTemplateFile oldFile : oldFiles) {
            if (!newFiles.contains(oldFile)) {
                deleteBlobIds.add(oldFile.getBlobDataId());
            }
        }

        for (DeclarationTemplateFile newFile : newFiles) {
            if (!oldFiles.contains(newFile)) {
                createBlobIds.add(newFile.getBlobDataId());
            }
        }

        declarationTemplateDao.deleteTemplateFile(declarationTemplateId, deleteBlobIds);
        declarationTemplateDao.createTemplateFile(declarationTemplateId, createBlobIds);
    }

    private void checkScript(DeclarationTemplate declarationTemplate, Logger logger) {
        if (declarationTemplate.getCreateScript() == null || declarationTemplate.getCreateScript().isEmpty())
            return;
        Logger tempLogger = new Logger();
        try{
            // Устанавливает тестовые параметры НФ. При необходимости в скрипте значения можно поменять
            DeclarationData declaration = new DeclarationData();
            declaration.setDepartmentReportPeriodId(1);
            declaration.setReportPeriodId(1);
            declaration.setDepartmentId(1);
            declaration.setState(State.CREATED);

            declarationDataScriptingService.executeScriptInNewReadOnlyTransaction(userService.getSystemUserInfo(), declarationTemplate, declaration, FormDataEvent.CHECK_SCRIPT, tempLogger, null);
            if (tempLogger.containsLevel(LogLevel.ERROR)) {
                throw new ServiceException("Обнаружены фатальные ошибки!");
            }
        } catch (Exception ex) {
            tempLogger.error(ex);
            logger.getEntries().addAll(tempLogger.getEntries());
            throw new ServiceLoggerException("Обнаружены ошибки в скрипте!", logEntryService.save(logger.getEntries()));
        }
        logger.getEntries().addAll(tempLogger.getEntries());
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
        StringWriter writer = new StringWriter();
        try {
            IOUtils.copy(jrxmlBlobData.getInputStream(), writer, ENCODING);
            IOUtils.closeQuietly(jrxmlBlobData.getInputStream());
            return writer.toString();
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
            throw new ServiceException(JRXML_NOT_FOUND);
        } finally {
            IOUtils.closeQuietly(writer);
        }
    }

 	@Override
	public void checkLockedByAnotherUser(Integer declarationTemplateId, TAUserInfo userInfo){
		if (declarationTemplateId!=null){
			LockData objectLock = lockDataService.getLock(LockData.LockObjects.DECLARATION_TEMPLATE.name() + "_" + declarationTemplateId);
			if (objectLock != null && objectLock.getUserId() != userInfo.getUser().getId()) {
				throw new AccessDeniedException("Шаблон налоговой формы заблокирован другим пользователем");
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
            throw new ServiceException("Обновление статуса налоговой формы.", e);
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
            String decKeyPDF = declarationDataService.generateAsyncTaskKey(dataId, DeclarationDataReportType.PDF_DEC);
            String decKeyXLSM = declarationDataService.generateAsyncTaskKey(dataId, DeclarationDataReportType.EXCEL_DEC);
            ReportPeriod rp = periodService.getReportPeriod(data.getReportPeriodId());
            DepartmentReportPeriod drp = departmentReportPeriodService.get(data.getDepartmentReportPeriodId());
            if (
                    reportService.getDec(currUser, dataId, DeclarationDataReportType.PDF_DEC) != null
                            ||
                            reportService.getDec(currUser, dataId, DeclarationDataReportType.EXCEL_DEC) != null) {
                existDec.add(String.format(
                        DEC_DATA_EXIST_IN_TASK,
                        template.getName(),
                        departmentService.getDepartment(data.getDepartmentId()).getName(),
                        rp.getName(),
                        rp.getTaxPeriod().getYear(),
                        drp.getCorrectionDate() != null ? String.format("с датой сдачи корректировки %s",
                                sdf.get().format(drp.getCorrectionDate())) : "",
                        data.getTaxOrganCode() != null ? ", налоговый орган " + data.getTaxOrganCode() : "",
                        data.getKpp() != null ? ", КПП " + data.getKpp() : ""
                ));

            }  else if(lockDataService.isLockExists(decKeyPDF, false) || lockDataService.isLockExists(decKeyXLSM, false)){
                existInLockDec.add(String.format(
                        DEC_DATA_EXIST_IN_TASK,
                        template.getName(),
                        departmentService.getDepartment(data.getDepartmentId()).getName(),
                        rp.getName(),
                        rp.getTaxPeriod().getYear(),
                        drp.getCorrectionDate() != null ? String.format("с датой сдачи корректировки %s",
                                sdf.get().format(drp.getCorrectionDate())) : "",
                        data.getTaxOrganCode() != null ? ", налоговый орган " + data.getTaxOrganCode() : "",
                        data.getKpp() != null ? ", КПП " + data.getKpp() : ""
                ));
            }
        }
        if(!existInLockDec.isEmpty()){
            isExist = true;
            logger.warn("По следующим экземплярам налоговых форм запущена операция формирования pdf/xlsx отчета:");
            for (String s : existInLockDec){
                logger.warn(s);
            }
        }
        if(!existDec.isEmpty()){
            isExist = true;
            logger.warn("По следующим экземплярам налоговых форм сформирован pdf/xlsx отчет:");
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
            if(reportService.getDec(userInfo, dataId, DeclarationDataReportType.PDF_DEC) != null
                    ||
                    reportService.getDec(userInfo, dataId, DeclarationDataReportType.EXCEL_DEC) != null){
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
            String decKeyPDF = declarationDataService.generateAsyncTaskKey(dataId, DeclarationDataReportType.PDF_DEC);
            String decKeyXLSM = declarationDataService.generateAsyncTaskKey(dataId, DeclarationDataReportType.EXCEL_DEC);
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
                        DescriptionTemplate.DECLARATION_TEMPLATE.getText(),
                        declarationTemplate.getName(),
                        TaxType.NDFL.getName(),
                        sdf.get().format(declarationTemplate.getVersion()),
                        endVersion != null ? sdf.get().format(endVersion) : "-"
                ));
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

    @Override
    public void updateScript(DeclarationTemplate declarationTemplate, Logger log, TAUserInfo userInfo) {
        try {
            checkScript(declarationTemplate, log);
        } catch (ServiceLoggerException e) {
            LOG.error(e.getLocalizedMessage(), e);
            return;
        }
        declarationTemplateDao.updateScript(declarationTemplate.getId(), declarationTemplate.getCreateScript());
        mainOperatingService.logging(declarationTemplate.getId(), FormDataEvent.SCRIPTS_IMPORT, userInfo.getUser());
    }

    @Override
    public Integer get(int declarationTypeId, int year) {
        return declarationTemplateDao.get(declarationTypeId, year);
    }

    @Override
    public DeclarationSubreport getSubreportByAlias(int declarationTemplateId, String alias) {
        return declarationSubreportDao.getSubreportByAlias(declarationTemplateId, alias);
    }

    @Override
    public void validateDeclarationTemplate(DeclarationTemplate declarationTemplate, Logger logger) {
        List<DeclarationSubreport> subreports = declarationTemplate.getSubreports();
        Set<String> checkSet = new HashSet<String>();

        for (DeclarationSubreport subreport : subreports) {
            if (subreport.getAlias() == null || subreport.getAlias().isEmpty()) {
                logger.error(
                        String.format(
                                "Отчет №\"%s\". Поле \"Псевдоним\" обязательно для заполнения.",
                                subreport.getOrder(), subreport.getAlias()
                        )
                );
            } else {
                if (!checkSet.add(subreport.getAlias())) {
                    logger.error(
                            String.format(
                                    "Отчет №\"%s\". Нарушено требование к уникальности, уже существует отчет с псевдонимом \"%s\" в данной версии макета!",
                                    subreport.getOrder(), subreport.getAlias()
                            )
                    );
                }
                if (subreport.getAlias().getBytes().length > SUBREPORT_ALIAS_MAX_VALUE) {
                    logger.error("Отчет №\"%s\". Значение для псевдонима отчета слишком велико (фактическое: %d, максимальное: %d)",
                            subreport.getOrder(), subreport.getAlias().getBytes().length, SUBREPORT_ALIAS_MAX_VALUE);
                }
            }

            if (subreport.getName() == null || subreport.getName().isEmpty()) {
                logger.error("Отчет №\"%s\". Поле \"Наименование\" обязательно для заполнения.",
                            subreport.getOrder(), subreport.getName());
            } else {
                if (subreport.getName().getBytes().length > SUBREPORT_NAME_MAX_VALUE) {
                    logger.error("Отчет №\"%s\". Значение для имени отчета слишком велико (фактическое: %d, максимальное: %d)",
                                 subreport.getOrder(), subreport.getName().getBytes().length, SUBREPORT_NAME_MAX_VALUE);
                }
            }
        }
    }

    @Override
    public boolean checkIfEventScriptPresent(int declarationTemplateId, int formDataEventId) {
        return declarationTemplateEventScriptDao.checkIfEventScriptPresent(declarationTemplateId, formDataEventId);
    }

    @Override
    public List<DeclarationTemplateCheck> getChecks(int declarationTypeId, Integer declarationTemplateId) {
        return declarationTemplateDao.getChecks(declarationTypeId, declarationTemplateId);
    }

    @Override
    public void createChecks(List<DeclarationTemplateCheck> checks, Integer declarationTemplateId) {
        if (CollectionUtils.isNotEmpty(checks)) {
            declarationTemplateDao.createChecks(checks, declarationTemplateId);
        }
    }

    @Override
    public void updateChecks(List<DeclarationTemplateCheck> checks, Integer declarationTemplateId) {
        if (CollectionUtils.isNotEmpty(checks)) {
            declarationTemplateDao.updateChecks(checks, declarationTemplateId);
        }
    }
}
