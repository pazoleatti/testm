package com.aplana.sbrf.taxaccounting.service;

import java.io.OutputStream;

public interface FormTemplateImpexService {
    /**
     * Экспортит все шаблоны, как налоговых форм так и деклараций.
     * @param stream
     */
    void exportAllTemplates(OutputStream stream);
}
