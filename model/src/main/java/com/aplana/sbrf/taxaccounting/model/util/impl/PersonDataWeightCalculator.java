package com.aplana.sbrf.taxaccounting.model.util.impl;

import com.aplana.sbrf.taxaccounting.model.identification.IdentityPerson;
import com.aplana.sbrf.taxaccounting.model.util.BaseWeigthCalculator;
import com.aplana.sbrf.taxaccounting.model.util.WeigthCalculator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Сравнивает двух физлиц по весам
 */
public class PersonDataWeightCalculator implements WeigthCalculator<IdentityPerson> {

    private Map<String, Double> result = new HashMap<String, Double>();

    private List<BaseWeigthCalculator> compareList;

    public PersonDataWeightCalculator(List<BaseWeigthCalculator> compareList) {
        this.compareList = compareList;
    }

    @Override
    public double calc(IdentityPerson a, IdentityPerson b) {
        double summWeigth = 0D;
        double summParameterWeigt = 0D;
        for (BaseWeigthCalculator calculator : compareList) {
            double weigth = calculator.calc(a, b);
            result.put(calculator.getName(), weigth);
            summWeigth += weigth;
            summParameterWeigt += calculator.getWeigth();
        }
        return summWeigth / summParameterWeigt;
    }

    public Map<String, Double> getResult() {
        return result;
    }

}
