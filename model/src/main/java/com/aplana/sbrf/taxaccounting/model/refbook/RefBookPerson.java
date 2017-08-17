package com.aplana.sbrf.taxaccounting.model.refbook;

import com.aplana.sbrf.taxaccounting.model.IdentityObject;

import java.util.Date;

/**
 * Физические лица
 * @author dloshkarev
 */
public class RefBookPerson extends RefBookSimple<Long> {
    private String firstName;
    private String lastName;
    private String middleName;
    private Byte sex;
    private String inn;
    private String innForeign;
    private String snils;
    private RefBookTaxpayerState taxpayerState;
    private Date birthDate;
    private String birthPlace;
    private RefBookCountry citizenship;
    private RefBookAddress address;
    private Boolean pension;
    private Boolean medical;
    private Boolean social;
    private Boolean employee;


    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public Byte getSex() {
        return sex;
    }

    public void setSex(Byte sex) {
        this.sex = sex;
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

    public RefBookTaxpayerState getTaxpayerState() {
        return taxpayerState;
    }

    public void setTaxpayerState(RefBookTaxpayerState taxpayerState) {
        this.taxpayerState = taxpayerState;
    }

    public Date getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }

    public String getBirthPlace() {
        return birthPlace;
    }

    public void setBirthPlace(String birthPlace) {
        this.birthPlace = birthPlace;
    }

    public RefBookCountry getCitizenship() {
        return citizenship;
    }

    public void setCitizenship(RefBookCountry citizenship) {
        this.citizenship = citizenship;
    }

    public RefBookAddress getAddress() {
        return address;
    }

    public void setAddress(RefBookAddress address) {
        this.address = address;
    }

    public Boolean getPension() {
        return pension;
    }

    public void setPension(Boolean pension) {
        this.pension = pension;
    }

    public Boolean getMedical() {
        return medical;
    }

    public void setMedical(Boolean medical) {
        this.medical = medical;
    }

    public Boolean getSocial() {
        return social;
    }

    public void setSocial(Boolean social) {
        this.social = social;
    }

    public Boolean getEmployee() {
        return employee;
    }

    public void setEmployee(Boolean employee) {
        this.employee = employee;
    }
}
