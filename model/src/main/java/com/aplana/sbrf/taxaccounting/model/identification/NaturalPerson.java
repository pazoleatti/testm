package com.aplana.sbrf.taxaccounting.model.identification;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Andrey Drunk
 */
public class NaturalPerson extends RefBookObject implements IdentityPerson {

    /**
     * Идентификатор ФЛ в первичной форме
     */
    private Long primaryPersonId;


    /**
     * Фамилия
     */
    private String lastName;

    /**
     * Имя
     */
    private String firstName;

    /**
     * Отчетство
     */
    private String middleName;

    /**
     * ИНН
     */
    private String inn;

    /**
     * ИНН ИН
     */
    private String innForeign;

    /**
     * Снилс
     */
    private String snils;

    /**
     * Номер ФЛ
     */
    private Integer num;

    /**
     * Статус физлица
     */
    private TaxpayerStatus taxPayerStatus;

    /**
     * Гражданство
     */
    private Country citizenship;

    /**
     * Дата рождения
     */
    private Date birthDate;

    /**
     * Список идентификаторов ФЛ
     */
    private List<PersonIdentifier> personIdentityList;

    /**
     * Список документов ФЛ
     */
    private List<PersonDocument> personDocumentList;

    /**
     * Адрес фл
     */
    private Address address;

    private Integer employee;

    /**
     * Система источник
     */
    private Long sourceId;


    /**
     * Поле для хранения веса записи при идентификации
     */
    private Double weight;

    /**
     * Флаг указывающий на то что запись в справочнике не надо обновлять
     */
    private boolean needUpdate = true;

    public NaturalPerson() {
        this.personIdentityList = new ArrayList<PersonIdentifier>();
        this.personDocumentList = new ArrayList<PersonDocument>();
    }

    public Integer getNum() {
        return num;
    }

    public void setNum(Integer num) {
        this.num = num;
    }

    public Long getPrimaryPersonId() {
        return primaryPersonId;
    }

    public void setPrimaryPersonId(Long primaryPersonId) {
        this.primaryPersonId = primaryPersonId;
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

    public TaxpayerStatus getTaxPayerStatus() {
        return taxPayerStatus;
    }

    public void setTaxPayerStatus(TaxpayerStatus taxPayerStatusId) {
        this.taxPayerStatus = taxPayerStatusId;
    }

    public Country getCitizenship() {
        return citizenship;
    }

    public void setCitizenship(Country citizenship) {
        this.citizenship = citizenship;
    }

    public Date getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }


    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public Integer getEmployee() {
        return employee;
    }


    public void setEmployee(Integer employee) {
        this.employee = employee;
    }

    public List<PersonIdentifier> getPersonIdentityList() {
        return personIdentityList;
    }

    public void setPersonIdentityList(List<PersonIdentifier> personIdentityList) {
        this.personIdentityList = personIdentityList;
    }

    public List<PersonDocument> getPersonDocumentList() {
        return personDocumentList;
    }

    public void setPersonDocumentList(List<PersonDocument> personDocumentList) {
        this.personDocumentList = personDocumentList;
    }

    public Long getSourceId() {
        return sourceId;
    }

    public void setSourceId(Long sourceId) {
        this.sourceId = sourceId;
    }

    public PersonDocument getIncludeReportDocument() {
        for (PersonDocument document : personDocumentList) {
            if (document.isIncludeReport()) {
                return document;
            }
        }
        return null;
    }

    /**
     * Получить документ ФЛ, данный метод используется при работе с ФЛ из первичных форм, так как там может быть не более одного документа
     *
     * @return докумен ФЛ
     */
    public PersonDocument getPersonDocument() {
        if (personDocumentList != null && !personDocumentList.isEmpty()) {
            return personDocumentList.get(0);
        }
        return null;
    }

    /**
     * Получить идентификатор ФЛ, данный метод используется при работе с ФЛ из первичных форм, так как там может быть не более одного идентификатора
     *
     * @return идентификатор ФЛ
     */
    public PersonIdentifier getPersonIdentifier() {
        if (personIdentityList != null && !personIdentityList.isEmpty()) {
            return personIdentityList.get(0);
        }
        return null;
    }


    @Override
    public Double getWeight() {
        return weight;
    }

    @Override
    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public boolean isNeedUpdate() {
        return needUpdate;
    }

    public void setNeedUpdate(boolean needUpdate) {
        this.needUpdate = needUpdate;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("id(refBook)", id)
                .append("id(primary)", primaryPersonId)
                .append("recordId", recordId)
                .append("lastName", lastName)
                .append("firstName", firstName)
                .append("middleName", middleName)
                .append("inn", inn)
                .append("innForeign", innForeign)
                .append("snils", snils)
                .append("taxPayerStatus", taxPayerStatus)
                .append("citizenship", citizenship)
                .append("birthDate", birthDate)
                .append("personIdentityList", personIdentityList)
                .append("personDocumentList", personDocumentList)
                .append("address", address)
                .append("employee", employee)
                .append("weight", weight)
                .toString();
    }

}
