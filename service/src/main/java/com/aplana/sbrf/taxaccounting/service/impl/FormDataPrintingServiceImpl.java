package com.aplana.sbrf.taxaccounting.service.impl;

import java.io.IOException;

import com.aplana.sbrf.taxaccounting.dao.DepartmentDao;
import com.aplana.sbrf.taxaccounting.dao.FormDataDao;
import com.aplana.sbrf.taxaccounting.dao.FormTemplateDao;
import com.aplana.sbrf.taxaccounting.dao.ReportPeriodDao;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.FormDataReport;
import com.aplana.sbrf.taxaccounting.model.FormTemplate;
import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.exception.AccessDeniedException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.service.FormDataAccessService;
import com.aplana.sbrf.taxaccounting.service.FormDataPrintingService;
import com.aplana.sbrf.taxaccounting.service.impl.print.FormDataXlsxReportBuilder;

@Service
public class FormDataPrintingServiceImpl implements FormDataPrintingService  {

	private static final Log logger = LogFactory.getLog(FormDataPrintingServiceImpl.class);

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
	
	@Override
	public String generateExcel(int userId, long formDataId,boolean isShowChecked) {
		if (formDataAccessService.canRead(userId, formDataId)) {
			FormDataReport data = new FormDataReport();
			FormData formData = formDataDao.get(formDataId);
			FormTemplate formTemplate = formTemplateDao.get(formData.getFormTemplateId());
			Department department =  departmentDao.getDepartment(formData.getDepartmentId());
			ReportPeriod reportPeriod = reportPeriodDao.get(formData.getReportPeriodId());
			
			data.setData(formData);
			data.setDepartment(department);
			data.setFormTemplate(formTemplate);
			data.setReportPeriod(reportPeriod);
			try {
				FormDataXlsxReportBuilder builder = new FormDataXlsxReportBuilder(data,isShowChecked);
				return builder.createReport();
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
				throw new ServiceException("Ошибка при создании печатной формы.");
			}
		}else{
			throw new AccessDeniedException("Недостаточно прав на просмотр данных налоговой формы",
					userId, formDataId
				);
		}
		
	}

}
