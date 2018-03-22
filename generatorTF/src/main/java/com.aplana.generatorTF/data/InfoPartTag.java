package com.aplana.generatorTF.data;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by aokunev on 22.03.2018.
 */
public class InfoPartTag {
    private Map<String, String> infoPartTagAttributes;

    private Map<String, String> incomeTagAttributes;

    private List<OperationInfoTag> operationInfoTags;

    public InfoPartTag(Map<String, String> infoPartTagAttributes) {
        this.incomeTagAttributes = new LinkedHashMap<String, String>();
        this.infoPartTagAttributes = infoPartTagAttributes;
        this.operationInfoTags = new LinkedList<OperationInfoTag>();
    }

    public Map<String, String> getInfoPartTagAttributes() {
        return infoPartTagAttributes;
    }

    public Map<String, String> getIncomeTagAttributes() {
        return incomeTagAttributes;
    }

    public void setIncomeTagAttributes(Map<String, String> incomeTagAttributes) {
        this.incomeTagAttributes.clear();
        for (Map.Entry<String, String> attr : incomeTagAttributes.entrySet()) {
            this.incomeTagAttributes.put(attr.getKey(), attr.getValue());
        }
    }

    public void addOperationInfoTag(OperationInfoTag operationInfoTag) {
        operationInfoTags.add(operationInfoTag);
    }

    public List<OperationInfoTag> getOperationInfoTags() {
        return operationInfoTags;
    }
}
