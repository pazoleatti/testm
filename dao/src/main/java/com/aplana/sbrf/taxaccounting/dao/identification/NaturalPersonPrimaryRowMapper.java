package com.aplana.sbrf.taxaccounting.dao.identification;

import com.aplana.sbrf.taxaccounting.model.identification.NaturalPerson;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookCountry;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookDocType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookTaxpayerState;
import org.springframework.jdbc.core.RowMapper;

import java.util.Map;

/**
 * Обработчик запроса данных из ПНФ
 *
 * @author Andrey Drunk
 */
public abstract class NaturalPersonPrimaryRowMapper implements RowMapper<NaturalPerson> {

    private Map<String, RefBookCountry> countryCodeMap;

    private Map<String, RefBookTaxpayerState> taxpayerStatusCodeMap;

    private Map<String, RefBookDocType> docTypeCodeMap;

    protected Logger logger = new Logger();

    public Map<String, RefBookCountry> getCountryCodeMap() {
        return countryCodeMap;
    }

    public void setCountryCodeMap(Map<String, RefBookCountry> countryCodeMap) {
        this.countryCodeMap = countryCodeMap;
    }

    public Map<String, RefBookTaxpayerState> getTaxpayerStatusCodeMap() {
        return taxpayerStatusCodeMap;
    }

    public void setTaxpayerStatusCodeMap(Map<String, RefBookTaxpayerState> taxpayerStatusCodeMap) {
        this.taxpayerStatusCodeMap = taxpayerStatusCodeMap;
    }

    public Map<String, RefBookDocType> getDocTypeCodeMap() {
        return docTypeCodeMap;
    }

    public void setDocTypeCodeMap(Map<String, RefBookDocType> docTypeCodeMap) {
        this.docTypeCodeMap = docTypeCodeMap;
    }

    public Logger getLogger() {
        return logger;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    public RefBookCountry getCountryByCode(String code) {
        if (code != null) {

            if (countryCodeMap == null) {
                logger.warn("Не проинициализирован кэш справочника 'ОК 025-2001 (Общероссийский классификатор стран мира)'!");
                return new RefBookCountry();
            }

            RefBookCountry result = countryCodeMap.get(code);

            if (result == null) {
                logger.warn("В справочнике 'ОК 025-2001 (Общероссийский классификатор стран мира)' не найдена страна с кодом: " + code);
                result = new RefBookCountry();
            }

            return result;
        } else {
            return new RefBookCountry();
        }
    }

    public RefBookTaxpayerState getTaxpayerStatusByCode(String code) {

        if (code != null) {

            if (taxpayerStatusCodeMap == null) {
                logger.warn("Не проинициализирован кэш справочника 'Статусы налогоплательщика'!");
                return new RefBookTaxpayerState();
            }

            RefBookTaxpayerState result = taxpayerStatusCodeMap.get(code);
            if (result == null) {
                logger.warn("В справочнике 'Статусы налогоплательщика' не найден статус с кодом: " + code);
                result = new RefBookTaxpayerState();
            }
            return result;

        } else {
            return new RefBookTaxpayerState();
        }
    }

    public RefBookDocType getDocTypeByCode(String code, NaturalPerson person) {
        if (code != null) {

            if (docTypeCodeMap == null) {
                logger.warn("Не проинициализирован кэш справочника 'Коды документов'!");
                return null;
            }

            RefBookDocType result = docTypeCodeMap.get(code);

            if (result == null) {
                logger.warn("В справочнике 'Коды документов' не найден документ с кодом: " + code);
            }

            return result;
        } else {
            return null;
        }
    }


}
