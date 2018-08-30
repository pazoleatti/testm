package com.aplana.sbrf.taxaccounting.model.refbook;

import com.aplana.sbrf.taxaccounting.model.Permissive;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;

/**
 * Физические лица
 */
public class RefBookPerson extends RefBookVersioned<Long> {
    //Имя
    private String firstName;
    //Фамилия
    private String lastName;
    //Отчество
    private String middleName;
    //ИНН в РФ
    private Permissive<String> inn;
    //ИНН в стране гражданства
    private Permissive<String> innForeign;
    //СНИЛС
    private Permissive<String> snils;
    //Статус налогоплательщика
    private RefBookTaxpayerState taxpayerState;
    //Дата рождения
    private Date birthDate;
    //Место рождения
    private String birthPlace;
    //Гражданство
    private RefBookCountry citizenship;
    //Место жительства
    private Permissive<RefBookAddress> address;
    //Признак, показывающий, является ли ФЛ сотрудником Сбербанка
    private Integer employee;
    private RefBookAddress address;
    //Система-источник: ссылка на справочник кодов АС НУ
    private RefBookAsnu source;
    //Старый идентификатор ФЛ
    private Long oldId;
    //Признак, является ли ФЛ VIP-ом
    private Boolean vip;

    /*
     * Вспомогательные поля для улучшения производительности. Заполняются не во всех случаях
     */
    //Адрес как текст, используется чтобы сразу получать данные из бд одним запросои без запросом на каждую запись
    private String addressAsText;
    //Название ДУЛ
    private Permissive<String> docName;
    //Серия и номер ДУЛ
    private Permissive<String> docNumber;


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
        return inn.value();
    }

    public void setInn(Permissive<String> inn) {
        this.inn = inn;
    }

    public String getInnForeign() {
        return innForeign.value();
    }

    public void setInnForeign(Permissive<String> innForeign) {
        this.innForeign = innForeign;
    }

    public String getSnils() {
        return snils.value();
    }

    public void setSnils(Permissive<String> snils) {
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
        return address.value();
    }

    public void setAddress(Permissive<RefBookAddress> address) {
        this.address = address;
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

    public String getDocName() {
        return docName.value();
    }

    public void setDocName(Permissive<String> docName) {
        this.docName = docName;
    }

    public String getDocNumber() {
        return docNumber.value();
    }

    public void setDocNumber(Permissive<String> docNumber) {
        this.docNumber = docNumber;
    }

    public Boolean isVip() {
        return vip;
    }

    public void setVip(Boolean vip) {
        this.vip = vip;
    }

    /*
     * Набор методов для сериализации объекта в JSON
     */

    @JsonProperty("inn")
    public Permissive<String> getInnForJson() {
        return inn;
    }

    @JsonProperty("innForeign")
    public Permissive<String> getInnForeignForJson() {
        return innForeign;
    }

    @JsonProperty("snils")
    public Permissive<String> getSnilsForJson() {
        return snils;
    }

    @JsonProperty("address")
    public Permissive<RefBookAddress> getAddressForJson() {
        return address;
    }

    @JsonProperty("docName")
    public Permissive<String> getDocNameForJson() {
        return docName;
    }

    @JsonProperty("docNumber")
    public Permissive<String> getDocNumberForJson() {
        return docNumber;
    }
}
