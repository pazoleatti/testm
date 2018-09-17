package com.aplana.sbrf.taxaccounting.model.filter.refbook;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class RefBookPersonFilter implements Serializable {

    private String lastName;
    private String firstName;
    private String middleName;
    private Date birthDateFrom;
    private Date birthDateTo;
    private Long terBankId;
    private List<Long> documentTypes;
    private String documentNumber;
    private Long citizenshipCountryId;
    private Long taxpayerStateId;
    private Long sourceId;
    private String id;
    private Long inp;
    private String inn;
    private String innForeign;
    private String snils;
    private Boolean vip;

    // Адрес
    private String postalCode;
    private String region;
    private String district;
    private String city;
    private String locality;
    private String street;
    // Адрес за пределами РФ
    private String country;
    private String foreignAddress;

    private Boolean allVersions;
    private Date versionDate;

    private Boolean duplicates;

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public Date getBirthDateFrom() {
        return birthDateFrom;
    }

    public void setBirthDateFrom(Date birthDateFrom) {
        this.birthDateFrom = birthDateFrom;
    }

    public Date getBirthDateTo() {
        return birthDateTo;
    }

    public void setBirthDateTo(Date birthDateTo) {
        this.birthDateTo = birthDateTo;
    }

    public Long getTerBankId() {
        return terBankId;
    }

    public void setTerBankId(Long terBankId) {
        this.terBankId = terBankId;
    }

    public List<Long> getDocumentTypes() {
        return documentTypes;
    }

    public void setDocumentTypes(List<Long> documentTypes) {
        this.documentTypes = documentTypes;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public Long getCitizenshipCountryId() {
        return citizenshipCountryId;
    }

    public void setCitizenshipCountryId(Long citizenshipCountryId) {
        this.citizenshipCountryId = citizenshipCountryId;
    }

    public Long getTaxpayerStateId() {
        return taxpayerStateId;
    }

    public void setTaxpayerStateId(Long taxpayerStateId) {
        this.taxpayerStateId = taxpayerStateId;
    }

    public Long getSourceId() {
        return sourceId;
    }

    public void setSourceId(Long sourceId) {
        this.sourceId = sourceId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getInp() {
        return inp;
    }

    public void setInp(Long inp) {
        this.inp = inp;
    }

    public String getInn() {
        return inn;
    }

    public void setInn(String inn) {
        this.inn = inn;
    }

    public String getInnForeign() {
        return innForeign;
    }

    public void setInnForeign(String innForeign) {
        this.innForeign = innForeign;
    }

    public String getSnils() {
        return snils;
    }

    public void setSnils(String snils) {
        this.snils = snils;
    }

    public Boolean getVip() {
        return vip;
    }

    public void setVip(Boolean vip) {
        this.vip = vip;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
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

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getForeignAddress() {
        return foreignAddress;
    }

    public void setForeignAddress(String foreignAddress) {
        this.foreignAddress = foreignAddress;
    }

    public Boolean isAllVersions() {
        return allVersions;
    }

    public void setAllVersions(Boolean allVersions) {
        this.allVersions = allVersions;
    }

    public Date getVersionDate() {
        return versionDate;
    }

    public void setVersionDate(Date versionDate) {
        this.versionDate = versionDate;
    }

    public Boolean getDuplicates() {
        return duplicates;
    }

    public void setDuplicates(Boolean duplicates) {
        this.duplicates = duplicates;
    }
}
