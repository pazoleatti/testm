package com.aplana.sbrf.taxaccounting.model.filter;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

/**
 * Модель для параметров Фильтра вкладки Реквизиты страницу РНУ НДФЛ
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
public class NdflPersonFilter {
    /**
     * Общий фильтр
     */
    @ToString.Exclude
    private NdflFilter ndflFilter;
    /**
     * ИНП
     */
    private String inp;
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
     * Дата рождения с
     */
    private Date dateFrom;
    /**
     * Дата рождения по
     */
    private Date dateTo;
    /**
     * Код ДУЛ
     */
    private String idDocType;
    /**
     * № ДУЛ
     */
    private String idDocNumber;
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
    private String snils;
    /**
     * ИНН РФ
     */
    private String innNp;
    /**
     * ИНН Ино
     */
    private String innForeign;

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

    public NdflPersonFilter(NdflFilter ndflFilter) {
        this.ndflFilter = ndflFilter;
    }
}
