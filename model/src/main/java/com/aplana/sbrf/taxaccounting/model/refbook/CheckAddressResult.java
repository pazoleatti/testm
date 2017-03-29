package com.aplana.sbrf.taxaccounting.model.refbook;

import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPerson;

/**
 * Результат проверки адреса по справочнику фиас
 *
 * @author Andrey Drunk
 */
public class CheckAddressResult {

    /**
     * Исходная запись, используется для проверки адреса
     */
    private NdflPerson ndflPerson;

    /**
     * Адрес из первичной формы одной строкой
     */
    private String primaryAddressPath;

    /**
     * Район
     */
    private AddressObject area;

    /**
     * Город
     */
    private AddressObject city;

    /**
     * Населенный пункт
     */
    private AddressObject location;

    /**
     * Улица
     */
    private AddressObject street;

    /**
     *
     */
    private boolean regionValid;

    /**
     *
     */
    private boolean postalCodeValid;

    public boolean isRegionValid() {
        return regionValid;
    }

    public void setRegionValid(boolean regionValid) {
        this.regionValid = regionValid;
    }

    public boolean isPostalCodeValid() {
        return postalCodeValid;
    }

    public void setPostalCodeValid(boolean postalCodeValid) {
        this.postalCodeValid = postalCodeValid;
    }

    public NdflPerson getNdflPerson() {
        return ndflPerson;
    }

    public void setNdflPerson(NdflPerson ndflPerson) {
        this.ndflPerson = ndflPerson;
    }

    public String getPrimaryAddressPath() {
        return primaryAddressPath;
    }

    public void setPrimaryAddressPath(String primaryAddressPath) {
        this.primaryAddressPath = primaryAddressPath;
    }

    public AddressObject getArea() {
        return area;
    }

    public void setArea(AddressObject area) {
        this.area = area;
    }

    public AddressObject getCity() {
        return city;
    }

    public void setCity(AddressObject city) {
        this.city = city;
    }

    public AddressObject getLocation() {
        return location;
    }

    public void setLocation(AddressObject location) {
        this.location = location;
    }

    public AddressObject getStreet() {
        return street;
    }

    public void setStreet(AddressObject street) {
        this.street = street;
    }
}
