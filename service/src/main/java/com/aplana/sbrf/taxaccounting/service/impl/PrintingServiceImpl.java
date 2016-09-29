package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.core.api.LockStateLogger;
import com.aplana.sbrf.taxaccounting.dao.FormDataDao;
import com.aplana.sbrf.taxaccounting.dao.FormTemplateDao;
import com.aplana.sbrf.taxaccounting.dao.LogBusinessDao;
import com.aplana.sbrf.taxaccounting.dao.api.DataRowDao;
import com.aplana.sbrf.taxaccounting.dao.api.ReportPeriodDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.refbook.RefBookHelper;
import com.aplana.sbrf.taxaccounting.service.*;
import com.aplana.sbrf.taxaccounting.service.impl.print.formdata.FormDataCSVReportBuilder;
import com.aplana.sbrf.taxaccounting.service.impl.print.formdata.FormDataXlsmReportBuilder;
import com.aplana.sbrf.taxaccounting.service.impl.print.logentry.LogEntryReportBuilder;
import com.aplana.sbrf.taxaccounting.service.impl.print.logsystem.LogSystemCsvBuilder;
import com.aplana.sbrf.taxaccounting.service.impl.print.logsystem.LogSystemXlsxReportBuilder;
import com.aplana.sbrf.taxaccounting.service.impl.print.refbook.RefBookCSVReportBuilder;
import com.aplana.sbrf.taxaccounting.service.impl.print.refbook.RefBookExcelReportBuilder;
import com.aplana.sbrf.taxaccounting.service.impl.print.tausers.TAUsersReportBuilder;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.*;

@Service
public class PrintingServiceImpl implements PrintingService {

	private static final Log LOG = LogFactory.getLog(PrintingServiceImpl.class);
    private static final String FILE_NAME = "Налоговый_отчет_";
    private static final String POSTFIX = ".xlsm";

	@Autowired
	private FormDataDao formDataDao;
    @Autowired
	private FormTemplateDao formTemplateDao;
	@Autowired
	private FormDataAccessService formDataAccessService;
    @Autowired
	private ReportPeriodDao reportPeriodDao;
    @Autowired
	private LogBusinessDao logBusinessDao;
    @Autowired
    private DataRowDao dataRowDao;
    @Autowired
    private RefBookHelper refBookHelper;
    @Autowired
    private RefBookFactory refBookFactory;
    @Autowired
    private BlobDataService blobDataService;
    @Autowired
    private DepartmentReportPeriodService departmentReportPeriodService;
    @Autowired
    private FormDataService formDataService;
    @Autowired
    private RefBookScriptingService refBookScriptingService;
    @Autowired
    private LogEntryService logEntryService;

    private static final long REF_BOOK_ID = 8L;
    private static final String REF_BOOK_VALUE_NAME = "CODE";

	@Override
	public String generateExcel(TAUserInfo userInfo, long formDataId, boolean manual, boolean isShowChecked, boolean saved, boolean deleteHiddenColumns, LockStateLogger stateLogger) {
        String filePath = null;
        Logger log = new Logger();
        try {
            formDataAccessService.canRead(userInfo, formDataId);
            FormDataReport data = new FormDataReport();
            FormData formData = formDataDao.get(formDataId, manual);
            FormTemplate formTemplate = formTemplateDao.get(formData.getFormTemplateId());
            ReportPeriod reportPeriod = reportPeriodDao.get(formData.getReportPeriodId());
            // http://jira.aplana.com/browse/SBRFACCTAX-6399
            if ((formData.getKind() == FormDataKind.PRIMARY || formData.getKind() == FormDataKind.CONSOLIDATED)
                    && reportPeriod.getTaxPeriod().getTaxType() == TaxType.INCOME) {
                RefBookDataProvider dataProvider = refBookFactory.getDataProvider(REF_BOOK_ID);
                Map<String, RefBookValue> refBookValueMap = dataProvider.getRecordData(reportPeriod.getDictTaxPeriodId());
                Integer code = Integer.parseInt(refBookValueMap.get(REF_BOOK_VALUE_NAME).getStringValue());
                reportPeriod.setName(ReportPeriodSpecificName.fromId(code).getName());
            }
            formData.setHeaders(formDataService.getHeaders(formData, userInfo, log));
            data.setData(formData);
            data.setFormTemplate(formTemplate);
            data.setReportPeriod(reportPeriod);
            data.setAcceptanceDate(logBusinessDao.getFormAcceptanceDate(formDataId));
            data.setCreationDate(logBusinessDao.getFormCreationDate(formDataId));
            data.setRpCompare(formData.getComparativePeriodId() != null ? departmentReportPeriodService.get(formData.getComparativePeriodId()).getReportPeriod() : null);
            List<DataRow<Cell>> dataRows = (saved ? dataRowDao.getRows(formData, null) : dataRowDao.getTempRows(formData, null));
            refBookHelper.dataRowsDereference(log, dataRows, formTemplate.getColumns());

            RefBookValue periodCode = refBookFactory.getDataProvider(REF_BOOK_ID).
                getRecordData(reportPeriod.getDictTaxPeriodId()).get(REF_BOOK_VALUE_NAME);

            if (stateLogger != null) {
                stateLogger.updateState("Формирование XLSM-файла");
            }
            FormDataXlsmReportBuilder builder = new FormDataXlsmReportBuilder(data, isShowChecked, dataRows, periodCode, deleteHiddenColumns);
            filePath = builder.createReport();
            if (stateLogger != null) {
                stateLogger.updateState("Сохранение XLSM-файла в базе данных");
            }
            return blobDataService.create(filePath, FILE_NAME + POSTFIX);
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
            throw new ServiceException("Ошибка при создании печатной формы.");
        } catch (DaoException ex) {
			LOG.error(ex.getMessage(), ex);
            throw new ServiceException(ex.getMessage());
        } finally {
            cleanTmp(filePath);
        }
	}

    @Override
    public String generateCSV(TAUserInfo userInfo, long formDataId, boolean manual, boolean isShowChecked, boolean saved, LockStateLogger stateLogger) {
        String reportPath = null;
        try {
            formDataAccessService.canRead(userInfo, formDataId);
            FormDataReport data = new FormDataReport();
            FormData formData = formDataDao.get(formDataId, manual);
            FormTemplate formTemplate = formTemplateDao.get(formData.getFormTemplateId());
            ReportPeriod reportPeriod = reportPeriodDao.get(formData.getReportPeriodId());
            // http://jira.aplana.com/browse/SBRFACCTAX-6399
            if ((formData.getKind() == FormDataKind.PRIMARY || formData.getKind() == FormDataKind.CONSOLIDATED)
                    && reportPeriod.getTaxPeriod().getTaxType() == TaxType.INCOME) {
                RefBookDataProvider dataProvider = refBookFactory.getDataProvider(REF_BOOK_ID);
                Map<String, RefBookValue> refBookValueMap = dataProvider.getRecordData(reportPeriod.getDictTaxPeriodId());
                Integer code = Integer.parseInt(refBookValueMap.get(REF_BOOK_VALUE_NAME).getStringValue());
                reportPeriod.setName(ReportPeriodSpecificName.fromId(code).getName());
            }
            data.setData(formData);
            data.setFormTemplate(formTemplate);
            data.setAcceptanceDate(logBusinessDao.getFormAcceptanceDate(formDataId));
            data.setCreationDate(logBusinessDao.getFormCreationDate(formDataId));
            List<DataRow<Cell>> dataRows = (saved ? dataRowDao.getRows(formData, null) : dataRowDao.getTempRows(formData, null));
            Logger log = new Logger();
            refBookHelper.dataRowsDereference(log, dataRows, formTemplate.getColumns());

            RefBookValue refBookValue = refBookFactory.getDataProvider(REF_BOOK_ID).
                    getRecordData(reportPeriod.getDictTaxPeriodId()).get(REF_BOOK_VALUE_NAME);
            stateLogger.updateState("Формирование CSV-файла");
            FormDataCSVReportBuilder builder = new FormDataCSVReportBuilder(data, isShowChecked, dataRows, refBookValue);
            reportPath = builder.createReport();
            stateLogger.updateState("Сохранение CSV-файла в базе данных");
            return blobDataService.create(reportPath, FILE_NAME + ".csv");
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
            throw new ServiceException("Ошибка при создании печатной формы.");
        } catch (DaoException ex) {
			LOG.error(ex.getMessage(), ex);
            throw new ServiceException(ex.getMessage());
        } finally {
            cleanTmp(reportPath);
        }
    }

    @Override
	public String generateExcelLogEntry(List<LogEntry> listLogEntries) {
        String reportPath = null;
		try {
			LogEntryReportBuilder builder = new LogEntryReportBuilder(listLogEntries);
            reportPath = builder.createReport();
            return blobDataService.create(reportPath, "errors.csv");
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
			throw new ServiceException("Ошибка при создании печатной формы." + LogEntryReportBuilder.class);
		}finally {
            cleanTmp(reportPath);
        }
	}

    @Override
    public String generateExcelUsers(List<TAUserView> taUserViewList) {
        String reportPath = null;
        try {
            TAUsersReportBuilder taBuilder = new TAUsersReportBuilder(taUserViewList);
            reportPath = taBuilder.createReport();
            return blobDataService.create(reportPath, "Список_пользователей.xlsx");
        }catch (IOException e){
            LOG.error(e.getMessage(), e);
            throw new ServiceException("Ошибка при создании печатной формы." + TAUsersReportBuilder.class);
        }finally {
            cleanTmp(reportPath);
        }
    }

    @Override
    public String generateExcelLogSystem(List<LogSearchResultItem> resultItems) {
        String reportPath = null;
        try {
            LogSystemXlsxReportBuilder builder = new LogSystemXlsxReportBuilder(resultItems);
            reportPath = builder.createReport();
            return blobDataService.create(reportPath, "Журнал_аудита.xlsx");
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
            throw new ServiceException("Ошибка при создании печатной формы." + LogSystemXlsxReportBuilder.class);
        } finally {
            cleanTmp(reportPath);
        }
    }

    @Override
    public String generateAuditZip(List<LogSearchResultItem> resultItems) {
        String reportPath = null;
        try {
            LogSystemCsvBuilder logSystemCsvBuilder = new LogSystemCsvBuilder(resultItems);
            reportPath = logSystemCsvBuilder.createReport();
            String fileName = reportPath.substring(reportPath.lastIndexOf("\\") + 1);
            return blobDataService.create(reportPath, fileName);
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
            throw new ServiceException("Ошибка при архивировании журнала аудита." + LogSystemXlsxReportBuilder.class);
        } finally {
            cleanTmp(reportPath);
        }
    }

    private void cleanTmp(String filePath){
        if (filePath != null){
            File file = new File(filePath);
            if (file.delete()){
                LOG.warn(String.format("Временный файл %s не был удален", filePath));
            }
        }
    }

    @Override
    public String generateRefBookSpecificReport(long refBookId, String specificReportType, Date version, String filter, String searchPattern, RefBookAttribute sortAttribute, boolean isSortAscending, TAUserInfo userInfo, LockStateLogger stateLogger) {
        Map<String, Object> params = new HashMap<String, Object>();
        ScriptSpecificRefBookReportHolder scriptSpecificReportHolder = new ScriptSpecificRefBookReportHolder();
        File reportFile = null;
        try {
            reportFile = File.createTempFile("specific_report", ".dat");
            OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(reportFile));
            try {
                Logger logger = new Logger();
                scriptSpecificReportHolder.setSpecificReportType(specificReportType);
                scriptSpecificReportHolder.setFileOutputStream(outputStream);
                scriptSpecificReportHolder.setFileName(specificReportType);
                scriptSpecificReportHolder.setVersion(version);
                scriptSpecificReportHolder.setFilter(filter);
                scriptSpecificReportHolder.setSearchPattern(searchPattern);
                scriptSpecificReportHolder.setSortAttribute(sortAttribute);
                scriptSpecificReportHolder.setSortAscending(isSortAscending);
                params.put("scriptSpecificReportHolder", scriptSpecificReportHolder);
                stateLogger.updateState("Формирование отчета");
                if (!refBookScriptingService.executeScript(userInfo, refBookId, FormDataEvent.CREATE_SPECIFIC_REPORT, logger, params)) {
                    throw new ServiceException("Не предусмотрена возможность формирования отчета \"%s\"", specificReportType);
                }
                if (logger.containsLevel(LogLevel.ERROR)) {
                    throw new ServiceLoggerException("Возникли ошибки при формировании отчета", logEntryService.save(logger.getEntries()));
                }
            } finally {
                IOUtils.closeQuietly(outputStream);
            }
            stateLogger.updateState("Сохранение отчета в базе данных");
            return blobDataService.create(reportFile.getPath(), scriptSpecificReportHolder.getFileName());
        } catch (IOException e) {
            throw new ServiceException(e.getLocalizedMessage(), e);
        } finally {
            if (reportFile != null)
                reportFile.delete();
        }
    }

    @Override
    public String generateRefBookCSV(long refBookId, Date version, String filter,
                                     RefBookAttribute sortAttribute, boolean isSortAscending, LockStateLogger stateLogger) {

        String reportPath = null;
        try {
            RefBookDataProvider refBookDataProvider = refBookFactory.getDataProvider(refBookId);
            RefBook refBook = refBookFactory.get(refBookId);
            PagingParams pagingParams = new PagingParams();
            pagingParams.setStartIndex(1);
            pagingParams.setCount(refBookDataProvider.getRecordsCount(version, filter));
            List<Map<String, RefBookValue>> refBookPage = refBookDataProvider.getRecords(version,
                    pagingParams, filter, sortAttribute, isSortAscending);
            Map<Long, Map<Long, String>> dereferenceValues = refBookHelper.dereferenceValues(refBook, refBookPage, false);
            RefBookCSVReportBuilder refBookCSVReportBuilder;
            if (!refBook.isHierarchic()) {
                refBookCSVReportBuilder = new RefBookCSVReportBuilder(refBook, refBookPage, dereferenceValues, sortAttribute);
            } else {
                Map<Long, Map<String, RefBookValue>> hierarchicRecords = new HashMap<Long, Map<String, RefBookValue>>();
                Iterator<Map<String, RefBookValue>> iterator = refBookPage.iterator();
                while(iterator.hasNext()) {
                    Map<String, RefBookValue> record = iterator.next();
                    hierarchicRecords.put(record.get(RefBook.RECORD_ID_ALIAS).getNumberValue().longValue(), record);
                }
                do {
                    iterator = refBookPage.iterator();
                    List<Map<String, RefBookValue>> parentRecords = new ArrayList<Map<String, RefBookValue>>();
                    while (iterator.hasNext()) {
                        Map<String, RefBookValue> record = iterator.next();
                        Long parentId = record.get(RefBook.RECORD_PARENT_ID_ALIAS).getReferenceValue();
                        if (parentId != null && !hierarchicRecords.containsKey(parentId)) {
                            Map<String, RefBookValue> parentRecord = refBookDataProvider.getRecordData(parentId);
                            hierarchicRecords.put(parentRecord.get(RefBook.RECORD_ID_ALIAS).getNumberValue().longValue(), parentRecord);
                            parentRecords.add(parentRecord);
                        }
                    }
                    Map<Long, Map<Long, String>> dereferenceParentValues = refBookHelper.dereferenceValues(refBook, parentRecords, false);
                    dereferenceValues.putAll(dereferenceParentValues);
                    refBookPage = parentRecords;
                } while(!refBookPage.isEmpty());
                refBookCSVReportBuilder = new RefBookCSVReportBuilder(refBook, new ArrayList<Map<String, RefBookValue>>(hierarchicRecords.values()), dereferenceValues, sortAttribute);
            }
            stateLogger.updateState("Формирование отчета");
            reportPath = refBookCSVReportBuilder.createReport();
            String fileName = reportPath.substring(reportPath.lastIndexOf("\\") + 1);
            stateLogger.updateState("Сохранение отчета в базе данных");
            return blobDataService.create(reportPath, fileName);
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
            throw new ServiceException("Ошибка при формировании отчета по справочнику." + LogSystemXlsxReportBuilder.class);
        } finally {
            cleanTmp(reportPath);
        }
    }

    @Override
    public String generateRefBookExcel(long refBookId, Date version, String filter, String searchPattern,
                                     RefBookAttribute sortAttribute, boolean isSortAscending, LockStateLogger stateLogger) {

        String reportPath = null;
        try {
            RefBookDataProvider refBookDataProvider = refBookFactory.getDataProvider(refBookId);
            RefBook refBook = refBookFactory.get(refBookId);
            PagingParams pagingParams = new PagingParams();
            pagingParams.setStartIndex(1);
            pagingParams.setCount(refBookDataProvider.getRecordsCount(version, filter));
            List<Map<String, RefBookValue>> refBookPage = refBookDataProvider.getRecords(version,
                    pagingParams, filter, sortAttribute, isSortAscending);
            Map<Long, Pair<RefBookAttribute, Map<Long, RefBookValue>>> dereferenceValues = refBookHelper.dereferenceValuesAttributes(refBook, refBookPage);
            RefBookExcelReportBuilder refBookExcelReportBuilder;
            if (!refBook.isHierarchic()) {
                refBookExcelReportBuilder = new RefBookExcelReportBuilder(refBook, refBookPage, dereferenceValues, version, searchPattern, null);
            } else {
                Map<Long, Map<String, RefBookValue>> hierarchicRecords = new HashMap<Long, Map<String, RefBookValue>>();
                Iterator<Map<String, RefBookValue>> iterator = refBookPage.iterator();
                while(iterator.hasNext()) {
                    Map<String, RefBookValue> record = iterator.next();
                    hierarchicRecords.put(record.get(RefBook.RECORD_ID_ALIAS).getNumberValue().longValue(), record);
                }
                do {
                    iterator = refBookPage.iterator();
                    List<Map<String, RefBookValue>> parentRecords = new ArrayList<Map<String, RefBookValue>>();
                    while (iterator.hasNext()) {
                        Map<String, RefBookValue> record = iterator.next();
                        Long parentId = record.get(RefBook.RECORD_PARENT_ID_ALIAS).getReferenceValue();
                        if (parentId != null && !hierarchicRecords.containsKey(parentId)) {
                            Map<String, RefBookValue> parentRecord = refBookDataProvider.getRecordData(parentId);
                            hierarchicRecords.put(parentRecord.get(RefBook.RECORD_ID_ALIAS).getNumberValue().longValue(), parentRecord);
                            parentRecords.add(parentRecord);
                        }
                    }
                    Map<Long, Pair<RefBookAttribute, Map<Long, RefBookValue>>> dereferenceParentValues = refBookHelper.dereferenceValuesAttributes(refBook, parentRecords);
                    dereferenceValues.putAll(dereferenceParentValues);
                    refBookPage = parentRecords;
                } while(!refBookPage.isEmpty());
                refBookExcelReportBuilder = new RefBookExcelReportBuilder(refBook, new ArrayList<Map<String, RefBookValue>>(hierarchicRecords.values()), dereferenceValues, version, searchPattern, sortAttribute);
            }
            stateLogger.updateState("Формирование отчета");
            reportPath = refBookExcelReportBuilder.createReport();
            String fileName = reportPath.substring(reportPath.lastIndexOf("\\") + 1);
            stateLogger.updateState("Сохранение отчета в базе данных");
            return blobDataService.create(reportPath, fileName);
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
            throw new ServiceException("Ошибка при формировании отчета по справочнику." + LogSystemXlsxReportBuilder.class);
        } finally {
            cleanTmp(reportPath);
        }
    }
}