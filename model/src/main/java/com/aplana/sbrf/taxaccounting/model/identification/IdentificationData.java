package com.aplana.sbrf.taxaccounting.model.identification;

import com.aplana.sbrf.taxaccounting.model.log.Logger;

import java.util.List;
import java.util.Map;

/**
 * Класс инкапсулирующий данные для идентификации ФЛ
 */
public class IdentificationData {

    private NaturalPerson naturalPerson;

    private List<NaturalPerson> refBookPersonList;

    private int tresholdValue;

    private long declarationDataAsnuId;

    /**
     * Кэш проритетов АСНУ. Ключ id ref_book-asnu, значение приоритет
     */
    private Map<Long, Integer> priorityMap;

    public NaturalPerson getNaturalPerson() {
        return naturalPerson;
    }

    public void setNaturalPerson(NaturalPerson naturalPerson) {
        this.naturalPerson = naturalPerson;
    }

    public List<NaturalPerson> getRefBookPersonList() {
        return refBookPersonList;
    }

    public void setRefBookPersonList(List<NaturalPerson> refBookPersonList) {
        this.refBookPersonList = refBookPersonList;
    }

    public int getTresholdValue() {
        return tresholdValue;
    }

    public void setTresholdValue(int tresholdValue) {
        this.tresholdValue = tresholdValue;
    }

    public long getDeclarationDataAsnuId() {
        return declarationDataAsnuId;
    }

    public void setDeclarationDataAsnuId(long declarationDataId) {
        this.declarationDataAsnuId = declarationDataId;
    }

    public Map<Long, Integer> getPriorityMap() {
        return priorityMap;
    }

    public void setPriorityMap(Map<Long, Integer> priorityMap) {
        this.priorityMap = priorityMap;
    }
}
