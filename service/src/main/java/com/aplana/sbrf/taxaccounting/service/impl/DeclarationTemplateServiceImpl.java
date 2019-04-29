package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.async.AsyncManager;
import com.aplana.sbrf.taxaccounting.dao.*;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.action.UpdateTemplateAction;
import com.aplana.sbrf.taxaccounting.model.action.UpdateTemplateStatusAction;
import com.aplana.sbrf.taxaccounting.model.exception.AccessDeniedException;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.result.ActionResult;
import com.aplana.sbrf.taxaccounting.model.result.UpdateTemplateResult;
import com.aplana.sbrf.taxaccounting.model.result.UpdateTemplateStatusResult;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.service.*;
import com.aplana.sbrf.taxaccounting.templateversion.DeclarationTemplateVersionService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static com.aplana.sbrf.taxaccounting.model.VersionedObjectStatus.FAKE;


/**
 * Сервис для работы с шаблонами деклараций.
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
    private static final int SUBREPORT_NAME_MAX_VALUE = 1000;
    private static final int SUBREPORT_ALIAS_MAX_VALUE = 128;
    private static final String SCRIPT_FILE = "script.groovy";
    private static final String REPORT_FILE = "report.jrxml";
    private static final String CONTENT_FILE = "content.xml";
    private static final String XSD = "main.xsd";
    private static final ThreadLocal<SimpleDateFormat> SIMPLE_DATE_FORMAT_YEAR = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy");
        }
    };

    @Autowired
    private AsyncManager asyncManager;
    @Autowired
    private LogEntryService logEntryService;
    @Autowired
    private DeclarationTemplateService declarationTemplateService;
    @Autowired
    private DeclarationTemplateVersionService declarationTemplateVersionService;
    @Autowired
    private TemplateChangesService templateChangesService;
    @Autowired
    private DeclarationDataService declarationDataService;
    @Autowired
    private AuditService auditService;
    @Autowired
    private BlobDataService blobDataService;
    @Autowired
    private LockDataService lockDataService;
    @Autowired
    private DepartmentService departmentService;
    @Autowired
    private PeriodService periodService;
    @Autowired
    private DepartmentReportPeriodService departmentReportPeriodService;
    @Autowired
    private DeclarationDataScriptingService declarationDataScriptingService;

    @Autowired
    private ReportDao reportDao;
    @Autowired
    private DeclarationDataDao declarationDataDao;
    @Autowired
    private DeclarationTemplateDao declarationTemplateDao;
    @Autowired
    private DeclarationSubreportDao declarationSubreportDao;
    @Autowired
    private DeclarationTemplateEventScriptDao declarationTemplateEventScriptDao;


    @Override
    public List<DeclarationTemplate> listAll() {
        return declarationTemplateDao.listAll();
    }

    @Override
    public DeclarationTemplate get(int declarationTemplateId) {
        try {
            return declarationTemplateDao.get(declarationTemplateId);
        } catch (DaoException e) {
            throw new ServiceException("Ошибка получения шаблона налоговой формы.", e);
        }
    }

    @Override
    @PreAuthorize("hasPermission(#declarationTemplateId, 'com.aplana.sbrf.taxaccounting.model.DeclarationTemplate', T(com.aplana.sbrf.taxaccounting.permissions.DeclarationTemplatePermission).VIEW)")
    public DeclarationTemplate fetchWithScripts(int declarationTemplateId) {
        DeclarationTemplate declarationTemplate = get(declarationTemplateId);
        List<DeclarationTemplateEventScript> eventScriptList = declarationTemplateEventScriptDao.fetch(declarationTemplateId);
        declarationTemplate.setEventScripts(eventScriptList);
        declarationTemplate.setCreateScript(getDeclarationTemplateScript(declarationTemplateId));
        return declarationTemplate;
    }

    @Override
    public int save(DeclarationTemplate declarationTemplate, TAUserInfo userInfo) {
        LOG.info(String.format("DeclarationTemplateServiceImpl.save. declarationTemplate: %s",
                declarationTemplate.getId()));
        if (declarationTemplate.getId() == null) {
            int declarationTemplateId = declarationTemplateDao.create(declarationTemplate);
            saveDeclarationTemplateFile(declarationTemplateId, new ArrayList<DeclarationTemplateFile>(), declarationTemplate.getDeclarationTemplateFiles());
            return declarationTemplateId;
        }
        checkScript(declarationTemplate, new Logger(), userInfo);
        DeclarationTemplate declarationTemplateBase = declarationTemplateDao.get(declarationTemplate.getId());
        int savedId = declarationTemplateDao.save(declarationTemplate);
        if (declarationTemplate.getXsdId() != null && !declarationTemplate.getXsdId().equals(declarationTemplateBase.getXsdId()))
            blobDataService.delete(declarationTemplateBase.getXsdId());
        if (declarationTemplate.getJrxmlBlobId() != null && !declarationTemplate.getJrxmlBlobId().equals(declarationTemplateBase.getJrxmlBlobId()))
            blobDataService.delete(declarationTemplateBase.getJrxmlBlobId());
        saveDeclarationTemplateFile(savedId, declarationTemplateBase.getDeclarationTemplateFiles(), declarationTemplate.getDeclarationTemplateFiles());
        return savedId;
    }

    private void saveDeclarationTemplateFile(int declarationTemplateId, List<DeclarationTemplateFile> oldFiles, List<DeclarationTemplateFile> newFiles) {
        List<String> deleteBlobIds = new ArrayList<>();
        List<String> createBlobIds = new ArrayList<>();

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

    private void checkScript(DeclarationTemplate declarationTemplate, Logger logger, TAUserInfo userInfo) {
        if (declarationTemplate.getCreateScript() == null || declarationTemplate.getCreateScript().isEmpty())
            return;
        Logger tempLogger = new Logger();
        try {
            declarationDataScriptingService.executeScriptInNewReadOnlyTransaction(userInfo, declarationTemplate, FormDataEvent.CHECK_SCRIPT, tempLogger, null);
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
    public int getActiveDeclarationTemplateId(int declarationTypeId, int reportPeriodId) {
        try {
            return declarationTemplateDao.getActiveDeclarationTemplateId(declarationTypeId, reportPeriodId);
        } catch (DaoException e) {
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

    /**
     * Проверяет, не заблокирован ли шаблон декларации другим пользователем
     *
     * @param declarationTemplateId идентификатор вида декларации
     * @param userInfo              - информация о пользователе
     */
    private void checkLockedByAnotherUser(Integer declarationTemplateId, TAUserInfo userInfo) {
        if (declarationTemplateId != null) {
            LockData objectLock = lockDataService.findLock(LockData.LockObjects.DECLARATION_TEMPLATE.name() + "_" + declarationTemplateId);
            if (objectLock != null && objectLock.getUserId() != userInfo.getUser().getId()) {
                throw new AccessDeniedException("Шаблон налоговой формы заблокирован другим пользователем");
            }
        }
    }

    @Override
    public String getDeclarationTemplateScript(int declarationTemplateId) {
        return declarationTemplateDao.getDeclarationTemplateScript(declarationTemplateId);
    }

    @Override
    @PreAuthorize("hasPermission(#userInfo.user, T(com.aplana.sbrf.taxaccounting.permissions.UserPermission).VIEW_ADMINISTRATION_SETTINGS)")
    public List<DeclarationTemplate> fetchAllByType(int declarationTypeId, TAUserInfo userInfo) {
        return declarationTemplateDao.fetchAllByType(declarationTypeId);
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
    public int updateVersionStatus(VersionedObjectStatus versionStatus, int declarationTemplateId) {
        try {
            return declarationTemplateDao.updateVersionStatus(versionStatus, declarationTemplateId);
        } catch (DaoException e) {
            throw new ServiceException("Обновление статуса налоговой формы.", e);
        }
    }

    /**
     * Поиск экземпляров деклараций использующих данную версию макета, которые имеют pdf/xlsx отчеты и/или
     * для которых созданы блокировки для задания формирования pdf/xlsx отчета
     *
     * @param dtId идентификатор макета декларации
     * @return true - если есть декларации
     */
    private boolean checkExistingDataJrxml(int dtId, Logger logger) {
        boolean isExist = false;
        DeclarationTemplate template = declarationTemplateDao.get(dtId);
        ArrayList<String> existDec = new ArrayList<>();
        ArrayList<String> existInLockDec = new ArrayList<>();

        for (Long dataId : declarationDataService.getFormDataListInActualPeriodByTemplate(template.getId(), template.getVersion())) {
            DeclarationData data = declarationDataDao.get(dataId);
            String decKeyPDF = declarationDataService.generateAsyncTaskKey(dataId, AsyncTaskType.PDF_DEC);
            String decKeyXLSM = declarationDataService.generateAsyncTaskKey(dataId, AsyncTaskType.EXCEL_DEC);
            ReportPeriod rp = periodService.fetchReportPeriod(data.getReportPeriodId());
            DepartmentReportPeriod drp = departmentReportPeriodService.fetchOne(data.getDepartmentReportPeriodId());

            String message = String.format(
                    "%s в подразделении \"%s\" в периоде \"%s %d%s\"%s%s",
                    template.getName(),
                    departmentService.getDepartment(data.getDepartmentId()).getName(),
                    rp.getName(),
                    rp.getTaxPeriod().getYear(),
                    drp.getCorrectionDate() != null ? String.format("с датой сдачи корректировки %s", sdf.get().format(drp.getCorrectionDate())) : "",
                    data.getTaxOrganCode() != null ? ", налоговый орган " + data.getTaxOrganCode() : "",
                    data.getKpp() != null ? ", КПП " + data.getKpp() : ""
            );

            if (reportDao.getDec(dataId, DeclarationDataReportType.PDF_DEC) != null || reportDao.getDec(dataId, DeclarationDataReportType.EXCEL_DEC) != null) {
                existDec.add(message);
            } else if (lockDataService.lockExists(decKeyPDF) || lockDataService.lockExists(decKeyXLSM)) {
                existInLockDec.add(message);
            }
        }
        if (!existInLockDec.isEmpty()) {
            isExist = true;
            logger.warn("По следующим экземплярам налоговых форм запущена операция формирования pdf/xlsx отчета:");
            for (String s : existInLockDec) {
                logger.warn(s);
            }
        }
        if (!existDec.isEmpty()) {
            isExist = true;
            logger.warn("По следующим экземплярам налоговых форм сформирован pdf/xlsx отчет:");
            for (String s : existDec) {
                logger.warn(s);
            }
        }

        return isExist;
    }

    /**
     * Получает идентификаторы деклараций, которые используют jrxml этого макета
     */
    private Collection<Long> getDataIdsThatUseJrxml(int dtId) {
        HashSet<Long> dataIds = new HashSet<>();
        DeclarationTemplate template = declarationTemplateDao.get(dtId);
        for (Long dataId : declarationDataService.getFormDataListInActualPeriodByTemplate(template.getId(), template.getVersion())) {
            if (reportDao.getDec(dataId, DeclarationDataReportType.PDF_DEC) != null
                    || reportDao.getDec(dataId, DeclarationDataReportType.EXCEL_DEC) != null) {
                dataIds.add(dataId);
            }
        }
        return Collections.unmodifiableCollection(dataIds);
    }

    /**
     * Получает идентификаторы деклараций, для которых формируется отчет по этому jrxml
     */
    private Collection<Long> getLockDataIdsThatUseJrxml(int dtId) {
        HashSet<Long> lockDataIds = new HashSet<>();
        DeclarationTemplate template = declarationTemplateDao.get(dtId);
        for (Long dataId : declarationDataService.getFormDataListInActualPeriodByTemplate(template.getId(), template.getVersion())) {
            String decKeyPDF = declarationDataService.generateAsyncTaskKey(dataId, AsyncTaskType.PDF_DEC);
            String decKeyXLSM = declarationDataService.generateAsyncTaskKey(dataId, AsyncTaskType.EXCEL_DEC);
            if (lockDataService.lockExists(decKeyPDF) || lockDataService.lockExists(decKeyXLSM)) {
                lockDataIds.add(dataId);
            }
        }
        return Collections.unmodifiableCollection(lockDataIds);
    }

    /**
     * Блокировка declarationTemplate.
     *
     * @param declarationTemplateId - идентификатор налоговой формы
     * @param userInfo              - информация о пользователе
     * @return информацию о блокировке объекта
     */
    private LockData lock(int declarationTemplateId, TAUserInfo userInfo) {
        DeclarationTemplate declarationTemplate = get(declarationTemplateId);
        Date endVersion = getDTEndDate(declarationTemplateId);
        return lockDataService.lock(LockData.LockObjects.DECLARATION_TEMPLATE.name() + "_" + declarationTemplateId, userInfo.getUser().getId(),
                String.format(
                        DescriptionTemplate.DECLARATION_TEMPLATE.getText(),
                        declarationTemplate.getName(),
                        TaxType.NDFL.getName(),
                        sdf.get().format(declarationTemplate.getVersion()),
                        endVersion != null ? sdf.get().format(endVersion) : "-"
                ));
    }

    /**
     * Снять блокировку с declarationTemplate.
     *
     * @param declarationTemplateId - идентификатор шаблона налоговой формы
     * @param userInfo              - информация о пользователе
     * @return true - если удалось разблокировать форму декларации, иначе - false
     */
    private boolean unlock(int declarationTemplateId, TAUserInfo userInfo) {
        lockDataService.unlock(LockData.LockObjects.DECLARATION_TEMPLATE.name() + "_" + declarationTemplateId,
                userInfo.getUser().getId());
        return true;
    }

    private List<Integer> createStatusList(VersionedObjectStatus[] status) {
        List<Integer> statusList = new ArrayList<>();
        if (status.length == 0) {
            statusList.add(VersionedObjectStatus.NORMAL.getId());
            statusList.add(FAKE.getId());
            statusList.add(VersionedObjectStatus.DRAFT.getId());
        } else {
            for (VersionedObjectStatus objectStatus : status)
                statusList.add(objectStatus.getId());
        }

        return statusList;
    }

    @Override
    public void updateScript(DeclarationTemplate declarationTemplate, Logger log, TAUserInfo userInfo) {
        LOG.info(String.format("DeclarationTemplateServiceImpl.updateScript. declarationTemplate: %s", declarationTemplate.getId()));
        try {
            checkScript(declarationTemplate, log, userInfo);
        } catch (ServiceLoggerException e) {
            LOG.error(e.getLocalizedMessage(), e);
            return;
        }
        declarationTemplateDao.updateScript(declarationTemplate.getId(), declarationTemplate.getCreateScript());
        logging(declarationTemplate.getId(), FormDataEvent.SCRIPTS_IMPORT, userInfo.getUser());
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
        Set<String> checkSet = new HashSet<>();

        for (DeclarationSubreport subreport : subreports) {
            if (subreport.getAlias() == null || subreport.getAlias().isEmpty()) {
                logger.error(String.format("Отчет №\"%s\". Поле \"Псевдоним\" обязательно для заполнения.", subreport.getOrder()));
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
    public List<DeclarationTemplateCheck> getChecks(int declarationTypeId, Integer declarationTemplateId) {
        return declarationTemplateDao.getChecks(declarationTypeId, declarationTemplateId);
    }

    @Override
    public void updateChecks(List<DeclarationTemplateCheck> checks, Integer declarationTemplateId) {
        if (CollectionUtils.isNotEmpty(checks)) {
            declarationTemplateDao.updateChecks(checks, declarationTemplateId);
        }
    }

    @Override
    @PreAuthorize("hasPermission(#action.declarationTemplate.id, 'com.aplana.sbrf.taxaccounting.model.DeclarationTemplate', T(com.aplana.sbrf.taxaccounting.permissions.DeclarationTemplatePermission).UPDATE)")
    public UpdateTemplateResult update(UpdateTemplateAction action, TAUserInfo userInfo) {
        LOG.info(String.format("DeclarationTemplateServiceImpl.update. action: %s", action));
        UpdateTemplateResult result = new UpdateTemplateResult();
        Logger logger = new Logger();

        // Устанавливаем границы действия макета полученной с фронтенда версии
        DeclarationTemplate template = action.getDeclarationTemplate();
        template.setVersion(DateUtils.truncate(template.getVersion(), Calendar.DATE));
        if (template.getVersionEnd() != null) {
            template.setVersionEnd(DateUtils.truncate(template.getVersionEnd(), Calendar.DATE));
        }

        // Блокируем макет
        LockData lockData = lock(template.getId(), userInfo);
        if (lockData != null && lockData.getUserId() != userInfo.getUser().getId()) {
            throw new ServiceException("Макет формы заблокирован другим пользователем");
        }
        try {
            //TODO: Подразумевается, что false вернётся только в случае, когда потребуется подтверждение. Странный подход
            if (edit(template, action.getChecks(), template.getVersionEnd(), logger, userInfo, action.isFormsExistWarningConfirmed())) {
                result.setSuccess(true);
                if (!logger.getEntries().isEmpty()) {
                    result.setUuid(logEntryService.save(logger.getEntries()));
                }
            } else {
                result.setConfirmNeeded(true);
            }
        } finally {
            unlock(template.getId(), userInfo);
        }
        return result;
    }

    @Override
    @PreAuthorize("hasPermission(#action.templateId, 'com.aplana.sbrf.taxaccounting.model.DeclarationTemplate', T(com.aplana.sbrf.taxaccounting.permissions.DeclarationTemplatePermission).UPDATE)")
    public UpdateTemplateStatusResult updateStatus(UpdateTemplateStatusAction action, TAUserInfo userInfo) {
        LOG.info(String.format("DeclarationTemplateServiceImpl.updateStatus. action: %s", action));
        UpdateTemplateStatusResult result = new UpdateTemplateStatusResult();
        Logger logger = new Logger();

        result.setSuccess(setStatusTemplate(action.getTemplateId(), logger, userInfo, action.isFormsExistWarningConfirmed()));
        result.setConfirmNeeded(!result.isSuccess());

        result.setStatus(get(action.getTemplateId()).getStatus());
        if (!logger.getEntries().isEmpty()) {
            result.setUuid(logEntryService.save(logger.getEntries()));
        }
        return result;
    }

    @Override
    @PreAuthorize("hasPermission(#declarationTemplateId, 'com.aplana.sbrf.taxaccounting.model.DeclarationTemplate', T(com.aplana.sbrf.taxaccounting.permissions.DeclarationTemplatePermission).VIEW)")
    public String uploadXsd(int declarationTemplateId, InputStream inputStream, String fileName) {
        LOG.info(String.format("DeclarationTemplateServiceImpl.uploadXsd. declarationTemplateId: %s; fileName: %s", declarationTemplateId, fileName));

        String fileExtension = FilenameUtils.getExtension(fileName);
        if (fileExtension.equals("xsd")) {
            return blobDataService.create(inputStream, fileName);
        } else {
            throw new ServiceException("Файл должен иметь расширение xsd!");
        }
    }

    @Override
    @PreAuthorize("hasPermission(#declarationTemplateId, 'com.aplana.sbrf.taxaccounting.model.DeclarationTemplate', T(com.aplana.sbrf.taxaccounting.permissions.DeclarationTemplatePermission).VIEW)")
    public BlobData downloadXsd(int declarationTemplateId) {
        DeclarationTemplate dt = declarationTemplateDao.get(declarationTemplateId);
        return blobDataService.get(dt.getXsdId());
    }

    @Override
    @PreAuthorize("hasPermission(#declarationTemplateId, 'com.aplana.sbrf.taxaccounting.model.DeclarationTemplate', T(com.aplana.sbrf.taxaccounting.permissions.DeclarationTemplatePermission).VIEW)")
    public void exportDeclarationTemplate(TAUserInfo userInfo, Integer declarationTemplateId, OutputStream os) {
        try {
            ZipOutputStream zos = new ZipOutputStream(os);
            DeclarationTemplate dt = get(declarationTemplateId);
            dt.setCreateScript(getDeclarationTemplateScript(declarationTemplateId));
            doExportDeclarationTemplate("", dt, zos);
            zos.finish();
        } catch (Exception e) {
            throw new ServiceException("Не удалось экспортировать макет декларации с id = " + declarationTemplateId, e);
        }
    }

    private void doExportDeclarationTemplate(String path, DeclarationTemplate dt, ZipOutputStream zos) throws IOException, JAXBException {
        // Структура формы
        ZipEntry ze = new ZipEntry(path + CONTENT_FILE);
        zos.putNextEntry(ze);
        DeclarationTemplateContent dtc = new DeclarationTemplateContent();
        dtc.fillDeclarationTemplateContent(dt);
        for (DeclarationSubreportContent declarationSubreportContent : dtc.getSubreports()) {
            if (declarationSubreportContent.getBlobDataId() != null) {
                declarationSubreportContent.setFileName(blobDataService.get(declarationSubreportContent.getBlobDataId()).getName());
            }
        }
        JAXBContext jaxbContext = JAXBContext.newInstance(DeclarationTemplateContent.class);
        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        jaxbMarshaller.marshal(dtc, zos);
        zos.closeEntry();

        // Основной скрипт
        String dtScript = dt.getCreateScript();
        if (dtScript != null) {
            ze = new ZipEntry(path + SCRIPT_FILE);
            zos.putNextEntry(ze);
            zos.write(dtScript.getBytes(ENCODING));
            zos.closeEntry();
        }

        // Скрипты событий
        for (DeclarationTemplateEventScript eventScript : dt.getEventScripts()) {
            ze = new ZipEntry(path + eventScript.getEventId() + "_" + SCRIPT_FILE);
            zos.putNextEntry(ze);
            zos.write(eventScript.getScript().getBytes(ENCODING));
            zos.closeEntry();
        }

        // JRXML-файл
        putBlobIntoZip(path, REPORT_FILE, dt.getJrxmlBlobId(), zos);

        // JRXML-файлы спецотчетов
        for (DeclarationSubreport subreport : dt.getSubreports()) {
            putBlobIntoZip(path, subreport.getBlobDataId(), zos);
        }

        // XSD и XLSX файлы
        putBlobIntoZip(path, XSD, dt.getXsdId(), zos);
        for (DeclarationTemplateFile file : dt.getDeclarationTemplateFiles()) {
            putBlobIntoZip(path, file.getBlobDataId(), zos);
        }
    }

    @Override
    @PreAuthorize("hasRole('N_ROLE_CONF')")
    public void exportAllDeclarationTemplates(TAUserInfo userInfo, OutputStream os) {
        ZipOutputStream zos = new ZipOutputStream(os);
        try {
            List<DeclarationTemplate> allTemplates = listAll();
            for (DeclarationTemplate dt : allTemplates) {
                dt.setCreateScript(getDeclarationTemplateScript(dt.getId()));
                String folderTemplateName = String.format("%s" + File.separator + "%s" + File.separator,
                        "declarationType_" + dt.getType().getId(),
                        SIMPLE_DATE_FORMAT_YEAR.get().format(dt.getVersion()));
                ZipArchiveEntry ze = new ZipArchiveEntry(folderTemplateName);
                zos.putNextEntry(ze);
                doExportDeclarationTemplate(folderTemplateName, dt, zos);
                zos.closeEntry();
            }
        } catch (Exception e) {
            throw new ServiceException("Не удалось экспортировать макеты деклараций", e);
        } finally {
            IOUtils.closeQuietly(zos);
        }
    }

    /**
     * Добавляет содержимое блоба в архив
     *
     * @param path     путь к файлу внутри архива
     * @param fileName имя файла, которое будет ему присвоено в архиве. Если null - присваивается имя из блоба
     * @param blobId   идентификатор блоба из БД
     * @param zos      поток записи в архив
     */
    private void putBlobIntoZip(String path, String fileName, String blobId, ZipOutputStream zos) throws IOException {
        if (blobId != null) {
            BlobData blob = blobDataService.get(blobId);
            if (blob != null) {
                ZipEntry ze = new ZipEntry(path + (fileName != null ? fileName : blob.getName()));
                zos.putNextEntry(ze);
                IOUtils.copy(blob.getInputStream(), zos);
                zos.closeEntry();
            }
        }
    }

    private void putBlobIntoZip(String path, String blobId, ZipOutputStream zos) throws IOException {
        putBlobIntoZip(path, null, blobId, zos);
    }

    @Override
    @PreAuthorize("hasPermission(#declarationTemplateId, 'com.aplana.sbrf.taxaccounting.model.DeclarationTemplate', T(com.aplana.sbrf.taxaccounting.permissions.DeclarationTemplatePermission).UPDATE)")
    public ActionResult importDeclarationTemplate(TAUserInfo userInfo, int declarationTemplateId, InputStream fileData) {
        LOG.info(String.format("DeclarationTemplateServiceImpl.importDeclarationTemplate. declarationTemplateId: %s", declarationTemplateId));
        try {
            // Проверки перед выполнением импорта + блокировка макета
            checkLockedByAnotherUser(declarationTemplateId, userInfo);
            lock(declarationTemplateId, userInfo);

            Logger logger = new Logger();
            DeclarationTemplate declarationTemplate = doImportDeclarationTemplate(declarationTemplateId, fileData);
            Date endDate = getDTEndDate(declarationTemplateId);

            if (declarationTemplate.getStatus().equals(VersionedObjectStatus.NORMAL)) {
                // Проверка использования макета в формах
                isInUsed(declarationTemplateId,
                        declarationTemplate.getType().getId(),
                        declarationTemplate.getStatus(),
                        declarationTemplate.getVersion(),
                        endDate,
                        logger);

                if (logger.containsLevel(LogLevel.ERROR)) {
                    throw new ServiceLoggerException("Не пройдена проверка использования макета", logEntryService.save(logger.getEntries()));
                }
            }

            ActionResult result = new ActionResult();
            // Если найдены созданные отчеты - требуется запрос подтверждения для их удаления
            result.setSuccess(declarationTemplate.getJrxmlBlobId() == null || !checkExistingDataJrxml(declarationTemplateId, logger));
            // Сохраняем изменения в любом случае
            edit(declarationTemplate, endDate, logger, userInfo);
            result.setUuid(logEntryService.save(logger.getEntries()));
            return result;
        } finally {
            unlock(declarationTemplateId, userInfo);
            IOUtils.closeQuietly(fileData);
        }
    }

    @Override
    @PreAuthorize("hasPermission(#declarationTemplateId, 'com.aplana.sbrf.taxaccounting.model.DeclarationTemplate', T(com.aplana.sbrf.taxaccounting.permissions.DeclarationTemplatePermission).UPDATE)")
    public void deleteJrxmlReports(TAUserInfo userInfo, int declarationTemplateId) {
        LOG.info(String.format("DeclarationTemplateServiceImpl.deleteJrxmlReports. declarationTemplateId: %s", declarationTemplateId));
        declarationDataService.cleanBlobs(
                getDataIdsThatUseJrxml(declarationTemplateId),
                Arrays.asList(DeclarationDataReportType.EXCEL_DEC, DeclarationDataReportType.PDF_DEC, DeclarationDataReportType.JASPER_DEC));
        for (Long id : getLockDataIdsThatUseJrxml(declarationTemplateId)) {
            String keyPDF = declarationDataService.generateAsyncTaskKey(id, AsyncTaskType.PDF_DEC);
            String keyEXCEL = declarationDataService.generateAsyncTaskKey(id, AsyncTaskType.EXCEL_DEC);
            asyncManager.interruptTask(keyPDF, userInfo, TaskInterruptCause.DECLARATION_TEMPLATE_JRXML_CHANGE);
            asyncManager.interruptTask(keyEXCEL, userInfo, TaskInterruptCause.DECLARATION_TEMPLATE_JRXML_CHANGE);
        }
        deleteJrxml(declarationTemplateId);
    }

    private void deleteJrxml(int declarationTemplateId) {
        LOG.info(String.format("DeclarationTemplateServiceImpl.deleteJrxml. declarationTemplate: %s", declarationTemplateId));
        try {
            DeclarationTemplate template = get(declarationTemplateId);
            declarationTemplateDao.deleteJrxml(declarationTemplateId);
            blobDataService.delete(template.getJrxmlBlobId());
        } catch (DaoException e) {
            throw new ServiceException("Ошибка при удалении jrxml", e);
        }
    }

    /**
     * Выполняет непосредственно импорт файла в макет
     *
     * @param declarationTemplateId идентификатор макета
     * @param data                  содержимое архива
     */
    private DeclarationTemplate doImportDeclarationTemplate(int declarationTemplateId, InputStream data) {
        try {
            // Такая кодировка нужна для исправления бага IllegalArgumentException: MALFORMED
            ZipInputStream zis = new ZipInputStream(data, Charset.forName("CP866"));
            ZipEntry entry;
            DeclarationTemplate dt = SerializationUtils.clone(get(declarationTemplateId));
            dt.setJrxmlBlobId(null);
            dt.setCreateScript("");
            dt.setSubreports(new ArrayList<DeclarationSubreport>());
            dt.setEventScripts(new LinkedList<DeclarationTemplateEventScript>());
            Map<String, byte[]> subreports = new HashMap<>();
            DeclarationTemplateContent dtc = null;

            while ((entry = zis.getNextEntry()) != null) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                if (entry.getSize() == 0) {
                    throw new ServiceException("Файл " + entry.getName() + " не должен быть пуст");
                }
                if (entry.getName().endsWith(".groovy")) {
                    if (entry.getName().equals(SCRIPT_FILE)) {
                        // Основной скрипт
                        IOUtils.copy(zis, baos);
                        dt.setCreateScript(new String(baos.toByteArray(), 0, baos.toByteArray().length, Charset.forName("UTF-8")));
                    } else {
                        // Скрипт события
                        int eventId = Integer.parseInt(entry.getName().split("_")[0]);
                        DeclarationTemplateEventScript dtes = new DeclarationTemplateEventScript();
                        dtes.setDeclarationTemplateId(declarationTemplateId);
                        dtes.setEventId(eventId);
                        IOUtils.copy(zis, baos);
                        dtes.setScript(new String(baos.toByteArray(), 0, baos.toByteArray().length, Charset.forName("UTF-8")));
                        dt.getEventScripts().add(dtes);
                    }
                } else if (entry.getName().endsWith(".jrxml")) {
                    if (entry.getName().equals(REPORT_FILE)) {
                        // Основной JRXML-файл
                        IOUtils.copy(zis, baos);
                        String uuid = blobDataService.create(new ByteArrayInputStream(baos.toByteArray()), entry.getName());
                        dt.setJrxmlBlobId(uuid);
                    } else {
                        // JRXML-файл спецотчета - сохраняем для дальнейшей обработки
                        baos = new ByteArrayOutputStream();
                        IOUtils.copy(zis, baos);
                        subreports.put(entry.getName(), baos.toByteArray());
                    }
                } else if (entry.getName().endsWith(".xsd")) {
                    if (entry.getName().equals(XSD)) {
                        // Основной XSD-файл
                        IOUtils.copy(zis, baos);
                        blobDataService.save(dt.getXsdId(), new ByteArrayInputStream(baos.toByteArray()));
                    }
                } else if (entry.getName().equals(CONTENT_FILE)) {
                    IOUtils.copy(zis, baos);
                    JAXBContext jaxbContext = JAXBContext.newInstance(DeclarationTemplateContent.class);
                    Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
                    dtc = (DeclarationTemplateContent) jaxbUnmarshaller.unmarshal(
                            new InputStreamReader(new ByteArrayInputStream(baos.toByteArray()), ENCODING));
                } else {
                    // Другие прикрепленные файлы
                    for (DeclarationTemplateFile file : dt.getDeclarationTemplateFiles()) {
                        if (file.getFileName().equals(entry.getName())) {
                            IOUtils.copy(zis, baos);
                            String uuid = blobDataService.create(new ByteArrayInputStream(baos.toByteArray()), entry.getName());
                            file.setBlobDataId(uuid);
                        }
                    }
                }
            }
            if (dtc != null) {
                // Изменяем шаблоны спецотчетов
                if (dtc.getSubreports() != null) {
                    for (DeclarationSubreportContent declarationSubreportContent : dtc.getSubreports()) {
                        if (declarationSubreportContent.getFileName() != null && subreports.containsKey(declarationSubreportContent.getFileName())) {
                            String uuid = blobDataService.create(
                                    new ByteArrayInputStream(subreports.get(declarationSubreportContent.getFileName())), declarationSubreportContent.getFileName());
                            declarationSubreportContent.setBlobDataId(uuid);
                        }
                    }
                } else {
                    dtc.setSubreports(new ArrayList<DeclarationSubreportContent>());
                }
                dtc.fillDeclarationTemplate(dt);
            }
            return dt;
        } catch (Exception e) {
            throw new ServiceException("Не удалось импортировать шаблон", e);
        } finally {
            IOUtils.closeQuietly(data);
        }
    }

    private <T> boolean edit(T template, Date templateActualEndDate, Logger logger, TAUserInfo user) {
        return edit(template, null, templateActualEndDate, logger, user, null);
    }

    private <T> boolean edit(T template, List<DeclarationTemplateCheck> checks, Date templateActualEndDate, Logger logger, TAUserInfo user, Boolean force) {
        DeclarationTemplate declarationTemplate = (DeclarationTemplate) template;
        checkRole(user.getUser());
        declarationTemplateService.validateDeclarationTemplate(declarationTemplate, logger);
        checkError(logger);
        Date dbVersionBeginDate = declarationTemplateService.get(declarationTemplate.getId()).getVersion();
        Date dbVersionEndDate = declarationTemplateService.getDTEndDate(declarationTemplate.getId());
        if (dbVersionBeginDate.compareTo(declarationTemplate.getVersion()) != 0
                || (dbVersionEndDate != null && templateActualEndDate == null)
                || (dbVersionEndDate == null && templateActualEndDate != null)
                || (dbVersionEndDate != null && dbVersionEndDate.compareTo(templateActualEndDate) != 0)) {
            declarationTemplateVersionService.isIntersectionVersion(declarationTemplate.getId(), declarationTemplate.getType().getId(),
                    declarationTemplate.getStatus(), declarationTemplate.getVersion(), templateActualEndDate, logger, user);
            checkError(logger);
            //Выполенение шага 5.А.1.1
            Pair<Date, Date> beginRange = null;
            Pair<Date, Date> endRange = null;
            if (dbVersionBeginDate.compareTo(declarationTemplate.getVersion()) < 0) {
                Calendar c = Calendar.getInstance();
                c.setTime(declarationTemplate.getVersion());
                c.add(Calendar.DATE, -1);
                beginRange = new Pair<>(dbVersionBeginDate, c.getTime());
            }
            if (
                    (dbVersionEndDate == null && templateActualEndDate != null)
                            ||
                            (dbVersionEndDate != null && templateActualEndDate != null && dbVersionEndDate.compareTo(templateActualEndDate) > 0)) {
                Calendar c = Calendar.getInstance();
                c.setTime(templateActualEndDate);
                c.add(Calendar.DATE, 1);
                endRange = new Pair<>(c.getTime(), dbVersionEndDate);
            }
            declarationTemplateVersionService.checkDestinationsSources(declarationTemplate.getType().getId(), beginRange, endRange, logger);
            checkError(logger);
            declarationDataService.findDDIdsByRangeInReportPeriod(declarationTemplate.getId(), declarationTemplate.getVersion(), templateActualEndDate, logger);
            checkError(logger);
        }

        if ((force == null || !force) && declarationTemplate.getStatus().equals(VersionedObjectStatus.NORMAL)) {
            boolean isUsedVersion = declarationTemplateVersionService.isUsedVersion(declarationTemplate.getId(), declarationTemplate.getType().getId(),
                    declarationTemplate.getStatus(), declarationTemplate.getVersion(), templateActualEndDate, logger);
            if (force == null)
                checkError(logger);
            else {
                if (isUsedVersion)
                    return false;
            }
        }

        List<Long> ddIds = declarationDataService.getFormDataListInActualPeriodByTemplate(declarationTemplate.getId(), declarationTemplate.getVersion());
        for (long declarationId : ddIds) {
            // Отменяем задачи формирования спец отчетов/удаляем спец отчеты
            //declarationDataService.interruptAsyncTask(declarationId, user, AsyncTaskType.UPDATE_TEMPLATE_DEC, TaskInterruptCause.DECLARATION_TEMPLATE_UPDATE);
        }

        declarationTemplateService.save(declarationTemplate, user);
        logger.info("Изменения сохранены");
        int id = declarationTemplate.getId();

        List<DeclarationTemplateCheck> oldChecks = declarationTemplateService.getChecks(declarationTemplate.getType().getId(), declarationTemplate.getId());
        if (checks != null && !oldChecks.containsAll(checks) && !checks.containsAll(oldChecks)) {
            declarationTemplateService.updateChecks(checks, declarationTemplate.getId());
            auditService.add(FormDataEvent.TEMPLATE_MODIFIED, user, declarationTemplate.getVersion(),
                    declarationTemplateService.getDTEndDate(id), null, declarationTemplate.getName(), "Обновлена информация о фатальности проверок НФ", null);
        }

        auditService.add(FormDataEvent.TEMPLATE_MODIFIED, user, declarationTemplate.getVersion(),
                declarationTemplateService.getDTEndDate(id), null, declarationTemplate.getName(), null, null);
        logging(id, FormDataEvent.TEMPLATE_MODIFIED, user.getUser());

        return true;
    }

    private boolean setStatusTemplate(int templateId, Logger logger, TAUserInfo user, boolean force) {
        DeclarationTemplate declarationTemplate = declarationTemplateService.get(templateId);
        checkRole(user.getUser());
        if (declarationTemplate.getStatus() == VersionedObjectStatus.NORMAL) {
            declarationTemplateVersionService.isUsedVersion(declarationTemplate.getId(), declarationTemplate.getType().getId(),
                    declarationTemplate.getStatus(), declarationTemplate.getVersion(), null, logger);
            if (!force && logger.containsLevel(LogLevel.ERROR)) return false;
            declarationTemplate.setStatus(VersionedObjectStatus.DRAFT);
            declarationTemplateService.updateVersionStatus(VersionedObjectStatus.DRAFT, templateId);
            logging(templateId, FormDataEvent.TEMPLATE_DEACTIVATED, user.getUser());
        } else {
            declarationTemplate.setStatus(VersionedObjectStatus.NORMAL);
            declarationTemplateService.updateVersionStatus(VersionedObjectStatus.NORMAL, templateId);
            logging(templateId, FormDataEvent.TEMPLATE_ACTIVATED, user.getUser());
        }
        return true;
    }

    private void isInUsed(int templateId, int typeId, VersionedObjectStatus status, Date versionActualDateStart, Date versionActualDateEnd, Logger logger) {
        declarationTemplateVersionService.isUsedVersion(templateId, typeId, status, versionActualDateStart, versionActualDateEnd, logger);
    }

    private void logging(int id, FormDataEvent event, TAUser user) {
        TemplateChanges changes = new TemplateChanges();
        changes.setEvent(event);
        changes.setEventDate(new Date());
        changes.setDeclarationTemplateId(id);
        changes.setAuthor(user);
        templateChangesService.save(changes);
    }

    private void checkError(Logger logger) {
        if (logger.containsLevel(LogLevel.ERROR))
            throw new ServiceLoggerException("Версия макета не сохранена. Обнаружены фатальные ошибки!", logEntryService.save(logger.getEntries()));
    }

    private void checkRole(TAUser user) {
        TaxType taxType = TaxType.NDFL;
        if (!user.hasRole(taxType, TARole.N_ROLE_CONF)) {
            throw new ServiceException("Нет прав доступа к данному виду налогу \"%s\"!", taxType.getName());
        }
    }
}
