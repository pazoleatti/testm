package com.aplana.sbrf.taxaccounting.model.refbook;

import com.aplana.sbrf.taxaccounting.model.Permissive;

import java.util.Date;
import java.util.Map;

/**
 * Модель физлица для работы с реестром физлиц. Записи реестра физлиц содержат ссылки на таблицы справочники.
 * Чтобы работать с таким ссылками как с объектами, ссылки разыменовываются и объекты представляются в виде Мапы строка-значение справочника.
 */
public class RegistryPerson extends PermissivePerson {

    /**
     * Исходный идентификатор физлица
     */
    private Long oldId;

    /**
     * Статус из справочника
     */
    private Integer state;

    /**
     * Окончание даты действия версии
     */
    private Date recordVersionTo;

    /**
     * Фамилия
     */
    private String lastName;

    /**
     * Имя
     */
    private String firstName;

    /**
     * Отчество
     */
    private String middleName;

    /**
     * Дата рождения
     */
    private Date birthDate;

    /**
     * Гражданство
     */
    private Permissive<Map<String, RefBookValue>> citizenship;

    /**
     * Документ включаемый в отчетность
     */
    private Permissive<Map<String, RefBookValue>> reportDoc;

    /**
     * ИНН
     */
    private Permissive<String> inn;

    /**
     * ИНН в иностранном государстве
     */
    private Permissive<String> innForeign;

    /**
     * СНИЛС
     */
    private Permissive<String> snils;

    /**
     * Статус налогоплательщика
     */
    private Permissive<Map<String, RefBookValue>> taxPayerState;

    /**
     * Система-источник
     */
    private Map<String, RefBookValue> source;

    /**
     * Адрес
     */
    private Permissive<Map<String, RefBookValue>> address;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOldId() {
        return oldId;
    }

    public void setOldId(Long oldId) {
        this.oldId = oldId;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public Date getRecordVersionTo() {
        return recordVersionTo;
    }

    public void setRecordVersionTo(Date recordVersionTo) {
        this.recordVersionTo = recordVersionTo;
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

    public Date getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }

    public Permissive<Map<String, RefBookValue>> getCitizenship() {
        return citizenship;
    }

    public void setCitizenship(Permissive<Map<String, RefBookValue>> citizenship) {
        this.citizenship = citizenship;
    }

    public Permissive<Map<String, RefBookValue>> getReportDoc() {
        return reportDoc;
    }

    public void setReportDoc(Permissive<Map<String, RefBookValue>> reportDoc) {
        this.reportDoc = reportDoc;
    }

    public Permissive<String> getInn() {
        return inn;
    }

    public void setInn(Permissive<String> inn) {
        this.inn = inn;
    }

    public Permissive<String> getInnForeign() {
        return innForeign;
    }

    public void setInnForeign(Permissive<String> innForeign) {
        this.innForeign = innForeign;
    }

    public Permissive<String> getSnils() {
        return snils;
    }

    public void setSnils(Permissive<String> snils) {
        this.snils = snils;
    }

    public Permissive<Map<String, RefBookValue>> getTaxPayerState() {
        return taxPayerState;
    }

    public void setTaxPayerState(Permissive<Map<String, RefBookValue>> taxPayerState) {
        this.taxPayerState = taxPayerState;
    }

    public Map<String, RefBookValue> getSource() {
        return source;
    }

    public void setSource(Map<String, RefBookValue> source) {
        this.source = source;
    }

    public Permissive<Map<String, RefBookValue>> getAddress() {
        return address;
    }

    public void setAddress(Permissive<Map<String, RefBookValue>> address) {
        this.address = address;
    }

}
