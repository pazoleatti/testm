package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.refbook.RefBookHelper;
import com.aplana.sbrf.taxaccounting.service.*;
import com.aplana.sbrf.taxaccounting.service.impl.print.logentry.LogEntryReportBuilder;
import com.aplana.sbrf.taxaccounting.service.impl.print.refbook.RefBookCSVReportBuilder;
import com.aplana.sbrf.taxaccounting.service.impl.print.refbook.RefBookExcelReportBuilder;
import com.aplana.sbrf.taxaccounting.service.impl.print.tausers.TAUsersReportBuilder;
import com.aplana.sbrf.taxaccounting.service.impl.refbook.BatchIterator;
import com.aplana.sbrf.taxaccounting.service.refbook.CommonRefBookService;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.*;

@Service
public class PrintingServiceImpl implements PrintingService {

    private static final Log LOG = LogFactory.getLog(PrintingServiceImpl.class);
    private static final String FILE_NAME = "Налоговый_отчет_";
    private static final String POSTFIX = ".xlsm";

    @Autowired
    private RefBookHelper refBookHelper;
    @Autowired
    private RefBookFactory refBookFactory;
    @Autowired
    private CommonRefBookService commonRefBookService;
    @Autowired
    private BlobDataService blobDataService;
    @Autowired
    private RefBookScriptingService refBookScriptingService;
    @Autowired
    private LogEntryService logEntryService;

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
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'N_ROLE_CONTROL_UNP', 'N_ROLE_CONTROL_NS')")
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

    /**
     * Возвращает список атрибутов, которые будут отображаться в отчете по справочнику
     *
     * @param refBook справочник
     * @return список атрибутов
     */
    private List<RefBookAttribute> getAttributes(RefBook refBook) {
        List<RefBookAttribute> attributes = new ArrayList<>();
        if (refBook.isVersioned()) {
            // Для версионируемых справочников дополняем список атрибутов, т.к они отсутствуют в БД
            RefBookAttribute versionFromAttribute = new RefBookAttribute(RefBook.RECORD_VERSION_FROM_ALIAS, RefBookAttributeType.DATE);
            versionFromAttribute.setName(RefBook.REF_BOOK_VERSION_FROM_TITLE);
            versionFromAttribute.setWidth(10);
            versionFromAttribute.setVisible(true);
            attributes.add(versionFromAttribute);

            RefBookAttribute versionToAttribute = new RefBookAttribute(RefBook.RECORD_VERSION_TO_ALIAS, RefBookAttributeType.DATE);
            versionToAttribute.setName(RefBook.REF_BOOK_VERSION_TO_TITLE);
            versionToAttribute.setWidth(10);
            versionToAttribute.setVisible(true);
            attributes.add(versionToAttribute);
        }

        for (RefBookAttribute attribute : refBook.getAttributes()) {
            if (attribute.isVisible()) {
                attributes.add(attribute);
            }
        }
        return attributes;
    }

    @Override
    public String generateRefBookCSV(long refBookId, Date version, String searchPattern, boolean exactSearch, Map<String, String> extraParams,
                                     RefBookAttribute sortAttribute, String direction, LockStateLogger stateLogger) {

        String reportPath = null;
        try {
            RefBook refBook = commonRefBookService.get(refBookId);
            PagingResult<Map<String, RefBookValue>> records;

            if (refBook.isHierarchic()) {
                records = commonRefBookService.fetchHierRecords(refBookId, searchPattern, exactSearch, false);
            } else {
                records = commonRefBookService.fetchAllRecords(refBookId, null, version, searchPattern, exactSearch, extraParams, null, sortAttribute, direction);
            }

            RefBookCSVReportBuilder refBookCSVReportBuilder = new RefBookCSVReportBuilder(refBook, getAttributes(refBook), records, version, searchPattern, exactSearch, sortAttribute);
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
    public String generateRefBookExcel(final long refBookId, final Date version, final String searchPattern, final boolean exactSearch, final Map<String, String> extraParams,
                                       final RefBookAttribute sortAttribute, final String direction, LockStateLogger stateLogger) {

        String reportPath = null;
        try {
            final RefBook refBook = commonRefBookService.get(refBookId);

            RefBookExcelReportBuilder refBookExcelReportBuilder;
            if (refBook.isHierarchic()) {
                refBookExcelReportBuilder = new RefBookExcelReportBuilder(refBook, getAttributes(refBook),
                        version, searchPattern, exactSearch, sortAttribute, commonRefBookService.fetchHierRecords(refBookId, searchPattern, exactSearch, false));
            } else {
                refBookExcelReportBuilder = new RefBookExcelReportBuilder(refBook, getAttributes(refBook),
                        version, searchPattern, exactSearch, sortAttribute, new BatchIterator() {
                    private PagingResult<Map<String, RefBookValue>> currentBatch = null;
                    private Iterator<Map<String, RefBookValue>> iterator = null;
                    private int page = 1;

                    @Override
                    public boolean hasNext() {
                        if (iterator == null || !iterator.hasNext()) {
                            PagingParams pagingParams = new PagingParams();
                            pagingParams.setPage(page);
                            pagingParams.setCount(BATCH_SIZE);
                            currentBatch = commonRefBookService.fetchAllRecords(refBookId, null, version, searchPattern, exactSearch, extraParams, pagingParams, sortAttribute, direction);
                            page++;
                            iterator = currentBatch.iterator();
                            return iterator.hasNext();
                        }
                        return true;
                    }

                    @Override
                    public Map<String, RefBookValue> getNextRecord() {
                        if (iterator.hasNext()) {
                            return iterator.next();
                        }
                        return null;
                    }
                });
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