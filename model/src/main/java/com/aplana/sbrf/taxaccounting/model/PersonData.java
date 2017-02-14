package com.aplana.sbrf.taxaccounting.model;

import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPerson;
import com.aplana.sbrf.taxaccounting.model.util.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Date;

/**
 * Класс для передачи данных из ДАО в сервис
 *
 * @author Andrey Drunk
 */
public class PersonData {


    /**
     * Уникальный идентификатор записи в справочнике физлиц, заполняется при получении записи из БД
     */
    private Long id;

    /**
     * Идентификатор записи (buisiness key) в справочнике физлиц, заполняется при получении записи из БД
     */
    private Long recordId;

    //Набор полей из справочника физлица
    private String lastName;
    private String firstName;
    private String middleName;
    private Integer sex;
    private String inn;
    private String innForeign;
    private String snils;
    private Date birthDate;

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
     * Идентификаторы налогоплательщика. Уникальный неизменяемый цифровой идентификатор налогоплательщика
     */
    private String inp;

    /**
     *
     * Идентификаторы налогоплательщика. Ссылка на справочник {@link com.aplana.sbrf.taxaccounting.model.refbook.RefBook.Id#ASNU}
     */
    private Long asnuId;

    /**
     * Ссылка на справочник виды документов
     */
    private Long documentTypeId;

    /**
     * Номер документа
     */
    private String documentNumber;

    /**
     * Поле для хранения рассчитанного веса записи при выборке
     */
    private Double weigth;

    /**
     * Адрес физлица
     */
    private Integer addressType;

    private String regionCode;
    private String postalCode;
    private String district; //Район
    private String city;
    private String locality;
    private String street;
    private String house;
    private String build;
    private String appartment;

    /**
     * Адрес вне РФ
     */
    private Long countryId;
    private String addressIno;

    private Integer pension;
    private Integer medical;
    private Integer social;
    private Integer employee;

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

    public Date getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
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

    public String getInp() {
        return inp;
    }

    public void setInp(String inp) {
        this.inp = inp;
    }

    public Long getAsnuId() {
        return asnuId;
    }

    public void setAsnuId(Long asnuId) {
        this.asnuId = asnuId;
    }

    public Long getDocumentTypeId() {
        return documentTypeId;
    }

    public void setDocumentTypeId(Long documentTypeId) {
        this.documentTypeId = documentTypeId;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public Double getWeigth() {
        return weigth;
    }

    public void setWeigth(Double weigth) {
        this.weigth = weigth;
    }

    public Integer getAddressType() {
        return addressType;
    }

    public void setAddressType(Integer addressType) {
        this.addressType = addressType;
    }

    public Long getCountryId() {
        return countryId;
    }

    public void setCountryId(Long countryId) {
        this.countryId = countryId;
    }

    public String getRegionCode() {
        return regionCode;
    }

    public void setRegionCode(String regionCode) {
        this.regionCode = regionCode;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getLocality() {
        return locality;
    }

    public void setLocality(String locality) {
        this.locality = locality;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getHouse() {
        return house;
    }

    public void setHouse(String house) {
        this.house = house;
    }

    public String getBuild() {
        return build;
    }

    public void setBuild(String build) {
        this.build = build;
    }

    public String getAppartment() {
        return appartment;
    }

    public void setAppartment(String appartment) {
        this.appartment = appartment;
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

    public String getAddressIno() {
        return addressIno;
    }

    public void setAddressIno(String addressIno) {
        this.addressIno = addressIno;
    }
}
