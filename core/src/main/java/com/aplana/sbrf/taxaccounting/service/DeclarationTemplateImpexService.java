package com.aplana.sbrf.taxaccounting.service;

import java.io.InputStream;
import java.io.OutputStream;

import com.aplana.sbrf.taxaccounting.model.TAUserInfo;

public interface DeclarationTemplateImpexService {
	
	/**
	 * Экспорт
	 * 
	 * @param id
	 * @return
	 */
	void exportDeclarationTemplate(TAUserInfo userInfo, Integer id, OutputStream os);
	
	/**
	 * Импорт
	 * 
	 * @param id
	 * @param is
	 */
	void importDeclarationTemplate(TAUserInfo userInfo, Integer id, InputStream is);

}
