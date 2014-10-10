package com.aplana.sbrf.taxaccounting.service.impl;

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
import com.aplana.sbrf.taxaccounting.service.FormDataAccessService;
import com.aplana.sbrf.taxaccounting.service.PrintingService;
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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
public class PrintingServiceImpl implements PrintingService {

	private static final Log logger = LogFactory.getLog(PrintingServiceImpl.class);

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
    RefBookHelper refBookHelper;

    @Autowired
    RefBookFactory refBookFactory;

    @Autowired
    private BlobDataService blobDataService;

    private static final long REF_BOOK_ID = 8L;
    private static final String REF_BOOK_VALUE_NAME = "CODE";

	@Override
	public String generateExcel(TAUserInfo userInfo, long formDataId, boolean manual, boolean isShowChecked, boolean saved) {
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
            data.setReportPeriod(reportPeriod);
            data.setAcceptanceDate(logBusinessDao.getFormAcceptanceDate(formDataId));
            data.setCreationDate(logBusinessDao.getFormCreationDate(formDataId));
            List<DataRow<Cell>> dataRows = (saved ? dataRowDao.getSavedRows(formData, null) : dataRowDao.getRows(formData, null));
            Logger log = new Logger();
            refBookHelper.dataRowsDereference(log, dataRows, formTemplate.getColumns());

            RefBookValue refBookValue = refBookFactory.getDataProvider(REF_BOOK_ID).
                getRecordData(reportPeriod.getDictTaxPeriodId()).get(REF_BOOK_VALUE_NAME);

            FormDataXlsmReportBuilder builder = new FormDataXlsmReportBuilder(data, isShowChecked, dataRows, refBookValue);
            return blobDataService.create(new ByteArrayInputStream(builder.createBlobData()), FILE_NAME + POSTFIX);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            throw new ServiceException("Ошибка при создании печатной формы.");
        } catch (DaoException ex) {
            throw new ServiceException(ex.getMessage());
        }

	}

    @Override
    public String generateCSV(TAUserInfo userInfo, long formDataId, boolean manual, boolean isShowChecked, boolean saved) {
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
            data.setReportPeriod(reportPeriod);
            data.setAcceptanceDate(logBusinessDao.getFormAcceptanceDate(formDataId));
            data.setCreationDate(logBusinessDao.getFormCreationDate(formDataId));
            List<DataRow<Cell>> dataRows = (saved ? dataRowDao.getSavedRows(formData, null) : dataRowDao.getRows(formData, null));
            Logger log = new Logger();
            refBookHelper.dataRowsDereference(log, dataRows, formTemplate.getColumns());


            RefBookValue refBookValue = refBookFactory.getDataProvider(REF_BOOK_ID).
                    getRecordData(reportPeriod.getDictTaxPeriodId()).get(REF_BOOK_VALUE_NAME);
            FormDataCSVReportBuilder builder = new FormDataCSVReportBuilder(data, isShowChecked, dataRows, refBookValue);
            return blobDataService.create(new ByteArrayInputStream(builder.createBlobData()), FILE_NAME + ".csv");
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            throw new ServiceException("Ошибка при создании печатной формы.");
        } catch (DaoException ex) {
            throw new ServiceException(ex.getMessage());
        }
    }

    @Override
	public String generateExcelLogEntry(List<LogEntry> listLogEntries) {

		try {
			LogEntryReportBuilder builder = new LogEntryReportBuilder(listLogEntries);
			return builder.createReport() ;
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			throw new ServiceException("Ошибка при создании печатной формы." + LogEntryReportBuilder.class);
		}
	}

    @Override
    public String generateExcelUsers(List<TAUserView> taUserViewList) {
        try {
            TAUsersReportBuilder taBuilder = new TAUsersReportBuilder(taUserViewList);
            return taBuilder.createReport();
        }catch (IOException e){
            logger.error(e.getMessage(), e);
            throw new ServiceException("Ошибка при создании печатной формы." + TAUsersReportBuilder.class);
        }
    }

    @Override
    public String generateExcelLogSystem(List<LogSearchResultItem> resultItems) {
        try {
            LogSystemXlsxReportBuilder builder = new LogSystemXlsxReportBuilder(resultItems);
            return builder.createReport();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            throw new ServiceException("Ошибка при создании печатной формы." + LogSystemXlsxReportBuilder.class);
        }
    }

    @Override
    public String generateAuditCsv(List<LogSearchResultItem> resultItems) {
        try {
            LogSystemCsvBuilder logSystemCsvBuilder = new LogSystemCsvBuilder(resultItems);
            return logSystemCsvBuilder.createReport();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            throw new ServiceException("Ошибка при архивировании журнала аудита." + LogSystemXlsxReportBuilder.class);
        }
    }
}
