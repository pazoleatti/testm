package com.aplana.sbrf.taxaccounting.model;

/**
 * Класс хранящий константы псевдонимов спецотчета
 */
public class SubreportAliasConstants {

    /** Константы псевдонимов для спецотчетов */
    public static final String RNU_NDFL_PERSON_DB = "rnu_ndfl_person_db"; // РНУ НДФЛ по физическому лицу
    public static final String RNU_NDFL_PERSON_ALL_DB = "rnu_ndfl_person_all_db"; // РНУ НДФЛ по всем ФЛ
    public static final String RNU_KARMANNIKOVA_RATE_REPORT = "rnu_karmannikova_rate_report"; // Отчет Карманниковой: Отчет в разрезе ставок
    public static final String RNU_KARMANNIKOVA_PAYMENT_REPORT = "rnu_karmannikova_payment_report"; // Отчет Карманниковой: Отчет в разрезе платёжных поручений
    public static final String REPORT_KPP_OKTMO = "report_kpp_oktmo"; // Реестр сформированной отчетности
    public static final String REPORT_2NDFL = "report_2ndfl"; // 2-НДФЛ (1) по физическому лицу

    /** Константы псевдонимов для параметров спецотчетов */
    public static final String ID_DOC_NUMBER = "idDocNumber"; // № ДУЛ
    public static final String TO_BIRTHDAY = "toBirthDay"; // Дата рождения по
    public static final String FROM_BIRTHDAY = "fromBirthDay"; // Дата рождения с
    public static final String FIRST_NAME = "firstName"; // Имя
    public static final String INN = "inn"; // ИНН
    public static final String INP = "inp"; // ИНП
    public static final String MIDDLE_NAME = "middleName"; // Отчество
    public static final String SNILS = "snils"; // СНИЛС
    public static final String LAST_NAME = "lastName"; // lastName
    public static final String P_NUM_SPRAVKA = "pNumSpravka"; // Номер справки

    private SubreportAliasConstants() {
    }
}
