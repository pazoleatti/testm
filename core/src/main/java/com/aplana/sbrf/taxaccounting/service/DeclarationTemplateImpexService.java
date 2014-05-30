package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.DeclarationTemplate;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;

import java.io.InputStream;
import java.io.OutputStream;

public interface DeclarationTemplateImpexService {
    public final static String VERSION_FILE = "version";
    public final static String SCRIPT_FILE = "script.groovy";
    public final static String REPORT_FILE = "report.jrxml";

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
