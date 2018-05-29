package com.aplana.sbrf.taxaccounting.model.refbook;

import java.util.Date;

/**
 * Физические лица
 *
 * @author dloshkarev
 */
public class RefBookPerson extends RefBookVersioned<Long> {
    //Имя
    private String firstName;
    //Фамилия
    private String lastName;
    //Отчество
    private String middleName;
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
    //Признак, показывающий, является ли ФЛ сотрудником Сбербанка
    private Integer employee;
    //Система-источник: ссылка на справочник кодов АС НУ
    private RefBookAsnu source;
    //Старый идентификатор ФЛ
    private Long oldId;

    /** Вспомогательные поля для улучшения производительности. Заполняются не во всех случаях*/
    //Адрес как текст, используется чтобы сразу получать данные из бд одним запросои без запросом на каждую запись
    private String addressAsText;
    //Серия и номер ДУЛ
    private String docNumber;

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

    public Integer getEmployee() {
        return employee;
    }

    public void setEmployee(Integer employee) {
        this.employee = employee;
    }

    public RefBookAsnu getSource() {
        return source;
    }

    public void setSource(RefBookAsnu source) {
        this.source = source;
    }

    public String getAddressAsText() {
        return addressAsText;
    }

    public void setAddressAsText(String addressAsText) {
        this.addressAsText = addressAsText;
    }

    public Long getOldId() {
        return oldId;
    }

    public void setOldId(Long oldId) {
        this.oldId = oldId;
    }

    public String getDocNumber() {
        return docNumber;
    }

    public void setDocNumber(String docNumber) {
        this.docNumber = docNumber;
    }


}
