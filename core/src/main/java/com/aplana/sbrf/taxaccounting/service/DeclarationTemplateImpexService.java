package com.aplana.sbrf.taxaccounting.service;

import java.io.InputStream;
import java.io.OutputStream;

public interface DeclarationTemplateImpexService {
	
	/**
	 * Экспорт
	 * 
	 * @param id
	 * @return
	 */
	InputStream exportDeclarationTemplate(Integer id);
	
	/**
	 * Импорт
	 * 
	 * @param id
	 * @param is
	 */
	void importDeclarationTemplate(Integer id, OutputStream is);

}
