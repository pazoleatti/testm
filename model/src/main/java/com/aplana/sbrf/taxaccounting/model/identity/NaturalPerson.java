package com.aplana.sbrf.taxaccounting.model.identity;

import com.aplana.sbrf.taxaccounting.model.IdentityObject;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.*;

/**
 * @author Andrey Drunk
 */
public class NaturalPerson extends IdentityObject<Long> implements IdentityPerson {

    /**
     * Уникальный идентификатор записи в справочнике ФЛ
     */
    private Long refBookPersonId;

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
     * Статус физлица, сравнивается по идентификатору записи в справочнике, так как ссылается на не версионный справочник
     * Ссылка на справочник {@link com.aplana.sbrf.taxaccounting.model.refbook.RefBook.Id#TAXPAYER_STATUS}
     */
    private Long taxPayerStatusId;

    /**
     * Гражданство, сравнивается по идентификатору записи в справочнике, так как ссылается на не версионный справочник
     * Ссылка на справочник {@link com.aplana.sbrf.taxaccounting.model.refbook.RefBook.Id#COUNTRY}
     */
    private Long citizenshipId;

    /**
     * Дата рождения
     */
    private Date birthDate;

    /**
     * Список идентификаторов ФЛ
     */
    private Map<Long, PersonIdentifier> personIdentityMap;

    /**
     * Список документов ФЛ
     */
    private Map<Long, PersonDocument> personDocumentMap;

    /**
     * Адрес фл
     */
    private Address address;

    private Integer pension;
    private Integer medical;
    private Integer social;
    private Integer employee;

    /**
     * Идентификатор записи (buisiness key) в справочнике физлиц, заполняется при получении записи из БД
     */
    private Long recordId;

    /**
     * Идентификатор объекта, на основании которого создана запись
     */
    private Long sourceId;

    /**
     * Поле для хранения веса записи при идентификации
     */
    private Double weigth;

    public NaturalPerson() {
        this.personIdentityMap = new HashMap<Long, PersonIdentifier>();
        this.personDocumentMap = new HashMap<Long, PersonDocument>();
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

    public Long getTaxPayerStatusId() {
        return taxPayerStatusId;
    }

    public void setTaxPayerStatusId(Long taxPayerStatusId) {
        this.taxPayerStatusId = taxPayerStatusId;
    }

    public Long getCitizenshipId() {
        return citizenshipId;
    }

    public void setCitizenshipId(Long citizenshipId) {
        this.citizenshipId = citizenshipId;
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


    public Map<Long, PersonIdentifier> getPersonIdentityMap() {
        return personIdentityMap;
    }

    public void setPersonIdentityMap(Map<Long, PersonIdentifier> personIdentityMap) {
        this.personIdentityMap = personIdentityMap;
    }

    public Map<Long, PersonDocument> getPersonDocumentMap() {
        return personDocumentMap;
    }

    public PersonDocument getIncludeReportDocument() {
        for (PersonDocument document: personDocumentMap.values()){
            if (document.isIncludeReport()){
                return document;
            }
        }
        return null;
    }

    public void setPersonDocumentMap(Map<Long, PersonDocument> personDocumentMap) {
        this.personDocumentMap = personDocumentMap;
    }

    public Collection<PersonIdentifier> getPersonIdentifiers() {
        return personIdentityMap.values();
    }

    public Collection<PersonDocument> getPersonDocuments() {
        return personDocumentMap.values();
    }

    @Override
    public Double getWeigth() {
        return weigth;
    }

    @Override
    public void setWeigth(Double weigth) {
        this.weigth = weigth;
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
                .append("taxPayerStatusId", taxPayerStatusId)
                .append("citizenshipId", citizenshipId)
                .append("birthDate", birthDate)
                .append("personIdentityList", personIdentityMap)
                .append("personDocumentList", personDocumentMap)
                .append("address", address)
                .append("pension", pension)
                .append("medical", medical)
                .append("social", social)
                .append("employee", employee)
                .toString();
    }
}
