package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.DeclarationTemplate;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;

import java.io.InputStream;
import java.io.OutputStream;

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
	DeclarationTemplate importDeclarationTemplate(TAUserInfo userInfo, Integer id, InputStream is);

}
