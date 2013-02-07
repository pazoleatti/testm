package com.aplana.sbrf.taxaccounting.service.impl;

import java.io.IOException;

import com.aplana.sbrf.taxaccounting.dao.FormDataDao;
import com.aplana.sbrf.taxaccounting.exception.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.FormTemplate;
import com.aplana.sbrf.taxaccounting.service.FormDataPrintingService;
import com.aplana.sbrf.taxaccounting.service.FormTemplateService;
import com.aplana.sbrf.taxaccounting.service.impl.print.FormDataXlsxReportBuilder;

@Service
public class FormDataPrintingServiceImpl implements FormDataPrintingService  {

	@Autowired
	private FormDataDao formDataDao;
	@Autowired
	private FormTemplateService formTemplateService;
	
	@Override
	public String generateExcel(long formDataId) {
		// TODO: заменить логгер или вообще использовать дао класс. {Сделано}
		FormData formData = formDataDao.get(formDataId);
		FormTemplate formTemplate = formTemplateService.get(formData.getFormTemplateId());
		try {
			FormDataXlsxReportBuilder builder = new FormDataXlsxReportBuilder(formData,formTemplate);
			return builder.createReport();
		} catch (IOException e) {
			throw new ServiceException("Ошибка при создании печатной формы.");
		}
	}

}
