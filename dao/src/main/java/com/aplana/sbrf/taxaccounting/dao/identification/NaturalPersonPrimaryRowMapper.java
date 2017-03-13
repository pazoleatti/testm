package com.aplana.sbrf.taxaccounting.dao.identification;

import com.aplana.sbrf.taxaccounting.model.identification.Country;
import com.aplana.sbrf.taxaccounting.model.identification.DocType;
import com.aplana.sbrf.taxaccounting.model.identification.NaturalPerson;
import com.aplana.sbrf.taxaccounting.model.identification.TaxpayerStatus;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import org.springframework.jdbc.core.RowMapper;

import java.util.Map;

/**
 * Обработчик запроса данных из ПНФ
 *
 * @author Andrey Drunk
 */
public abstract class NaturalPersonPrimaryRowMapper implements RowMapper<NaturalPerson> {

    private Map<String, Country> countryCodeMap;

    private Map<String, TaxpayerStatus> taxpayerStatusCodeMap;

    private Map<String, DocType> docTypeCodeMap;

    protected Logger logger = new Logger();

    public Map<String, Country> getCountryCodeMap() {
        return countryCodeMap;
    }

    public void setCountryCodeMap(Map<String, Country> countryCodeMap) {
        this.countryCodeMap = countryCodeMap;
    }

    public Map<String, TaxpayerStatus> getTaxpayerStatusCodeMap() {
        return taxpayerStatusCodeMap;
    }

    public void setTaxpayerStatusCodeMap(Map<String, TaxpayerStatus> taxpayerStatusCodeMap) {
        this.taxpayerStatusCodeMap = taxpayerStatusCodeMap;
    }

    public Map<String, DocType> getDocTypeCodeMap() {
        return docTypeCodeMap;
    }

    public void setDocTypeCodeMap(Map<String, DocType> docTypeCodeMap) {
        this.docTypeCodeMap = docTypeCodeMap;
    }

    public Logger getLogger() {
        return logger;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    public Country getCountryByCode(String code) {
        if (code != null) {

            if (countryCodeMap == null) {
                logger.warn("Не проинициализирован кэш справочника 'ОК 025-2001 (Общероссийский классификатор стран мира)'!");
                return null;
            }

            Country result = countryCodeMap.get(code);

            if (result == null) {
                logger.warn("В справочнике 'ОК 025-2001 (Общероссийский классификатор стран мира)' не найдена страна с кодом: " + code);
            }

            return result;
        } else {
            return null;
        }
    }

    public TaxpayerStatus getTaxpayerStatusByCode(String code) {

        if (code != null) {

            if (taxpayerStatusCodeMap == null) {
                logger.warn("Не проинициализирован кэш справочника 'Статусы налогоплательщика'!");
                return null;
            }

            TaxpayerStatus result = taxpayerStatusCodeMap.get(code);
            if (result == null) {
                logger.warn("В справочнике 'Статусы налогоплательщика' не найден статус с кодом: " + code);
            }
            return result;

        } else {
            return null;
        }
    }

    public DocType getDocTypeByCode(String code) {
        if (code != null) {

            if (docTypeCodeMap == null) {
                logger.warn("Не проинициализирован кэш справочника 'Коды документов'!");
                return null;
            }

            DocType result = docTypeCodeMap.get(code);

            if (result == null) {
                logger.warn("В справочнике 'Коды документов' не найден документ с кодом: " + code);
            }

            return result;
        } else {
            return null;
        }
    }


}
