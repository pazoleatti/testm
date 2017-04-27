package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.FormTemplate;

import java.io.InputStream;
import java.io.OutputStream;

public interface FormTemplateImpexService {
    /**
     * Экспортит все шаблоны, как налоговых форм так и деклараций.
     * @param stream
     */
    void exportAllTemplates(OutputStream stream);
}
