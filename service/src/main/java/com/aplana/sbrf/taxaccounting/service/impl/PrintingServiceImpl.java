package com.aplana.sbrf.taxaccounting.service.impl;

import au.com.bytecode.opencsv.CSVWriter;
import com.aplana.sbrf.taxaccounting.dao.DepartmentDao;
import com.aplana.sbrf.taxaccounting.dao.FormDataDao;
import com.aplana.sbrf.taxaccounting.dao.FormTemplateDao;
import com.aplana.sbrf.taxaccounting.dao.LogBusinessDao;
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
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class PrintingServiceImpl implements PrintingService {

	private static final Log logger = LogFactory.getLog(PrintingServiceImpl.class);

    private static final SimpleDateFormat SDF_LOG_NAME = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
    private static final SimpleDateFormat SDF = new SimpleDateFormat("dd.MM.yyyy");
    private static final String PATTER_LOG_FILE_NAME = "log_<%s>-<%s>";
    private static final String ENCODING = "windows-1251";

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

    @Override
    public String generateAuditCsv(List<LogSystemSearchResultItem> resultItems) {
        try {
            String fileName = String.format(PATTER_LOG_FILE_NAME,
                    SDF_LOG_NAME.format(resultItems.get(0).getLogDate()),
                    SDF_LOG_NAME.format(resultItems.get(resultItems.size() - 1).getLogDate()));
            File file = File.createTempFile(fileName, ".csv");
            CSVWriter csvWriter = new CSVWriter(new FileWriter(file), ';');
            for (LogSystemSearchResultItem resultItem : resultItems) {
                csvWriter.writeNext(assemble(resultItem));
            }
            csvWriter.close();

            File zipFile = File.createTempFile(fileName, ".zip");
            ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(zipFile));
            ZipEntry zipEntry = new ZipEntry(file.getName());
            zout.putNextEntry(zipEntry);
            zout.write(IOUtils.toByteArray(new FileReader(file), ENCODING));
            zout.close();

            file.delete();
            return zipFile.getAbsolutePath();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            throw new ServiceException("Ошибка при архивировании журнала аудита." + LogSystemReportBuilder.class);
        }
    }

    private String[] assemble(LogSystemSearchResultItem item){
        List<String> entries = new ArrayList<String>();
        entries.add(SDF.format(item.getLogDate()));
        entries.add(item.getEvent().getTitle());
        entries.add(item.getNote());
        entries.add(item.getReportPeriod() != null ?
                item.getReportPeriod().getName() + " " + item.getReportPeriod().getYear() :
                "");
        entries.add(item.getDepartment().getName());
        entries.add(item.getFormType() != null?"Налоговые формы" :
                    item.getDeclarationType() != null?"Декларации":"");
        entries.add(item.getFormKind() != null ? item.getFormKind().getName() : "");
        entries.add(item.getFormType() != null ? item.getFormType().getName() : "");
        entries.add(item.getUser().getName());
        entries.add(item.getIp());
        entries.add(item.getRoles());

        return entries.toArray(new String[entries.size()]);
    }

}
