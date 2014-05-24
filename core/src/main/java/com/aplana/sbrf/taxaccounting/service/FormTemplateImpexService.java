package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.FormTemplate;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;

public interface FormTemplateImpexService {

    static final String TEMPLATES_FOLDER = "templates";
    static final String TEMPLATE_OF_FOLDER_NAME =
            "%s" + File.separator + "formTemplate_%s" + File.separator + "%s";
    static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy");

	/**
	 * Экспорт
	 * 
	 * @param id идентификатори макета
	 */
	void exportFormTemplate(Integer id, OutputStream os);
	
	/**
	 * Импорт
	 * 
	 * @param id идентификатори макета
	 * @param is импортирующийся архив
	 */
	FormTemplate importFormTemplate(Integer id, InputStream is);

    void exportAllTemplates(OutputStream stream);
}
