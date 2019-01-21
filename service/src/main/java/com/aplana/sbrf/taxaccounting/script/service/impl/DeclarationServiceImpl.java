package com.aplana.sbrf.taxaccounting.script.service.impl;

import com.aplana.sbrf.taxaccounting.dao.DeclarationDataDao;
import com.aplana.sbrf.taxaccounting.dao.DeclarationDataFileDao;
import com.aplana.sbrf.taxaccounting.dao.DeclarationTemplateDao;
import com.aplana.sbrf.taxaccounting.dao.api.DeclarationTypeDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookKnfType;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.script.service.DeclarationService;
import com.aplana.sbrf.taxaccounting.script.service.util.ScriptUtils;
import com.aplana.sbrf.taxaccounting.service.*;
import groovy.lang.Closure;
import net.sf.jasperreports.engine.JasperPrint;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.util.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.ZipInputStream;


@Service("declarationService")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class DeclarationServiceImpl implements DeclarationService {

    private static final Log LOG = LogFactory.getLog(DeclarationService.class);

    @Autowired
    private DeclarationDataDao declarationDataDao;
    @Autowired
    private DeclarationDataService declarationDataService;
    @Autowired
    private DeclarationTemplateDao declarationTemplateDao;
    @Autowired
    private SourceService sourceService;
    @Autowired
    private DeclarationDataSearchService declarationDataSearchService;
    @Autowired
    private TAUserService taUserService;
    @Autowired
    private DeclarationTypeDao declarationTypeDao;
    @Autowired
    private LoadDeclarationDataService loadDeclarationDataService;
    @Autowired
    private ReportService reportService;
    @Autowired
    private DeclarationDataFileDao declarationDataFileDao;
    @Autowired
    private ConfigurationService configurationService;
    @Autowired
    private LockDataService lockDataService;
    @Autowired
    private LogBusinessService logBusinessService;

    @Override
    public DeclarationData getDeclarationData(long declarationDataId) {
        return declarationDataDao.get(declarationDataId);
    }

    @Override
    public List<String> getDeclarationDataKppList(long declarationDataId) {
        return declarationDataDao.getDeclarationDataKppList(declarationDataId);
    }

    @Override
    public List<Long> getDeclarationDataPersonIds(long declarationDataId) {
        return declarationDataDao.getDeclarationDataPersonIds(declarationDataId);
    }

    @Override
    public List<DeclarationData> findAllByTypeIdAndPeriodId(int declarationTypeId, int departmentReportPeriodId) {
        return declarationDataDao.findAllByTypeIdAndPeriodId(declarationTypeId, departmentReportPeriodId);
    }

    @Override
    public DeclarationData findKnfByKnfTypeAndPeriodId(RefBookKnfType knfType, int departmentReportPeriodId) {
        return declarationDataDao.findKnfByKnfTypeAndPeriodId(knfType, departmentReportPeriodId);
    }

    @Override
    public DeclarationData find(int declarationTypeId, int departmentReportPeriodId, String kpp, String oktmo, String taxOrganCode, Long asnuId, String fileName) {
        return declarationDataDao.find(declarationTypeId, departmentReportPeriodId, kpp, oktmo, taxOrganCode, asnuId, fileName);
    }

    @Override
    public List<DeclarationData> findAllDeclarationData(int declarationTypeId, int departmentId, int reportPeriodId) {
        return declarationDataDao.findAllDeclarationData(declarationTypeId, departmentId, reportPeriodId);
    }

    @Override
    public String getXmlData(long declarationDataId, TAUserInfo userInfo) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ZipInputStream zipXmlIn = getZipInputStream(declarationDataId, userInfo);
        if (zipXmlIn != null) {
            try {
                zipXmlIn.getNextEntry();
                IOUtils.copy(zipXmlIn, byteArrayOutputStream);
            } catch (IOException e) {
                throw new ServiceException("Не удалось извлечь xml для скрипта.", e);
            } finally {
                IOUtils.closeQuietly(zipXmlIn);
            }
        }
        return new String(byteArrayOutputStream.toByteArray(), Charset.forName("cp1251"));
    }

    @Override
    public ZipInputStream getXmlStream(long declarationDataId, TAUserInfo userInfo) {
        ZipInputStream zipXmlIn = getZipInputStream(declarationDataId, userInfo);
        if (zipXmlIn != null) {
            try {
                zipXmlIn.getNextEntry();
                return zipXmlIn;
            } catch (IOException e) {
                throw new ServiceException("Не удалось получить поток xml для скрипта.", e);
            }
        }
        return null;
    }

    private ZipInputStream getZipInputStream(long declarationDataId, TAUserInfo userInfo) {
        InputStream zipXml = declarationDataService.getXmlDataAsStream(declarationDataId, userInfo);
        if (zipXml != null) {
            return new ZipInputStream(zipXml);
        }
        return null;
    }

    @Override
    public DeclarationTemplate getTemplate(int declarationTemplateId) {
        return declarationTemplateDao.get(declarationTemplateId);
    }

    @Override
    public DeclarationType getTemplateType(int declarationTypeId) {
        return declarationTypeDao.get(declarationTypeId);
    }

    @Override
    public JasperPrint createJasperReport(InputStream jrxmlTemplate, Map<String, Object> parameters, Closure xmlBuilder) {
        ByteArrayInputStream xmlData = xmlBuilder == null ? null : generateXmlData(xmlBuilder);
        return declarationDataService.createJasperReport(xmlData, jrxmlTemplate, parameters);
    }

    /**
     * Метод записывает xml данные в буфер формирует поток на чтение
     *
     * @param xmlBuilder замыкание в котором описано формирование xml
     * @return поток xml данных отчета
     */
    private ByteArrayInputStream generateXmlData(Closure xmlBuilder) {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(byteStream, StandardCharsets.UTF_8);
        //вызываем замыкание, в котором описано формирование xml
        xmlBuilder.call(writer);
        byte[] buffer = byteStream.toByteArray();
        return new ByteArrayInputStream(buffer);
    }


    @Override
    public JasperPrint createJasperReport(InputStream jrxmlTemplate, Map<String, Object> parameters) {
        return declarationDataService.createJasperReport(null, jrxmlTemplate, parameters);
    }

    @Override
    public void exportXLSX(JasperPrint jasperPrint, OutputStream data) {
        declarationDataService.exportXLSX(jasperPrint, data);
    }

    @Override
    public Long create(DeclarationData newDeclaration, DepartmentReportPeriod departmentReportPeriod, Logger logger, TAUserInfo userInfo, boolean writeAudit) {
        return declarationDataService.create(newDeclaration, departmentReportPeriod, logger, userInfo, writeAudit);
    }

    @Override
    public void deleteReport(long declarationDataId) {
        reportService.deleteAllByDeclarationId(declarationDataId);
    }

    @Override
    public void validateDeclaration(Logger logger, File xmlFile, String fileName, String xsdBlobDataId) {
        // TODO: Отправка null выглядит неправильно. Нам чисто везёт, что там алгоритм не заходит в ветки, способные вызвать NPE.
        declarationDataService.validateDeclaration(null, logger, xmlFile, fileName, xsdBlobDataId);
    }

    @Override
    public int getActiveDeclarationTemplateId(int declarationTypeId, int reportPeriodId) {
        return declarationTemplateDao.getActiveDeclarationTemplateId(declarationTypeId, reportPeriodId);
    }

    @Override
    public String getDeclarationTemplateScript(int declarationTemplateId) {
        return declarationTemplateDao.getDeclarationTemplateScript(declarationTemplateId);
    }

    @Override
    public List<DepartmentDeclarationType> getDDTByDepartment(int departmentId, TaxType taxType, Date periodStart, Date periodEnd) {
        return sourceService.getDDTByDepartment(departmentId, taxType, periodStart, periodEnd);
    }

    @Override
    public List<Long> getDeclarationIds(DeclarationDataFilter declarationFilter, DeclarationDataSearchOrdering ordering, boolean asc) {
        return declarationDataSearchService.getDeclarationIds(declarationFilter, ordering, asc);
    }

    @Override
    public void importXmlTransportFile(File xmlTransportFile, String xmlFileName, DeclarationData declarationData, TAUserInfo userInfo, Logger logger) {
        loadDeclarationDataService.importXmlTransportFile(xmlTransportFile, xmlFileName, declarationData, userInfo, logger);
    }

    @Override
    public List<DeclarationData> findDeclarationDataByFileNameAndFileType(String fileName, Long fileTypeId) {
        return declarationDataDao.findDeclarationDataByFileNameAndFileType(fileName, fileTypeId);
    }

    @Override
    public void saveFile(DeclarationDataFile file) {
        declarationDataFileDao.create(file);
    }

    @Override
    public TAUserInfo getSystemUserInfo() {
        return taUserService.getSystemUserInfo();
    }

    @Override
    public DeclarationDataFile findFileWithMaxWeight(Long declarationDataId) {
        return declarationDataFileDao.fetchWithMaxWeight(declarationDataId);
    }

    @Override
    public void setDocStateId(long declarationDataId, Long docStateId) {
        declarationDataDao.setDocStateId(declarationDataId, docStateId);
    }

    @Override
    public ConfigurationParamModel getAllConfig(TAUserInfo userInfo) {
        return configurationService.getCommonConfig(userInfo);
    }

    @Override
    public void createPdfReport(Logger logger, DeclarationData declarationData, TAUserInfo userInfo) {
        deleteReport(declarationData.getId(), Arrays.asList(DeclarationDataReportType.PDF_DEC, DeclarationDataReportType.JASPER_DEC));
        declarationDataService.setPdfDataBlobs(logger, declarationData, userInfo, new LockStateLogger() {
            @Override
            public void updateState(AsyncTaskState state) {
                // ничего не делаем
            }
        });
    }

    // Удаляет отчеты заданных типов
    private void deleteReport(long declarationDataId, List<DeclarationDataReportType> declarationDataReportTypeList) {
        reportService.deleteDec(Collections.singletonList(declarationDataId), declarationDataReportTypeList);
    }


    @Override
    public List<DeclarationDataFile> findFilesWithSpecificType(Long declarationDataId, String fileTypeName) {
        return declarationDataFileDao.fetchByAttachFileTypeName(declarationDataId, fileTypeName);
    }

    @Override
    public List<DeclarationData> find(int declarationTemplate, int departmentReportPeriodId, String taxOrganCode, String kpp, String oktmo) {
        return declarationDataDao.find(declarationTemplate, departmentReportPeriodId, taxOrganCode, kpp, oktmo);
    }

    @Override
    public List<Pair<String, String>> findNotPresentedPairKppOktmo(Long declarationDataId) {
        return declarationDataDao.findNotPresentedPairKppOktmo(declarationDataId);
    }

    @Override
    public Map<DeclarationDataReportType, LockData> getLockTaskType(long declarationDataId) {
        return declarationDataService.getLockTaskType(declarationDataId);
    }

    @Override
    public String generateAsyncTaskKey(long declarationDataId, DeclarationDataReportType type) {
        return declarationDataService.generateAsyncTaskKey(declarationDataId, type);
    }

    @Override
    @Transactional
    public LockData createDeleteLock(long declarationDataId, TAUserInfo userInfo) {
        String deleteLockKey = generateAsyncTaskKey(declarationDataId, DeclarationDataReportType.DELETE_DEC);
        if (declarationDataDao.existDeclarationData(declarationDataId)) {
            LockData lockData = lockDataService.lock(deleteLockKey, userInfo.getUser().getId(),
                    declarationDataService.getDeclarationFullName(declarationDataId, DeclarationDataReportType.DELETE_DEC));
            if (lockData != null) {
                return null;
            }
            return lockDataService.findLock(deleteLockKey);
        }
        return null;
    }

    @Override
    @Transactional
    public List<Pair<Long, DeclarationDataReportType>> deleteForms(int declarationTypeId, int departmentReportPeriodId, List<Pair<String, String>> kppOktmoPairs, Logger logger, TAUserInfo userInfo) {
        List<Pair<Long, DeclarationDataReportType>> result = new ArrayList<>();

        // Список ранее созданных отчетных форм
        List<DeclarationData> deletedDeclarationDataList = declarationDataDao.findAllByTypeIdAndPeriodIdAndKppOktmoPairs(declarationTypeId, departmentReportPeriodId, kppOktmoPairs);
        // Список блокировок на удаление форм
        List<LockData> lockList = new ArrayList<>();
        try {
            for (DeclarationData deletedDeclarationData : deletedDeclarationDataList) {
                Map<DeclarationDataReportType, LockData> taskTypeMap = getLockTaskType(deletedDeclarationData.getId());
                if (!taskTypeMap.isEmpty()) {
                    for (DeclarationDataReportType declarationDataReportType : taskTypeMap.keySet()) {
                        result.add(new Pair<>(deletedDeclarationData.getId(), declarationDataReportType));
                    }
                } else {
                    LockData deleteLock = createDeleteLock(deletedDeclarationData.getId(), userInfo);
                    if (deleteLock != null) {
                        lockList.add(deleteLock);
                    } else {
                        result.add(new Pair<>(deletedDeclarationData.getId(), DeclarationDataReportType.DELETE_DEC));
                    }
                }
            }
            if (result.size() == 0) {
                for (DeclarationData deletedDeclarationData : deletedDeclarationDataList) {
                    ScriptUtils.checkInterrupted();
                    try {
                        delete(deletedDeclarationData.getId(), userInfo);
                    } catch (ServiceException e) {
                        LOG.error(e);
                        result.add(new Pair<>(deletedDeclarationData.getId(), DeclarationDataReportType.DELETE_DEC));
                        break;
                    }
                }
            }
        } finally {
            // удаляем блокировки
            for (LockData lockData : lockList) {
                lockDataService.unlock(lockData.getKey(), lockData.getUserId());
            }
        }
        return result;
    }

    private void delete(long declarationDataId, TAUserInfo userInfo) {
        if (declarationDataDao.existDeclarationData(declarationDataId)) {
            declarationDataDao.setStatus(declarationDataId, State.CREATED);
            declarationDataService.deleteSync(declarationDataId, userInfo, false);
        }
    }


    @Override
    public void check(Logger logger, long declarationDataId, TAUserInfo userInfo, LockStateLogger lockStateLogger) {
        declarationDataService.check(logger, declarationDataId, userInfo, lockStateLogger);
    }

    @Override
    public String getDeclarationFullName(long declarationId, DeclarationDataReportType ddReportType, String... args) {
        return declarationDataService.getDeclarationFullName(declarationId, ddReportType);
    }

    @Override
    public boolean isCheckFatal(DeclarationCheckCode code, int templateId) {
        return declarationTemplateDao.isCheckFatal(code, templateId);
    }

    @Override
    public Date getDeclarationDataCreationDate(Long declarationDataId) {
        return logBusinessService.getFormCreationDate(declarationDataId);
    }

    @Override
    public List<Long> findApplication2DeclarationDataId(int reportYear) {
        return declarationDataDao.findApplication2DeclarationDataId(reportYear);
    }

    @Override
    public String findXsdIdByTemplateId(Integer declarationTemplateId) {
        return declarationTemplateDao.findXsdIdByTemplateId(declarationTemplateId);
    }
}
