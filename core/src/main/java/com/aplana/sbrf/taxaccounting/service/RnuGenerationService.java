package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.migration.Exemplar;

/**
 * Сервис для генерации ТФ с расширением *.rnu
 *
 * @author Alexande Ivanov
 */
public interface RnuGenerationService {
    String generateRnuFile(Exemplar ex);

    String getRnuFileName(Exemplar exemplar);
}
