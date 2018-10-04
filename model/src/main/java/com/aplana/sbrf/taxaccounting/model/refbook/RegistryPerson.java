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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RegistryPerson)) return false;

        RegistryPerson that = (RegistryPerson) o;

        if (getState() != null ? !getState().equals(that.getState()) : that.getState() != null) return false;
        if (getRecordVersionTo() != null ? !getRecordVersionTo().equals(that.getRecordVersionTo()) : that.getRecordVersionTo() != null)
            return false;
        if (getLastName() != null ? !getLastName().equals(that.getLastName()) : that.getLastName() != null)
            return false;
        if (getFirstName() != null ? !getFirstName().equals(that.getFirstName()) : that.getFirstName() != null)
            return false;
        if (getMiddleName() != null ? !getMiddleName().equals(that.getMiddleName()) : that.getMiddleName() != null)
            return false;
        if (getBirthDate() != null ? !getBirthDate().equals(that.getBirthDate()) : that.getBirthDate() != null)
            return false;
        if (getCitizenship() != null ? !getCitizenship().equals(that.getCitizenship()) : that.getCitizenship() != null)
            return false;
        if (getReportDoc() != null ? !getReportDoc().equals(that.getReportDoc()) : that.getReportDoc() != null)
            return false;
        if (getInn() != null ? !getInn().equals(that.getInn()) : that.getInn() != null) return false;
        if (getInnForeign() != null ? !getInnForeign().equals(that.getInnForeign()) : that.getInnForeign() != null)
            return false;
        if (getSnils() != null ? !getSnils().equals(that.getSnils()) : that.getSnils() != null) return false;
        if (getTaxPayerState() != null ? !getTaxPayerState().equals(that.getTaxPayerState()) : that.getTaxPayerState() != null)
            return false;
        if (getSource() != null ? !getSource().equals(that.getSource()) : that.getSource() != null) return false;
        return getAddress() != null ? getAddress().equals(that.getAddress()) : that.getAddress() == null;
    }

    @Override
    public int hashCode() {
        int result = getState() != null ? getState().hashCode() : 0;
        result = 31 * result + (getRecordVersionTo() != null ? getRecordVersionTo().hashCode() : 0);
        result = 31 * result + (getLastName() != null ? getLastName().hashCode() : 0);
        result = 31 * result + (getFirstName() != null ? getFirstName().hashCode() : 0);
        result = 31 * result + (getMiddleName() != null ? getMiddleName().hashCode() : 0);
        result = 31 * result + (getBirthDate() != null ? getBirthDate().hashCode() : 0);
        result = 31 * result + (getCitizenship() != null ? getCitizenship().hashCode() : 0);
        result = 31 * result + (getReportDoc() != null ? getReportDoc().hashCode() : 0);
        result = 31 * result + (getInn() != null ? getInn().hashCode() : 0);
        result = 31 * result + (getInnForeign() != null ? getInnForeign().hashCode() : 0);
        result = 31 * result + (getSnils() != null ? getSnils().hashCode() : 0);
        result = 31 * result + (getTaxPayerState() != null ? getTaxPayerState().hashCode() : 0);
        result = 31 * result + (getSource() != null ? getSource().hashCode() : 0);
        result = 31 * result + (getAddress() != null ? getAddress().hashCode() : 0);
        return result;
    }

    /**
     * Перечисление обновляемых полей записи реестра ФЛ
     */
    public enum UpdatableField {
        VERSION("version"),
        LAST_NAME("lastName"),
        FIRST_NAME("firstName"),
        MIDDLE_NAME("middleName"),
        BIRTH_DATE("birthDate"),
        CITIZENSHIP("citizenship"),
        REPORT_DOC("reportDoc"),
        INN("inn"),
        INN_FOREIGN("innForeign"),
        SNILS("snils"),
        TAX_PAYER_STATE("taxPayerState"),
        SOURCE("source"),
        REGION_CODE("REGION_CODE"),
        POSTAL_CODE("POSTAL_CODE"),
        DISTRICT("DISTRICT"),
        CITY("CITY"),
        LOCALITY("LOCALITY"),
        STREET("STREET"),
        HOUSE("HOUSE"),
        BUILD("BUILD"),
        APPARTMENT("APPARTMENT"),
        COUNTRY_ID("COUNTRY_ID"),
        ADDRESS("ADDRESS"),
        VIP("vip");

        private String alias;

        UpdatableField(String alias) {
            this.alias = alias;
        }

        public String getAlias() {
            return alias;
        }
    }

}
