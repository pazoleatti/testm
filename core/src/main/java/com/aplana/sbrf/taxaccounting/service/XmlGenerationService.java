package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.migration.Exemplar;

/**
 * Сервис для генерации ТФ в формате XML
 *
 * @author Alexande Ivanov
 */
public interface XmlGenerationService {

    String generateXmlFile(Exemplar ex);

    String getXmlFileName(Exemplar ex);
}
