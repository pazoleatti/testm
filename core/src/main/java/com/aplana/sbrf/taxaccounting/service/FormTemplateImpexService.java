package com.aplana.sbrf.taxaccounting.service;

import java.io.InputStream;
import java.io.OutputStream;

public interface FormTemplateImpexService {
	
	/**
	 * Экспорт
	 * 
	 * @param id
	 * @return
	 */
	void exportFormTemplate(Integer id, OutputStream os);
	
	/**
	 * Импорт
	 * 
	 * @param id
	 * @param is
	 */
	void importFormTemplate(Integer id, InputStream is);


}
