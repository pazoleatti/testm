package com.aplana.sbrf.taxaccounting.service.script.impl;

import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.core.api.LockStateLogger;
import com.aplana.sbrf.taxaccounting.dao.DeclarationDataDao;
import com.aplana.sbrf.taxaccounting.dao.DeclarationDataFileDao;
import com.aplana.sbrf.taxaccounting.dao.DeclarationTemplateDao;
import com.aplana.sbrf.taxaccounting.dao.api.DeclarationTypeDao;
import com.aplana.sbrf.taxaccounting.dao.api.DepartmentReportPeriodDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.service.*;
import com.aplana.sbrf.taxaccounting.service.api.ConfigurationService;
import com.aplana.sbrf.taxaccounting.service.script.DeclarationService;
import com.aplana.sbrf.taxaccounting.service.script.util.ScriptUtils;
import com.aplana.sbrf.taxaccounting.service.shared.ScriptComponentContext;
import com.aplana.sbrf.taxaccounting.service.shared.ScriptComponentContextHolder;
import groovy.lang.Closure;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.util.JRSwapFile;
import org.apache.commons.codec.CharEncoding;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.util.IOUtils;
import org.joda.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.*;
import java.util.*;
import java.util.zip.ZipInputStream;

/*
 * author auldanov
 */
@Service("declarationService")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class DeclarationServiceImpl implements DeclarationService, ScriptComponentContextHolder {

    private static final Log LOG = LogFactory.getLog(DeclarationService.class);

    private static final String CHECK_UNIQUE_ERROR = "Налоговая форма с заданными параметрами уже существует!";
    private static final String CHECK_UNIQUE_NOTIFICATION_ERROR = "Уведомление с заданными параметрами уже существует!";

    // Тип налога -> ID справочника с параметрами подразделения
    private static final Map<TaxType, Long> TAX_TYPE_TO_REF_BOOK_MAP = new HashMap<TaxType, Long>() {
        {
            put(TaxType.NDFL, RefBook.Id.NDFL.getId());
        }
    };

    private ScriptComponentContext context;

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
    private DepartmentReportPeriodDao departmentReportPeriodDao;
    @Autowired
    private LoadDeclarationDataService loadDeclarationDataService;
    @Autowired
    private ReportService reportService;
    @Autowired
    private DeclarationDataFileDao declarationDataFileDao;
    @Autowired
    private ConfigurationService configurationService;
    @Autowired
    private ValidateXMLService validateXMLService;
    @Autowired
    private LockDataService lockDataService;

    @Override
    public DeclarationData getDeclarationData(long declarationDataId) {
        return declarationDataDao.get(declarationDataId);
    }

    @Override
    public List<DeclarationData> getDeclarationData(List<Long> declarationDataIds) {
        return declarationDataDao.get(declarationDataIds);
    }

    @Override
    public List<DeclarationData> find(int declarationTypeId, int departmentReportPeriodId) {
        return declarationDataDao.find(declarationTypeId, departmentReportPeriodId);
    }

    @Override
    public DeclarationData find(int declarationTypeId, int departmentReportPeriodId, String kpp, String oktmo, String taxOrganCode, Long asnuId, String fileName) {
        return declarationDataDao.find(declarationTypeId, departmentReportPeriodId, kpp, oktmo, taxOrganCode, asnuId, fileName);
    }

    @Override
    public List<DeclarationData> find(String fileName) {
        return declarationDataDao.find(fileName);
    }

    @Override
    public DeclarationData getLast(int declarationTypeId, int departmentId, int reportPeriodId) {
        return declarationDataDao.getLast(declarationTypeId, departmentId, reportPeriodId);
    }

    @Override
    public List<DeclarationData> findAllDeclarationData(int declarationTypeId, int departmentId, int reportPeriodId) {
        return declarationDataDao.findAllDeclarationData(declarationTypeId, departmentId, reportPeriodId);
    }

    @Override
    public String getXmlData(long declarationDataId) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ZipInputStream zipXmlIn = getZipInputStream(declarationDataId);
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
        return new String(byteArrayOutputStream.toByteArray());
    }

    @Override
    public ZipInputStream getXmlStream(long declarationDataId) {
        ZipInputStream zipXmlIn = getZipInputStream(declarationDataId);
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

    @Override
    public XMLStreamReader getXmlStreamReader(long declarationDataId) {
        ZipInputStream zipXmlIn = getZipInputStream(declarationDataId);
        if (zipXmlIn != null) {
            try {
                zipXmlIn.getNextEntry();
                return XMLInputFactory.newInstance().createXMLStreamReader(zipXmlIn);
            } catch (IOException e) {
                throw new ServiceException("Не удалось получить поток xml для скрипта.", e);
            } catch (XMLStreamException e) {
                throw new ServiceException("Не удалось получить поток xml для скрипта.", e);
            }
        }
        return null;
    }

    private ZipInputStream getZipInputStream(long declarationDataId) {
        InputStream zipXml = declarationDataService.getXmlDataAsStream(declarationDataId, taUserService.getSystemUserInfo());
        if (zipXml != null) {
            return new ZipInputStream(zipXml);
        }
        return null;
    }

    @Override
    public boolean checkExistDeclarationsInPeriod(int declarationTypeId, int departmentReportPeriodId) {
        DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodDao.get(departmentReportPeriodId);
        DeclarationDataFilter declarationFilter = new DeclarationDataFilter();
        // фильтр
        declarationFilter.setDeclarationTypeIds(Arrays.asList((long) declarationTypeId));
        declarationFilter.setReportPeriodIds(Collections.singletonList(departmentReportPeriod.getReportPeriod().getId()));
        declarationFilter.setCorrectionDate(departmentReportPeriod.getCorrectionDate());
        declarationFilter.setCorrectionTag(departmentReportPeriod.getCorrectionDate() != null);
        declarationFilter.setTaxType(TaxType.INCOME);

        // пейджинг
        declarationFilter.setSearchOrdering(DeclarationDataSearchOrdering.ID);
        declarationFilter.setStartIndex(0);
        declarationFilter.setCountOfRecords(1);

        PagingResult<DeclarationDataSearchResultItem> result = declarationDataSearchService.search(declarationFilter);
        return (result != null && !result.isEmpty());
    }

    @Override
    public void setScriptComponentContext(ScriptComponentContext context) {
        this.context = context;
    }

    /*@Override
    public boolean checkUnique(DeclarationData declarationData, Logger logger) {
        DeclarationTemplate template = declarationTemplateDao.get(declarationData.getDeclarationTemplateId());
        DeclarationData existingDeclarationData = declarationDataDao.find(template.getType().getId(),
                declarationData.getDepartmentReportPeriodId(), declarationData.getKpp(), declarationData.getOktmo(), declarationData.getTaxOrganCode(),
                declarationData.getAsnuId(), declarationData.getFileName());
        // форма найдена
        if (existingDeclarationData != null) {
            logger.error(template.getType().getTaxType() == TaxType.DEAL ? CHECK_UNIQUE_NOTIFICATION_ERROR : CHECK_UNIQUE_ERROR);
            return false;
        }
        return true;
    }*/

    @Override
    public String getXmlDataFileName(long declarationDataId) {
        String fileName = declarationDataService.getXmlDataFileName(declarationDataId, taUserService.getSystemUserInfo());
        if (fileName != null) {
            return fileName.replace(".zip", ".xml");
        }
        return null;
    }

    @Override
    public List<Relation> getDeclarationSourcesInfo(DeclarationData declaration, boolean light, boolean excludeIfNotExist, State stateRestriction, TAUserInfo userInfo, Logger logger) {
        return sourceService.getDeclarationSourcesInfo(declaration, light, excludeIfNotExist, stateRestriction, userInfo, logger);
    }

    @Override
    public List<Integer> getDeclarationTypeIds(TaxType taxType) {
        if (taxType == null) {
            return new ArrayList<Integer>();
        }
        TemplateFilter filter = new TemplateFilter();
        filter.setTaxType(taxType);
        return declarationTypeDao.getByFilter(filter);
    }

    @Override
    public DeclarationType getType(int declarationTypeId) {
        return declarationTypeDao.get(declarationTypeId);
    }

    @Override
    public DeclarationType getTypeByTemplateId(int declarationTemplateId) {
        return declarationTypeDao.getTypeByTemplateId(declarationTemplateId);
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
    public JasperPrint createJasperReport(InputStream xmlIn, String jrxml, JRSwapFile jrSwapFile, Map<String, Object> params) {
        return declarationDataService.createJasperReport(xmlIn, jrxml, jrSwapFile, params);
    }

    @Override
    public JasperPrint createJasperReport(InputStream jrxmlTemplate, Map<String, Object> parameters, Closure xmlBuilder) {
        ByteArrayInputStream xmlData = xmlBuilder == null ? null : generateXmlData(xmlBuilder);
        return declarationDataService.createJasperReport(xmlData, jrxmlTemplate, parameters);
    }

    @Override
    public JasperPrint createJasperReport(InputStream jrxmlTemplate, Map<String, Object> parameters, InputStream inputStream) {
        return declarationDataService.createJasperReport(inputStream, jrxmlTemplate, parameters);
    }

    @Override
    public JasperPrint createJasperReport(InputStream jrxmlTemplate, Map<String, Object> parameters) {
        return declarationDataService.createJasperReport(null, jrxmlTemplate, parameters);
    }

    @Override
    public ByteArrayInputStream generateXmlData(Closure xmlBuilder) {
        try {
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            OutputStreamWriter writer = new OutputStreamWriter(byteStream, CharEncoding.UTF_8);
            //вызываем замыкание, в котором описано формирование xml
            xmlBuilder.call(writer);
            byte[] buffer = byteStream.toByteArray();
            ByteArrayInputStream inputStream = new ByteArrayInputStream(buffer);
            return inputStream;
        } catch (UnsupportedEncodingException e) {
            throw new ServiceException(e.getMessage(), e);
        }
    }

    @Override
    public void exportXLSX(JasperPrint jasperPrint, OutputStream data) {
        declarationDataService.exportXLSX(jasperPrint, data);
    }

    @Override
    public void exportPDF(JasperPrint jasperPrint, OutputStream data) {
        declarationDataService.exportPDF(jasperPrint, data);
    }

    @Override
    public Long create(Logger logger, int declarationTemplateId, TAUserInfo userInfo,
                       DepartmentReportPeriod departmentReportPeriod, String taxOrganCode, String taxOrganKpp, String oktmo, Long asunId, String fileName, String note) {
        return declarationDataService.create(logger, declarationTemplateId, userInfo, departmentReportPeriod, taxOrganCode, taxOrganKpp, oktmo, asunId, fileName, note);
    }

    @Override
    public void delete(long declarationDataId, TAUserInfo userInfo) {
        if (declarationDataDao.existDeclarationData(declarationDataId)) {
            declarationDataDao.setStatus(declarationDataId, State.CREATED);
            declarationDataService.delete(declarationDataId, userInfo, false);
        }
    }


    @Override
    public void deleteReport(long declarationDataId) {
        reportService.deleteDec(declarationDataId);
    }

    @Override
    public void deleteReport(long declarationDataId, List<DeclarationDataReportType> declarationDataReportTypeList) {
        reportService.deleteDec(Arrays.asList(declarationDataId), declarationDataReportTypeList);
    }

    @Override
    public void validateDeclaration(DeclarationData declarationData, TAUserInfo userInfo, Logger logger, File dataFile) {
        validateDeclaration(declarationData, userInfo, logger, dataFile, null, null);
    }

    @Override
    public void validateDeclaration(DeclarationData declarationData, TAUserInfo userInfo, Logger logger, File dataFile, String fileName) {
        validateDeclaration(declarationData, userInfo, logger, dataFile, fileName, null);
    }

    @Override
    public void validateDeclaration(TAUserInfo userInfo, Logger logger, File xmlFile, String fileName, String xsdBlobDataId) {
        validateDeclaration(null, userInfo, logger, xmlFile, fileName, xsdBlobDataId);
    }

    @Override
    public void validateDeclaration(DeclarationData declarationData, TAUserInfo userInfo, Logger logger, File dataFile, String fileName, String xsdBlobDataId) {
        declarationDataService.validateDeclaration(userInfo, declarationData, logger, true, FormDataEvent.IMPORT_TRANSPORT_FILE, dataFile, fileName, xsdBlobDataId, new LockStateLogger() {
            @Override
            public void updateState(String state) {
                // ничего не делаем
            }
        });
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
    public void importDeclarationData(Logger logger, TAUserInfo userInfo, DeclarationData declarationData, InputStream inputStream, String fileName, File dataFile, AttachFileType attachFileType, LocalDateTime createDateFile) {
        loadDeclarationDataService.importDeclarationData(logger, userInfo, declarationData, inputStream, fileName, dataFile, attachFileType, createDateFile);
    }

    @Override
    public DeclarationData findDeclarationDataByKppOktmoOfNdflPersonIncomes(int declarationTypeId, int departmentReportPeriodId, int departmentId, int reportPeriodId, String kpp, String oktmo) {
        return declarationDataDao.findDeclarationDataByKppOktmoOfNdflPersonIncomes(declarationTypeId, departmentReportPeriodId, departmentId, reportPeriodId, kpp, oktmo);
    }

    @Override
    public List<DeclarationData> findDeclarationDataByFileNameAndFileType(String fileName, Long fileTypeId) {
        return declarationDataDao.findDeclarationDataByFileNameAndFileType(fileName, fileTypeId);
    }

    @Override
    public void saveFile(DeclarationDataFile file) {
        declarationDataFileDao.saveFile(file);
    }

    @Override
    public TAUserInfo getSystemUserInfo() {
        return taUserService.getSystemUserInfo();
    }

    @Override
    public DeclarationDataFile findFileWithMaxWeight(Long declarationDataId) {
        return declarationDataFileDao.findFileWithMaxWeight(declarationDataId);
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
            public void updateState(String state) {
                // ничего не делаем
            }
        });
    }

    @Override
    public List<DeclarationDataFile> findFilesWithSpecificType(Long declarationDataId, String fileTypeName) {
        return declarationDataFileDao.findFilesWithSpecificType(declarationDataId, fileTypeName);
    }

    @Override
    public List<Integer> findDeclarationDataIdByTypeStatusReportPeriod(Integer reportPeriodId, Long ndflId,
                                                                       Integer declarationTypeId, Integer departmentType,
                                                                       Boolean departmentReportPeriodStatus, Integer declarationState) {
        return declarationDataDao.findDeclarationDataIdByTypeStatusReportPeriod(reportPeriodId, ndflId, declarationTypeId,
                departmentType, departmentReportPeriodStatus, declarationState);
    }


    @Override
    public List<DeclarationData> findAllActive(int declarationTypeId, int reportPeriodId) {
        return declarationDataDao.findAllActive(declarationTypeId, reportPeriodId);
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
    public LockData createDeleteLock(long declarationDataId, TAUserInfo userInfo) {
        String deleteLockKey = generateAsyncTaskKey(declarationDataId, DeclarationDataReportType.DELETE_DEC);
        LockData lockData = lockDataService.lock(deleteLockKey, userInfo.getUser().getId(),
                declarationDataService.getDeclarationFullName(declarationDataId, DeclarationDataReportType.DELETE_DEC));
        if (lockData != null) {
            return null;
        }
        return lockDataService.getLock(deleteLockKey);
    }

    @Override
    public String getTaskName(DeclarationDataReportType ddReportType) {
        return declarationDataService.getTaskName(ddReportType, TaxType.NDFL);
    }


    @Override
    public List<Pair<Long, DeclarationDataReportType>> deleteForms(int declarationTypeId, int departmentReportPeriodId, Logger logger, TAUserInfo userInfo) {
        List<Pair<Long, DeclarationDataReportType>> result = new ArrayList<Pair<Long, DeclarationDataReportType>>();

        // Список ранее созданных отчетных форм
        List<DeclarationData> deletedDeclarationDataList = find(declarationTypeId, departmentReportPeriodId);
        // Список блокировок на удаление форм
        List<LockData> lockList = new ArrayList<LockData>();
        try {
            for(DeclarationData deletedDeclarationData: deletedDeclarationDataList) {
                Map<DeclarationDataReportType, LockData> taskTypeMap = getLockTaskType(deletedDeclarationData.getId());
                if (!taskTypeMap.isEmpty()) {
                    for(DeclarationDataReportType declarationDataReportType: taskTypeMap.keySet()) {
                        result.add(new Pair<Long, DeclarationDataReportType>(deletedDeclarationData.getId(), declarationDataReportType));
                    }
                } else {
                    LockData deleteLock = createDeleteLock(deletedDeclarationData.getId(), userInfo);
                    if (deleteLock != null) {
                        lockList.add(deleteLock);
                    } else {
                        result.add(new Pair<Long, DeclarationDataReportType>(deletedDeclarationData.getId(), DeclarationDataReportType.DELETE_DEC));
                    }
                }
            }
            if (result.size() == 0) {
                for(DeclarationData deletedDeclarationData: deletedDeclarationDataList) {
                    ScriptUtils.checkInterrupted();
                    try {
                        delete(deletedDeclarationData.getId(), userInfo);
                    } catch (ServiceException e) {
                        LOG.error(e);
                        result.add(new Pair<Long, DeclarationDataReportType>(deletedDeclarationData.getId(), DeclarationDataReportType.DELETE_DEC));
                        break;
                    }
                }
            }
        } finally {
            // удаляем блокировки
            for(LockData lockData: lockList) {
                lockDataService.unlock(lockData.getKey(), lockData.getUserId());
            }
        }
        return result;
    }
}
