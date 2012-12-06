package com.aplana.sbrf.taxaccounting.service.impl;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.service.FormDataPrintingService;
import com.aplana.sbrf.taxaccounting.service.FormDataService;
import com.aplana.sbrf.taxaccounting.service.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.service.impl.print.FormDataXlsxReportBuilder;

@Service
public class FormDataPrintingServiceImpl implements FormDataPrintingService  {

	@Autowired
	private FormDataService formDataService;
	
	@Override
	public String generateExcel(int userId, long formDataId) {
		FormData formData = formDataService.getFormData(userId, formDataId);
		try {
			FormDataXlsxReportBuilder builder = new FormDataXlsxReportBuilder(formData);
			return builder.createReport();
		} catch (IOException e) {
			throw new ServiceException("Ошибка при создании печатной формы.");
		}
	}

}
