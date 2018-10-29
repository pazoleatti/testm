package com.aplana.sbrf.taxaccounting.model.refbook;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * @author Andrey Drunk
 */
@Getter
@Setter
@NoArgsConstructor
@ToString
public class Address {

    private String regionCode;
    private String postalCode;
    private String district; //Район
    private String city;
    private String locality;
    private String street;
    private String house;
    private String build;
    private String appartment;

    /**
     * Адрес вне РФ
     */
    private RefBookCountry country;
    private String addressIno;

}
