package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.LogBusinessDao;
import com.aplana.sbrf.taxaccounting.dao.api.ReportPeriodDao;
import com.aplana.sbrf.taxaccounting.model.AsyncTaskState;
import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.ScriptSpecificRefBookReportHolder;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.TAUserView;
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
import com.aplana.sbrf.taxaccounting.service.BlobDataService;
import com.aplana.sbrf.taxaccounting.service.DepartmentReportPeriodService;
import com.aplana.sbrf.taxaccounting.service.LockStateLogger;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.service.PrintingService;
import com.aplana.sbrf.taxaccounting.service.RefBookScriptingService;
import com.aplana.sbrf.taxaccounting.service.impl.print.logentry.LogEntryReportBuilder;
import com.aplana.sbrf.taxaccounting.service.impl.print.refbook.RefBookCSVReportBuilder;
import com.aplana.sbrf.taxaccounting.service.impl.print.refbook.RefBookExcelReportBuilder;
import com.aplana.sbrf.taxaccounting.service.impl.print.tausers.TAUsersReportBuilder;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Service
public class PrintingServiceImpl implements PrintingService {

    private static final Log LOG = LogFactory.getLog(PrintingServiceImpl.class);
    private static final String FILE_NAME = "Налоговый_отчет_";
    private static final String POSTFIX = ".xlsm";

    @Autowired
    private ReportPeriodDao reportPeriodDao;
    @Autowired
    private LogBusinessDao logBusinessDao;
    @Autowired
    private RefBookHelper refBookHelper;
    @Autowired
    private RefBookFactory refBookFactory;
    @Autowired
    private BlobDataService blobDataService;
    @Autowired
    private DepartmentReportPeriodService departmentReportPeriodService;
    @Autowired
    private RefBookScriptingService refBookScriptingService;
    @Autowired
    private LogEntryService logEntryService;

    private static final long REF_BOOK_ID = RefBook.Id.PERIOD_CODE.getId();
    private static final String REF_BOOK_VALUE_NAME = "CODE";

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
        } finally {
            cleanTmp(reportPath);
        }
    }

    private void cleanTmp(String filePath) {
        if (filePath != null) {
            File file = new File(filePath);
            if (file.delete()) {
                LOG.warn(String.format("Временный файл %s не был удален", filePath));
            }
        }
    }

    @Override
    public String generateExcelUsers(List<TAUserView> taUserViewList) {
        String reportPath = null;
        try {
            TAUsersReportBuilder taBuilder = new TAUsersReportBuilder(taUserViewList);
            reportPath = taBuilder.createReport();
            return blobDataService.create(reportPath, "Список_пользователей.xlsx");
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
            throw new ServiceException("Ошибка при создании печатной формы." + TAUsersReportBuilder.class);
        } finally {
            cleanTmp(reportPath);
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
                stateLogger.updateState(AsyncTaskState.BUILDING_REPORT);
                if (!refBookScriptingService.executeScript(userInfo, refBookId, FormDataEvent.CREATE_SPECIFIC_REPORT, logger, params)) {
                    throw new ServiceException("Не предусмотрена возможность формирования отчета \"%s\"", specificReportType);
                }
                if (logger.containsLevel(LogLevel.ERROR)) {
                    throw new ServiceLoggerException("Возникли ошибки при формировании отчета", logEntryService.save(logger.getEntries()));
                }
            } finally {
                IOUtils.closeQuietly(outputStream);
            }
            stateLogger.updateState(AsyncTaskState.SAVING_REPORT);
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
                refBookCSVReportBuilder = new RefBookCSVReportBuilder(refBook, refBookPage, dereferenceValues, version, sortAttribute);
            } else {
                Map<Long, Map<String, RefBookValue>> hierarchicRecords = new HashMap<Long, Map<String, RefBookValue>>();
                Iterator<Map<String, RefBookValue>> iterator = refBookPage.iterator();
                while (iterator.hasNext()) {
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
                } while (!refBookPage.isEmpty());
                refBookCSVReportBuilder = new RefBookCSVReportBuilder(refBook, new ArrayList<Map<String, RefBookValue>>(hierarchicRecords.values()), dereferenceValues, version, sortAttribute);
            }
            stateLogger.updateState(AsyncTaskState.BUILDING_REPORT);
            reportPath = refBookCSVReportBuilder.createReport();
            String fileName = reportPath.substring(reportPath.lastIndexOf("\\") + 1);
            stateLogger.updateState(AsyncTaskState.SAVING_REPORT);
            return blobDataService.create(reportPath, fileName);
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
            throw new ServiceException("Ошибка при формировании отчета по справочнику.");
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
                while (iterator.hasNext()) {
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
                } while (!refBookPage.isEmpty());
                refBookExcelReportBuilder = new RefBookExcelReportBuilder(refBook, new ArrayList<Map<String, RefBookValue>>(hierarchicRecords.values()), dereferenceValues, version, searchPattern, sortAttribute);
            }
            stateLogger.updateState(AsyncTaskState.BUILDING_REPORT);
            reportPath = refBookExcelReportBuilder.createReport();
            String fileName = reportPath.substring(reportPath.lastIndexOf("\\") + 1);
            stateLogger.updateState(AsyncTaskState.SAVING_REPORT);
            return blobDataService.create(reportPath, fileName);
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
            throw new ServiceException("Ошибка при формировании отчета по справочнику.");
        } finally {
            cleanTmp(reportPath);
        }
    }
}