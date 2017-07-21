package com.aplana.sbrf.taxaccounting.model.filter;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

/**
 * Модель для параметров Фильтра вкладки Реквизиты страницу РНУ НДФЛ
 */
@Getter
@Setter
@ToString
public class NdflPersonFilter {

    /**
     * id формы
     */
    private long declarationDataId;

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
    private  Date dateFrom;
    /**
     * Дата рождения по
     */
    private  Date dateTo;

}
