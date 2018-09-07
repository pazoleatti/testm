package com.aplana.sbrf.taxaccounting.model.refbook;

/**
 * Справочник "Адреса физических лиц"
 *
 * @author Andrey Drunk
 */
public class RefBookAddress extends RefBookSimple<Long> {

    //Тип адреса
    private Integer addressType;
    //Код региона
    private String regionCode;
    //Почтовый индекс
    private String postalCode;
    //Район
    private String district;
    //Город
    private String city;
    //Населенный пункт
    private String locality;
    //Улица
    private String street;
    //Номер дома
    private String house;
    //Номер корпуса (строения)
    private String build;
    //Номер квартиры
    private String apartment;
    //Страна проживания
    private RefBookCountry country;
    //Адрес
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

    public String getApartment() {
        return apartment;
    }

    public void setApartment(String apartment) {
        this.apartment = apartment;
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
