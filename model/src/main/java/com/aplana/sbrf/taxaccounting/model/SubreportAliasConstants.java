package com.aplana.sbrf.taxaccounting.model;

/**
 * Класс хранящий константы псевдонимов спецотчета
 */
public class SubreportAliasConstants {

    // Константы псевдонимов для спецотчетов
    public static String RNU_NDFL_PERSON_DB = "rnu_ndfl_person_db"; // РНУ НДФЛ по физическому лицу
    public static String RNU_NDFL_PERSON_ALL_DB = "rnu_ndfl_person_all_db"; // РНУ НДФЛ по всем ФЛ
    public static String REPORT_KPP_OKTMO = "report_kpp_oktmo"; // Реестр сформированной отчетности
    public static String REPORT_2NDFL = "report_2ndfl"; // 2-НДФЛ (1) по физическому лицу

    // Константы псевдонимов для параметров спецотчетов
    public static String ID_DOC_NUMBER = "idDocNumber"; // № ДУЛ
    public static String TO_BIRTHDAY = "toBirthDay"; // Дата рождения по
    public static String FROM_BIRTHDAY = "fromBirthDay"; // Дата рождения с
    public static String FIRST_NAME = "firstName"; // Имя
    public static String INN = "inn"; // ИНН
    public static String INP = "inp"; // ИНП
    public static String MIDDLE_NAME = "middleName"; // Отчество
    public static String SNILS = "snils"; // СНИЛС
    public static String LAST_NAME = "lastName"; // lastName
    public static String P_NUM_SPRAVKA = "pNumSpravka"; // Номер справки

    private SubreportAliasConstants() {
    }
}
