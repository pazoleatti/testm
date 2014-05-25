package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.DeclarationTemplate;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.zip.ZipOutputStream;

public interface DeclarationTemplateImpexService {
    static final String TEMPLATES_FOLDER = "declarationTemplates";
    static final String TEMPLATE_OF_FOLDER_NAME =
            "%s" + File.separator + "declaration_%s" + File.separator + "%s";
    static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy");
	
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

    void exportAllTemplates(ZipOutputStream stream);
}
