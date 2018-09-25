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
    //Зарубежный адрес
    private Permissive<RefBookAddress> foreignAddress;
    //Система-источник: ссылка на справочник кодов АС НУ
    private RefBookAsnu source;
    //Старый идентификатор ФЛ
    private Long oldId;
    //Признак, является ли ФЛ VIP-ом
    private Boolean vip;


    //Тип ДУЛ
    private Permissive<RefBookDocType> docType;
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
        return address == null ? null : address.value();
    }

    public void setAddress(Permissive<RefBookAddress> address) {
        this.address = address;
    }

    public void setForeignAddress(Permissive<RefBookAddress> foreignAddress) {
        this.foreignAddress = foreignAddress;
    }

    public RefBookAddress getForeignAddress() {
        return foreignAddress == null ? null : foreignAddress.value();
    }

    public RefBookAsnu getSource() {
        return source;
    }

    public void setSource(RefBookAsnu source) {
        this.source = source;
    }

    public Long getOldId() {
        return oldId;
    }

    public void setOldId(Long oldId) {
        this.oldId = oldId;
    }

    public RefBookDocType getDocType() {
        return docType.value();
    }

    public void setDocType(Permissive<RefBookDocType> docType) {
        this.docType = docType;
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

    public void forbid() {
        setDocNumber(Permissive.<String>forbidden());
        setDocType(Permissive.<RefBookDocType>forbidden());
        setInn(Permissive.<String>forbidden());
        setInnForeign(Permissive.<String>forbidden());
        setSnils(Permissive.<String>forbidden());
        setAddress(Permissive.<RefBookAddress>forbidden());
        setForeignAddress(Permissive.<RefBookAddress>forbidden());
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

    @JsonProperty("foreignAddress")
    public Permissive<RefBookAddress> getForeignAddressForJson() {
        return foreignAddress;
    }

    @JsonProperty("docType")
    public Permissive<RefBookDocType> getDocTypeForJson() {
        return docType;
    }

    @JsonProperty("docNumber")
    public Permissive<String> getDocNumberForJson() {
        return docNumber;
    }
}
