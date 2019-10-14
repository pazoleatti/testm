package com.aplana.sbrf.taxaccounting.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TaxFormType {
    NDFL_PRIMARY(2, "РНУ_НДФЛ", "Регистр налогового учёта налога на доходы физических лиц"),
    NDFL_2_1(3, "2-НДФЛ (1)", "2-НДФЛ с признаком 1"),
    NDFL_2_2(4, "2-НДФЛ (2)", "2-НДФЛ с признаком 2"),
    NDFL_6(5, "6-НДФЛ", "6-НДФЛ"),
    APPLICATION_2(7, "Приложение 2", "Приложение 2");

    private int id;
    private String code;
    private String name;

    public static TaxFormType getById(int id) {
        for (TaxFormType taxFormType: values()) {
            if (id == taxFormType.getId()) {
                return taxFormType;
            }
        }
        throw new IllegalArgumentException("Wrong Tax Form Type id: '" + id + "'");
    }

}
