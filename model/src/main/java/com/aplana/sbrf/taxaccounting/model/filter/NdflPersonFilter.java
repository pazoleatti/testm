package com.aplana.sbrf.taxaccounting.model.filter;

import java.util.Date;

/**
 * Модель для параметров Фильтра вкладки Реквизиты страницу РНУ НДФЛ
 */
public class NdflPersonFilter {

    /**
     * ИНП
     */
    private String inp;
    /**
     * ИНН РФ
     */
    private  String innNp;
    /**
     * ИНН Ино
     */
    private  String innForeign;
    /**
     * СНИЛС
     */
    private  String snils;
    /**
     * № ДУЛ
     */
    private  String idDocNumber;
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
}
