package com.aplana.sbrf.taxaccounting.model.util.impl;

import com.aplana.sbrf.taxaccounting.model.identification.IdentityPerson;
import com.aplana.sbrf.taxaccounting.model.util.BaseWeightCalculator;
import com.aplana.sbrf.taxaccounting.model.util.WeightCalculator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Сравнивает двух физлиц по весам
 */
public class PersonDataWeightCalculator implements WeightCalculator<IdentityPerson> {

    private Map<String, Double> result = new HashMap<String, Double>();

    private List<BaseWeightCalculator> compareList;

    public PersonDataWeightCalculator(List<BaseWeightCalculator> compareList) {
        this.compareList = compareList;
    }

    @Override
    public double calc(IdentityPerson a, IdentityPerson b) {
        double summWeight = 0D;
        double summParameterWeight = 0D;
        for (BaseWeightCalculator calculator : compareList) {
            double weight = calculator.calc(a, b);
            result.put(calculator.getName(), weight);
            summWeight += weight;
            summParameterWeight += calculator.getWeight();
        }
        return summWeight / summParameterWeight;
    }

    public Map<String, Double> getResult() {
        return result;
    }

}
