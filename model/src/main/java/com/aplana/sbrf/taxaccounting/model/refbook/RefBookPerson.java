package com.aplana.sbrf.taxaccounting.model.refbook;

import java.util.Date;

/**
 * Физические лица
 *
 * @author dloshkarev
 */
public class RefBookPerson extends RefBookSimple<Long> {
    //Имя
    private String firstName;
    //Фамилия
    private String lastName;
    //Отчество
    private String middleName;
    //Пол
    private Byte sex;
    //ИНН в РФ
    private String inn;
    //ИНН в стране гражданства
    private String innForeign;
    //СНИЛС
    private String snils;
    //Статус налогоплательщика
    private RefBookTaxpayerState taxpayerState;
    //Дата рождения
    private Date birthDate;
    //Место рождения
    private String birthPlace;
    //Гражданство
    private RefBookCountry citizenship;
    //Место жительства
    private RefBookAddress address;
    //Признак застрахованного лица в системе обязательного пенсионного страхования
    private Boolean pension;
    //Признак застрахованного лица в системе обязательного медицинского страхования
    private Boolean medical;
    //Признак застрахованного лица в системе обязательного социального страхования
    private Boolean social;
    //Признак, показывающий, является ли ФЛ сотрудником Сбербанка
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
