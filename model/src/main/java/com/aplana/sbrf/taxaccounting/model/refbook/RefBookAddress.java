package com.aplana.sbrf.taxaccounting.model.refbook;

import com.aplana.sbrf.taxaccounting.model.IdentityObject;

/**
 * @author Andrey Drunk
 */
public class RefBookAddress extends RefBookSimple<Long>{

    /**
     * Адрес физлица
     */
    private Integer addressType;

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
    private String address;


    public Integer getAddressType() {
        return addressType;
    }

    public void setAddressType(Integer addressType) {
        this.addressType = addressType;
    }

    public String getRegionCode() {
        return regionCode;
    }

    public void setRegionCode(String regionCode) {
        this.regionCode = regionCode;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getLocality() {
        return locality;
    }

    public void setLocality(String locality) {
        this.locality = locality;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getHouse() {
        return house;
    }

    public void setHouse(String house) {
        this.house = house;
    }

    public String getBuild() {
        return build;
    }

    public void setBuild(String build) {
        this.build = build;
    }

    public String getAppartment() {
        return appartment;
    }

    public void setAppartment(String appartment) {
        this.appartment = appartment;
    }

    public RefBookCountry getCountry() {
        return country;
    }

    public void setCountry(RefBookCountry country) {
        this.country = country;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAddress() {
        return address;
    }
}
