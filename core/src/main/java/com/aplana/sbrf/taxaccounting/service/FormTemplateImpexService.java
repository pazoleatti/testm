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
	InputStream exportFormTemplate(Integer id);
	
	/**
	 * Импорт
	 * 
	 * @param id
	 * @param is
	 */
	void importFormTemplate(Integer id, OutputStream is);


}
