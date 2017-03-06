package com.aplana.sbrf.taxaccounting.model.identification;

import com.aplana.sbrf.taxaccounting.model.IdentityObject;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * @author Andrey Drunk
 */
public class Address extends IdentityObject<Long> {

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
    private Country country;
    private String countryCode;
    private String addressIno;


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

    public Country getCountry() {
        return country;
    }

    public void setCountry(Country country) {
        this.country = country;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getAddressIno() {
        return addressIno;
    }

    public void setAddressIno(String addressIno) {
        this.addressIno = addressIno;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SIMPLE_STYLE)
                .append("addressType", addressType)
                .append("regionCode", regionCode)
                .append("postalCode", postalCode)
                .append("district", district)
                .append("city", city)
                .append("locality", locality)
                .append("street", street)
                .append("house", house)
                .append("build", build)
                .append("appartment", appartment)
                .append("country", country)
                .append("countryCode", countryCode)
                .append("addressIno", addressIno)
                .toString();
    }
}
