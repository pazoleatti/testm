package com.aplana.sbrf.taxaccounting.service;

public interface FormDataPrintingService  {
	
	String generateExcel(int userId, long formDataId, boolean isShowChecked);

}
