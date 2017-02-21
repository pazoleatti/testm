package com.aplana.sbrf.taxaccounting.service.script.impl;

import com.aplana.sbrf.taxaccounting.core.api.LockStateLogger;
import com.aplana.sbrf.taxaccounting.dao.DeclarationDataDao;
import com.aplana.sbrf.taxaccounting.dao.DeclarationDataFileDao;
import com.aplana.sbrf.taxaccounting.dao.DeclarationTemplateDao;
import com.aplana.sbrf.taxaccounting.dao.FormDataDao;
import com.aplana.sbrf.taxaccounting.dao.api.DeclarationTypeDao;
import com.aplana.sbrf.taxaccounting.dao.api.DepartmentReportPeriodDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.service.*;
import com.aplana.sbrf.taxaccounting.service.api.ConfigurationService;
import com.aplana.sbrf.taxaccounting.service.script.DeclarationService;
import com.aplana.sbrf.taxaccounting.service.shared.ScriptComponentContext;
import com.aplana.sbrf.taxaccounting.service.shared.ScriptComponentContextHolder;
import groovy.lang.Closure;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.util.JRSwapFile;
import org.apache.commons.codec.CharEncoding;
import org.apache.poi.util.IOUtils;
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

    private static final String CHECK_UNIQUE_ERROR = "Налоговая форма с заданными параметрами уже существует!";
    private static final String CHECK_UNIQUE_NOTIFICATION_ERROR = "Уведомление с заданными параметрами уже существует!";

    // Тип налога -> ID справочника с параметрами подразделения
    private static final Map<TaxType, Long> TAX_TYPE_TO_REF_BOOK_MAP = new HashMap<TaxType, Long>() {
        {
            put(TaxType.NDFL, RefBook.Id.NDFL.getId());
            put(TaxType.PFR, RefBook.Id.FOND.getId());
        }
    };

    private ScriptComponentContext context;

    @Autowired
    private DeclarationDataDao declarationDataDao;
    @Autowired
    private DeclarationDataService declarationDataService;
    @Autowired
    private FormDataDao formDataDao;
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
    public FormDataCollection getAcceptedFormDataSources(DeclarationData declarationData, TAUserInfo userInfo, Logger logger) {
        List<Relation> relations = sourceService.getDeclarationSourcesInfo(declarationData, true, true, null, userInfo, logger);
        List<FormData> sources = new ArrayList<FormData>();
        for (Relation relation : relations) {
            if (relation.getState() == WorkflowState.ACCEPTED) {
                FormData formData = formDataDao.get(relation.getFormDataId(), relation.isManual());
                sources.add(formData);
            } else {
                context.getLogger().warn(
                        "Форма-источник существует, но не может быть использована, так как еще не принята. Вид формы: \"%s\", тип формы: \"%s\", подразделение: \"%s\"",
                        relation.getFormTypeName(),
                        relation.getFormDataKind().getTitle(),
                        relation.getFullDepartmentName()
                );
            }
        }
        FormDataCollection formDataCollection = new FormDataCollection();
        formDataCollection.setRecords(sources);
        return formDataCollection;
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

    @Override
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
    }

    @Override
    public String getXmlDataFileName(long declarationDataId) {
        String fileName = declarationDataService.getXmlDataFileName(declarationDataId, taUserService.getSystemUserInfo());
        if (fileName != null) {
            return fileName.replace(".zip", ".xml");
        }
        return null;
    }

    @Override
    public List<Relation> getDeclarationDestinationsInfo(FormData sourceFormData, boolean light, boolean excludeIfNotExist, WorkflowState stateRestriction, TAUserInfo userInfo, Logger logger) {
        return sourceService.getDeclarationDestinationsInfo(sourceFormData, light, excludeIfNotExist, stateRestriction, userInfo, logger);
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
        ByteArrayInputStream xmlData = generateXmlData(xmlBuilder);
        return declarationDataService.createJasperReport(xmlData, jrxmlTemplate, parameters);
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
        declarationDataDao.setStatus(declarationDataId, State.CREATED);
        declarationDataService.delete(declarationDataId, userInfo);
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
        validateDeclaration(declarationData, userInfo, logger, dataFile, null);
    }

    @Override
    public void validateDeclaration(DeclarationData declarationData, TAUserInfo userInfo, Logger logger, File dataFile, String xsdBlobDataId) {
        declarationDataService.validateDeclaration(userInfo, declarationData, logger, false, FormDataEvent.IMPORT_TRANSPORT_FILE, dataFile, xsdBlobDataId, new LockStateLogger() {
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
    public void importDeclarationData(Logger logger, TAUserInfo userInfo, DeclarationData declarationData, InputStream inputStream, String fileName, File dataFile, AttachFileType attachFileType, Date createDateFile) {
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
        return configurationService.getAllConfig(userInfo);
    }
}
