package com.aplana.generatorTF.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by aokunev on 22.03.2018.
 */
public class OperationInfoTag {
    private Map<String, String> operationInfoTagAttributes;

    private List<Map<String, String>> incomeTaxInfoTagsAttributesList;

    private List<Map<String, String>> deductionInfoTagsAttributesList;

    private List<Map<String, String>> prepaymentInfoTagsAttributesList;

    public OperationInfoTag(Map<String, String> operationInfoTagAttributes) {
        this.operationInfoTagAttributes = operationInfoTagAttributes;
        incomeTaxInfoTagsAttributesList = new ArrayList<Map<String, String>>();
        deductionInfoTagsAttributesList = new ArrayList<Map<String, String>>();
        prepaymentInfoTagsAttributesList = new ArrayList<Map<String, String>>();
    }

    public int getIncomeTaxInfoTagsCount() {
        return incomeTaxInfoTagsAttributesList.size();
    }

    public Map<String, String> getIncomeTaxInfoTagAttributes(int index) {
        return incomeTaxInfoTagsAttributesList.get(index);
    }

    public void addIncomeTaxInfoTagAttributes(Map<String, String> attributes) {
        incomeTaxInfoTagsAttributesList.add(attributes);
    }

    public int getDeductionInfoTagsCount() {
        return deductionInfoTagsAttributesList.size();
    }

    public Map<String, String> getDeductionInfoTagAttributes(int index) {
        return deductionInfoTagsAttributesList.get(index);
    }

    public void addDeductionInfoTagAttributes(Map<String, String> attributes) {
        deductionInfoTagsAttributesList.add(attributes);
    }

    public int getPrepaymentInfoTagsCount() {
        return prepaymentInfoTagsAttributesList.size();
    }

    public Map<String, String> getPrepaymentInfoTagAttributes(int index) {
        return prepaymentInfoTagsAttributesList.get(index);
    }

    public void addPrepaymentInfoTagAttributes(Map<String, String> attributes) {
        prepaymentInfoTagsAttributesList.add(attributes);
    }

    public Map<String, String> getOperationInfoTagAttributes() {
        return operationInfoTagAttributes;
    }

    public List<Map<String, String>> getIncomeTaxInfoTagsAttributesList() {
        return incomeTaxInfoTagsAttributesList;
    }

    public List<Map<String, String>> getDeductionInfoTagsAttributesList() {
        return deductionInfoTagsAttributesList;
    }

    public List<Map<String, String>> getPrepaymentInfoTagsAttributesList() {
        return prepaymentInfoTagsAttributesList;
    }
}
