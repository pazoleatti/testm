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
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.refbook.RefBookHelper;
import com.aplana.sbrf.taxaccounting.service.BlobDataService;
import com.aplana.sbrf.taxaccounting.service.DepartmentReportPeriodService;
import com.aplana.sbrf.taxaccounting.service.FormDataAccessService;
import com.aplana.sbrf.taxaccounting.service.PrintingService;
import com.aplana.sbrf.taxaccounting.service.FormDataService;
import com.aplana.sbrf.taxaccounting.service.impl.print.formdata.FormDataCSVReportBuilder;
import com.aplana.sbrf.taxaccounting.service.impl.print.formdata.FormDataXlsmReportBuilder;
import com.aplana.sbrf.taxaccounting.service.impl.print.logentry.LogEntryReportBuilder;
import com.aplana.sbrf.taxaccounting.service.impl.print.logsystem.LogSystemCsvBuilder;
import com.aplana.sbrf.taxaccounting.service.impl.print.logsystem.LogSystemXlsxReportBuilder;
import com.aplana.sbrf.taxaccounting.service.impl.print.tausers.TAUsersReportBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

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

    private static final long REF_BOOK_ID = 8L;
    private static final String REF_BOOK_VALUE_NAME = "CODE";

	@Override
	public String generateExcel(TAUserInfo userInfo, long formDataId, boolean manual, boolean isShowChecked, boolean saved, LockStateLogger stateLogger) {
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

            RefBookValue refBookValue = refBookFactory.getDataProvider(REF_BOOK_ID).
                getRecordData(reportPeriod.getDictTaxPeriodId()).get(REF_BOOK_VALUE_NAME);

            if (stateLogger != null) {
                stateLogger.updateState("Формирование XLSM-файла");
            }
            FormDataXlsmReportBuilder builder = new FormDataXlsmReportBuilder(data, isShowChecked, dataRows, refBookValue);
            filePath = builder.createReport();
            if (stateLogger != null) {
                stateLogger.updateState("Сохранение XLSM-файла в базе данных");
            }
            return blobDataService.create(new FileInputStream(filePath), FILE_NAME + POSTFIX);
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
            throw new ServiceException("Ошибка при создании печатной формы.");
        } catch (DaoException ex) {
			LOG.error(ex.getMessage(), ex);
            throw new ServiceException(ex.getMessage());
        }finally {
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
            return blobDataService.create(new FileInputStream(reportPath), FILE_NAME + ".csv");
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
            return blobDataService.create(new FileInputStream(reportPath), "errors.csv");
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
            return blobDataService.create(new FileInputStream(reportPath), "Список_пользователей.xlsx");
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
            return blobDataService.create(new FileInputStream(reportPath), "Журнал_аудита.xlsx");
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
            return blobDataService.create(new FileInputStream(reportPath), fileName);
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
}