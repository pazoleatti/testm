package com.aplana.sbrf.taxaccounting.model.identification;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Andrey Drunk
 */
public class NaturalPerson implements IdentityPerson {

    /**
     * Идентификатор ФЛ
     */
    private Long personId;

    /**
     * Уникальный идентификатор записи в справочнике ФЛ
     */
    private Long refBookPersonId;

    /**
     * Идентификатор записи (buisiness key) в справочнике физлиц, заполняется при получении записи из БД
     */
    private Long recordId;

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
     * Пол
     */
    private Integer sex;

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

    private Integer pension;
    private Integer medical;
    private Integer social;
    private Integer employee;

    /**
     * Система источник
     */
    private Long sourceId;


    /**
     * Поле для хранения веса записи при идентификации
     */
    private Double weigth;

    public NaturalPerson() {
        this.personIdentityList = new ArrayList<PersonIdentifier>();
        this.personDocumentList = new ArrayList<PersonDocument>();
    }

    public Long getPersonId() {
        return personId;
    }

    public void setPersonId(Long personId) {
        this.personId = personId;
    }

    public Long getRefBookPersonId() {
        return refBookPersonId;
    }

    public void setRefBookPersonId(Long refBookPersonId) {
        this.refBookPersonId = refBookPersonId;
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

    public Integer getSex() {
        return sex;
    }

    public void setSex(Integer sex) {
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

    public Integer getPension() {
        return pension;
    }

    public void setPension(Integer pension) {
        this.pension = pension;
    }

    public Integer getMedical() {
        return medical;
    }

    public void setMedical(Integer medical) {
        this.medical = medical;
    }

    public Integer getSocial() {
        return social;
    }

    public void setSocial(Integer social) {
        this.social = social;
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

    public Long getRecordId() {
        return recordId;
    }

    public void setRecordId(Long recordId) {
        this.recordId = recordId;
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

    public PersonDocument getPersonDocument() {
        if (personDocumentList != null && !personDocumentList.isEmpty()) {
            personDocumentList.get(0);
        }
        return null;
    }

    public PersonIdentifier getPersonIdentifier() {
        if (personIdentityList != null && !personIdentityList.isEmpty()) {
            personIdentityList.get(0);
        }
        return null;
    }


    @Override
    public Double getWeigth() {
        return weigth;
    }

    @Override
    public void setWeigth(Double weigth) {
        this.weigth = weigth;
    }


    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("refBookPersonId", refBookPersonId)
                .append("lastName", lastName)
                .append("firstName", firstName)
                .append("middleName", middleName)
                .append("sex", sex)
                .append("inn", inn)
                .append("innForeign", innForeign)
                .append("snils", snils)
                .append("taxPayerStatus", taxPayerStatus)
                .append("citizenship", citizenship)
                .append("birthDate", birthDate)
                .append("personIdentityList", personIdentityList)
                .append("personDocumentList", personDocumentList)
                .append("address", address)
                .append("pension", pension)
                .append("medical", medical)
                .append("social", social)
                .append("employee", employee)
                .toString();
    }
}
