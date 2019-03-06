package com.aplana.sbrf.taxaccounting.dao.identification;

import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAsnu;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookCountry;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookDocType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookTaxpayerState;

import java.util.Map;

/**
 * Класс для хранения кэшей справочников
 */
public class RefDataHolder {
    /**
     * Кэш справочника страны
     */
    private Map<Long, RefBookCountry> countryMap;

    /**
     * Кэш справочника статусы Налогоплателищика
     */
    private Map<Long, RefBookTaxpayerState> taxpayerStatusMap;

    /**
     * Кэш справочника типы документов
     */
    private Map<Long, RefBookDocType> docTypeMap;

    private Map<Long, RefBookAsnu> asnuMap;

    public Map<Long, RefBookCountry> getCountryMap() {
        return countryMap;
    }

    public void setCountryMap(Map<Long, RefBookCountry> countryMap) {
        this.countryMap = countryMap;
    }

    public Map<Long, RefBookTaxpayerState> getTaxpayerStatusMap() {
        return taxpayerStatusMap;
    }

    public void setTaxpayerStatusMap(Map<Long, RefBookTaxpayerState> taxpayerStatusMap) {
        this.taxpayerStatusMap = taxpayerStatusMap;
    }

    public Map<Long, RefBookDocType> getDocTypeMap() {
        return docTypeMap;
    }

    public void setDocTypeMap(Map<Long, RefBookDocType> docTypeMap) {
        this.docTypeMap = docTypeMap;
    }

    public Map<Long, RefBookAsnu> getAsnuMap() {
        return asnuMap;
    }

    public void setAsnuMap(Map<Long, RefBookAsnu> asnuMap) {
        this.asnuMap = asnuMap;
    }

    public RefBookTaxpayerState getTaxpayerStatusById(Long taxpayerStatusId) {
        if (taxpayerStatusId != null) {
            return taxpayerStatusMap != null ? taxpayerStatusMap.get(taxpayerStatusId) : new RefBookTaxpayerState(taxpayerStatusId, null);
        } else {
            return new RefBookTaxpayerState();
        }
    }

    public RefBookCountry getCountryById(Long countryId) {
        if (countryId != null) {
            return countryMap != null ? countryMap.get(countryId) : new RefBookCountry(countryId, null);
        } else {
            return new RefBookCountry();
        }
    }

    public RefBookDocType getDocTypeById(Long docTypeId) {
        if (docTypeId != null) {
            RefBookDocType refBookDocType = new RefBookDocType();
            refBookDocType.setId(docTypeId);
            refBookDocType.setCode(null);
            return docTypeMap != null ? docTypeMap.get(docTypeId) : refBookDocType;
        } else {
            return null;
        }
    }
}
