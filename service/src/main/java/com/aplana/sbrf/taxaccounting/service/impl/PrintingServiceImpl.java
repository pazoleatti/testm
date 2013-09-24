package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.*;
import com.aplana.sbrf.taxaccounting.dao.api.DataRowDao;
import com.aplana.sbrf.taxaccounting.dao.api.ReportPeriodDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.AccessDeniedException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.refbook.RefBookHelper;
import com.aplana.sbrf.taxaccounting.service.FormDataAccessService;
import com.aplana.sbrf.taxaccounting.service.PrintingService;
import com.aplana.sbrf.taxaccounting.service.impl.print.formdata.FormDataXlsxReportBuilder;
import com.aplana.sbrf.taxaccounting.service.impl.print.logentry.LogEntryReportBuilder;
import com.aplana.sbrf.taxaccounting.service.impl.print.logsystem.LogSystemReportBuilder;
import com.aplana.sbrf.taxaccounting.service.impl.print.tausers.TAUsersReportBuilder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class PrintingServiceImpl implements PrintingService {

	private static final Log logger = LogFactory.getLog(PrintingServiceImpl.class);

	@Autowired
	private FormDataDao formDataDao;

    @Autowired
	private FormTemplateDao formTemplateDao;

	@Autowired
	private FormDataAccessService formDataAccessService;

    @Autowired
	private DepartmentDao departmentDao;

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

    private static long REF_BOOK_ID = 8L;
    private static String REF_BOOK_VALUE_NAME = "CODE";
	
	@Override
	public String generateExcel(TAUserInfo userInfo, long formDataId, boolean isShowChecked) {
		if (formDataAccessService.canRead(userInfo, formDataId)) {
			FormDataReport data = new FormDataReport();
			FormData formData = formDataDao.get(formDataId);
			FormTemplate formTemplate = formTemplateDao.get(formData.getFormTemplateId());
			Department department =  departmentDao.getDepartment(formData.getDepartmentId());
			ReportPeriod reportPeriod = reportPeriodDao.get(formData.getReportPeriodId());
			
			data.setData(formData);
			data.setDepartment(department);
			data.setFormTemplate(formTemplate);
			data.setReportPeriod(reportPeriod);
			data.setAcceptanceDate(logBusinessDao.getFormAcceptanceDate(formDataId));
			data.setCreationDate(logBusinessDao.getFormCreationDate(formDataId));
            List<DataRow<Cell>> dataRows = dataRowDao.getSavedRows(formData, null, null);
            refBookHelper.dataRowsDereference(dataRows, formTemplate.getColumns());

            RefBookValue refBookValue = refBookFactory.getDataProvider(REF_BOOK_ID).
                    getRecordData((long) reportPeriod.getDictTaxPeriodId()).get(REF_BOOK_VALUE_NAME);
			try {
				FormDataXlsxReportBuilder builder = new FormDataXlsxReportBuilder(data,isShowChecked, dataRows, refBookValue);
				return builder.createReport();
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
				throw new ServiceException("Ошибка при создании печатной формы.");
			}
		}else{
			throw new AccessDeniedException("Недостаточно прав на просмотр данных налоговой формы",
					userInfo.getUser().getId(), formDataId
				);
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
    public String generateExcelUsers(List<TAUserFull> taUserFullList) {
        try {
            TAUsersReportBuilder taBuilder = new TAUsersReportBuilder(taUserFullList);
            return taBuilder.createReport();
        }catch (IOException e){
            logger.error(e.getMessage(), e);
            throw new ServiceException("Ошибка при создании печатной формы." + TAUsersReportBuilder.class);
        }
    }

    @Override
    public String generateExcelLogSystem(List<LogSystemSearchResultItem> resultItems) {
        try {
            LogSystemReportBuilder builder = new LogSystemReportBuilder(resultItems);
            return builder.createReport();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            throw new ServiceException("Ошибка при создании печатной формы." + LogSystemReportBuilder.class);
        }
    }

}
