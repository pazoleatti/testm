package com.aplana.sbrf.taxaccounting.model.filter.refbook;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class RefBookPersonFilter implements Serializable {

    private Long versionId;
    private String lastName;
    private String firstName;
    private String middleName;
    private Date birthDateFrom;
    private Date birthDateTo;
    private List<Long> terBanks;
    private List<Long> documentTypes;
    private String documentNumber;
    private List<Long> citizenshipCountries;
    private List<Long> taxpayerStates;
    private List<Long> sourceSystems;
    private String id;
    private String inp;
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
    private List<Long> countries;
    private String foreignAddress;

    private Boolean allVersions;
    private Date versionDate;

    private Boolean duplicates;

    public Long getVersionId() {
        return versionId;
    }

    public void setVersionId(Long versionId) {
        this.versionId = versionId;
    }

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

    public List<Long> getTerBanks() {
        return terBanks;
    }

    public void setTerBanks(List<Long> terBanks) {
        this.terBanks = terBanks;
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

    public List<Long> getCitizenshipCountries() {
        return citizenshipCountries;
    }

    public void setCitizenshipCountries(List<Long> citizenshipCountries) {
        this.citizenshipCountries = citizenshipCountries;
    }

    public List<Long> getTaxpayerStates() {
        return taxpayerStates;
    }

    public void setTaxpayerStates(List<Long> taxpayerStates) {
        this.taxpayerStates = taxpayerStates;
    }

    public List<Long> getSourceSystems() {
        return sourceSystems;
    }

    public void setSourceSystems(List<Long> sourceSystems) {
        this.sourceSystems = sourceSystems;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getInp() {
        return inp;
    }

    public void setInp(String inp) {
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

    public List<Long> getCountries() {
        return countries;
    }

    public void setCountries(List<Long> countries) {
        this.countries = countries;
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
