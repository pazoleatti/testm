package com.aplana.sbrf.taxaccounting.model.refbook;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Адрес
 */
@Getter
@Setter
@NoArgsConstructor
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Address {
    /**
     * Код региона
     */
    private String regionCode;
    /**
     * Почтовый индекс
     */
    private String postalCode;
    /**
     * Район
     */
    private String district;
    /**
     * Город
     */
    private String city;
    /**
     * Населенный пункт
     */
    private String locality;
    /**
     * Улица
     */
    private String street;
    /**
     * Номер дома
     */
    private String house;
    /**
     * Номер корпуса
     */
    private String build;
    /**
     * Номер квартиры
     */
    private String appartment;

    /**
     * Адрес вне РФ
     */
    private RefBookCountry country;
    private String addressIno;

}
