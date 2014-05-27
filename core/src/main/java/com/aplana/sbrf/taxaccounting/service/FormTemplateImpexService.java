package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.FormTemplate;

import java.io.InputStream;
import java.io.OutputStream;

public interface FormTemplateImpexService {

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

    /**
     * Экспортит все шаблоны, как налоговых форм так и деклараций.
     * @param stream
     */
    void exportAllTemplates(OutputStream stream);
}
