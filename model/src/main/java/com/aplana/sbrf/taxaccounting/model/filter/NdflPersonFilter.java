package com.aplana.sbrf.taxaccounting.model.filter;

import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAsnu;

import java.util.Date;
import java.util.List;

/**
 * Модель для параметров Фильтра вкладки Реквизиты страницу РНУ НДФЛ
 */
public class NdflPersonFilter {

    /**
     * ИНП
     */
    private String inp;
    /**
     * Фамилия
     */
    private  String lastName;
    /**
     * Имя
     */
    private  String firstName;
    /**
     * Отчество
     */
    private  String middleName;
    /**
     * Дата рождения с
     */
    private Date dateFrom;
    /**
     * Дата рождения по
     */
    private  Date dateTo;
    /**
     * Код ДУЛ
     */
    private  String idDocType;
    /**
     * № ДУЛ
     */
    private  String idDocNumber;
    /**
     * Гражданство (код страны)
     */
    private String citizenship;
    /**
     * Статус (Код)
     */
    private String status;

    /**
     * Код региона
     */
    private String regionCode;
    /**
     * Индекс
     */
    private String postIndex;
    /**
     * Район
     */
    private String area;
    /**
     * Город
     */
    private String city;
    /**
     * Населенный пункт
     */
    private String locality;
    /**
     * Улица
     */
    private String street;
    /**
     * Дом
     */
    private String house;
    /**
     * Корпус
     */
    private String building;
    /**
     * Квартира
     */
    private String flat;

    /**
     * СНИЛС
     */
    private  String snils;
    /**
     * ИНН РФ
     */
    private  String innNp;
    /**
     * ИНН Ино
     */
    private  String innForeign;

    /**
     * Номер строки
     */
    private String rowNum;
    /**
     * Идентификатор строки
     */
    private String id;
    /**
     * Дата редактирования с
     */
    private Date modifiedDateFrom;
    /**
     * Дата редактирования по
     */
    private Date modifiedDateTo;
    /**
     * Обновил
     */
    private String modifiedBy;

    /**
     * АСНУ
     */
    private List<RefBookAsnu> asnu;


    public String getInp() {
        return inp;
    }

    public void setInp(String inp) {
        this.inp = inp;
    }

    public String getInnNp() {
        return innNp;
    }

    public void setInnNp(String innNp) {
        this.innNp = innNp;
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

    public String getIdDocNumber() {
        return idDocNumber;
    }

    public void setIdDocNumber(String idDocNumber) {
        this.idDocNumber = idDocNumber;
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

    public Date getDateFrom() {
        return dateFrom;
    }

    public void setDateFrom(Date dateFrom) {
        this.dateFrom = dateFrom;
    }

    public Date getDateTo() {
        return dateTo;
    }

    public void setDateTo(Date dateTo) {
        this.dateTo = dateTo;
    }

    public String getIdDocType() {
        return idDocType;
    }

    public void setIdDocType(String idDocType) {
        this.idDocType = idDocType;
    }

    public String getCitizenship() {
        return citizenship;
    }

    public void setCitizenship(String citizenship) {
        this.citizenship = citizenship;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRegionCode() {
        return regionCode;
    }

    public void setRegionCode(String regionCode) {
        this.regionCode = regionCode;
    }

    public String getPostIndex() {
        return postIndex;
    }

    public void setPostIndex(String postIndex) {
        this.postIndex = postIndex;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
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

    public String getBuilding() {
        return building;
    }

    public void setBuilding(String building) {
        this.building = building;
    }

    public String getFlat() {
        return flat;
    }

    public void setFlat(String flat) {
        this.flat = flat;
    }

    public String getRowNum() {
        return rowNum;
    }

    public void setRowNum(String rowNum) {
        this.rowNum = rowNum;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getModifiedDateFrom() {
        return modifiedDateFrom;
    }

    public void setModifiedDateFrom(Date modifiedDateFrom) {
        this.modifiedDateFrom = modifiedDateFrom;
    }

    public Date getModifiedDateTo() {
        return modifiedDateTo;
    }

    public void setModifiedDateTo(Date modifiedDateTo) {
        this.modifiedDateTo = modifiedDateTo;
    }

    public String getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(String modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    public List<RefBookAsnu> getAsnu() {
        return asnu;
    }

    public void setAsnu(List<RefBookAsnu> asnu) {
        this.asnu = asnu;
    }

    @Override
    public String toString() {
        return "NdflPersonFilter{" +
                "inp='" + inp + '\'' +
                ", lastName='" + lastName + '\'' +
                ", firstName='" + firstName + '\'' +
                ", middleName='" + middleName + '\'' +
                ", dateFrom=" + dateFrom +
                ", dateTo=" + dateTo +
                ", idDocType='" + idDocType + '\'' +
                ", idDocNumber='" + idDocNumber + '\'' +
                ", citizenship='" + citizenship + '\'' +
                ", status='" + status + '\'' +
                ", regionCode='" + regionCode + '\'' +
                ", postIndex='" + postIndex + '\'' +
                ", area='" + area + '\'' +
                ", city='" + city + '\'' +
                ", locality='" + locality + '\'' +
                ", street='" + street + '\'' +
                ", house='" + house + '\'' +
                ", building='" + building + '\'' +
                ", flat='" + flat + '\'' +
                ", snils='" + snils + '\'' +
                ", innNp='" + innNp + '\'' +
                ", innForeign='" + innForeign + '\'' +
                ", rowNum='" + rowNum + '\'' +
                ", id='" + id + '\'' +
                ", modifiedDateFrom=" + modifiedDateFrom +
                ", modifiedDateTo=" + modifiedDateTo +
                ", modifiedBy='" + modifiedBy + '\'' +
                ", asnu='" + asnu + "\'" +
                '}';
    }
}
