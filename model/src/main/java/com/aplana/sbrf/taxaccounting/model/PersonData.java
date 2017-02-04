package com.aplana.sbrf.taxaccounting.model;

import com.aplana.sbrf.taxaccounting.model.VersionedObjectStatus;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Date;

/**
 * Класс для передачи данных из ДАО в сервис
 *
 * @author Andrey Drunk
 */
public class PersonData {


    private Long id;
    private Long recordId;
    private String lastName;
    private String firstName;
    private String middleName;
    private Integer sex;
    private String inn;
    private String innForeign;
    private String snils;
    private Date birthDate;
    private String birthPlace;

    /**
     * Ссылка на справочник {@link com.aplana.sbrf.taxaccounting.model.refbook.RefBook.Id#TAXPAYER_STATUS}
     */
    private Long taxPayerStatusId;

    /**
     * Ссылка на справочник {@link com.aplana.sbrf.taxaccounting.model.refbook.RefBook.Id#COUNTRY}
     */
    private Long citizenshipId;

    /**
     * Ссылка на справочник {@link com.aplana.sbrf.taxaccounting.model.refbook.RefBook.Id#PERSON_ADDRESS}
     */
    private Long addressId;

    private Integer pension;
    private Integer medical;
    private Integer social;
    private Integer employee;

    /**
     * Ссылка на справочник {@link com.aplana.sbrf.taxaccounting.model.refbook.RefBook.Id#ASNU}
     * Указывает на систему данными которой заполнена либо обновлена запись о физ. лице.
     */
    private Long sourceId;

    /**
     * Код страны из справочника
     */
    private String citizenship;

    /**
     * Версия записи
     */
    private Date version;

    /**
     * Статус записи
     */
    private VersionedObjectStatus status;

    //----------------------- Дополнительные параметры используются при идентификации -----------------------
    /**
     * Код из справочника
     */
    private String inp;
    private String asnu;
    private Long asnuId;
    /**
     * Код документа
     */
    private String documentType;
    private String documentNumber;
    /**
     * Поле для хранения рассчитанного веса записи при выборке
     */
    private Double weigth;

    //----------------------------------------------

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

    public String getInp() {
        return inp;
    }

    public void setInp(String inp) {
        this.inp = inp;
    }

    public String getAsnu() {
        return asnu;
    }

    public void setAsnu(String asnu) {
        this.asnu = asnu;
    }

    public Long getAsnuId() {
        return asnuId;
    }

    public void setAsnuId(Long asnuId) {
        this.asnuId = asnuId;
    }

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentCode) {
        this.documentType = documentCode;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRecordId() {
        return recordId;
    }

    public void setRecordId(Long recordId) {
        this.recordId = recordId;
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

    public Long getAddressId() {
        return addressId;
    }

    public void setAddressId(Long addressId) {
        this.addressId = addressId;
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

    public Long getSourceId() {
        return sourceId;
    }

    public void setSourceId(Long sourceId) {
        this.sourceId = sourceId;
    }

    public String getCitizenship() {
        return citizenship;
    }

    public void setCitizenship(String citizenship) {
        this.citizenship = citizenship;
    }

    public Date getVersion() {
        return version;
    }

    public void setVersion(Date version) {
        this.version = version;
    }

    public VersionedObjectStatus getStatus() {
        return status;
    }

    public void setStatus(VersionedObjectStatus status) {
        this.status = status;
    }

    public Double getWeigth() {
        return weigth;
    }

    public void setWeigth(Double weigth) {
        this.weigth = weigth;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", id)
                .append("recordId", recordId)
                .append("lastName", lastName)
                .append("firstName", firstName)
                .append("middleName", middleName)
                .append("sex", sex)
                .append("inn", inn)
                .append("innForeign", innForeign)
                .append("snils", snils)
                .append("birthDate", birthDate)
                .append("birthPlace", birthPlace)
                .append("taxPayerStatusId", taxPayerStatusId)
                .append("citizenshipId", citizenshipId)
                .append("addressId", addressId)
                .append("pension", pension)
                .append("medical", medical)
                .append("social", social)
                .append("employee", employee)
                .append("sourceId", sourceId)
                .append("citizenship", citizenship)
                .append("version", version)
                .append("status", status)
                .append("inp", inp)
                .append("asnu", asnu)
                .append("asnuId", asnuId)
                .append("documentType", documentType)
                .append("documentNumber", documentNumber)
                .append("weigth", weigth)
                .toString();
    }
}
