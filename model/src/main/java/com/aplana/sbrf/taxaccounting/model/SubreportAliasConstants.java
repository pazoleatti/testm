package com.aplana.sbrf.taxaccounting.model;

/**
 * Класс хранящий константы псевдонимов спецотчета
 */
public class SubreportAliasConstants {

    /** Константы псевдонимов для спецотчетов */
    public static final String RNU_NDFL_PERSON_DB = "rnu_ndfl_person_db"; // РНУ НДФЛ по физическому лицу
    public static final String RNU_NDFL_PERSON_ALL_DB = "rnu_ndfl_person_all_db"; // РНУ НДФЛ по всем ФЛ
    public static final String RNU_RATE_REPORT = "rnu_rate_report"; // Отчет Карманниковой: Отчет в разрезе ставок
    public static final String RNU_PAYMENT_REPORT = "rnu_payment_report"; // Отчет Карманниковой: Отчет в разрезе платёжных поручений
    public static final String RNU_NDFL_2_6_DATA_XLSX_REPORT = "rnu_ndfl_2_6_data_xlsx_report"; // Отчет xlsx Данные для включения в 2-НДФЛ и 6-НДФЛ
    public static final String RNU_NDFL_2_6_DATA_TXT_REPORT = "rnu_ndfl_2_6_data_txt_report"; // Отчет txt Данные для включения в 2-НДФЛ и 6-НДФЛ
    public static final String RNU_NDFL_DETAIL_REPORT = "rnu_ndfl_detail_report"; // Спецотчет "Детализация – доходы, вычеты, налоги"
    public static final String REPORT_KPP_OKTMO = "report_kpp_oktmo"; // Реестр сформированной отчетности
    public static final String REPORT_2NDFL1 = "report_2ndfl1"; // 2-НДФЛ (1) по физическому лицу
    public static final String REPORT_2NDFL2 = "report_2ndfl2"; // 2-НДФЛ (2) по физическому лицу
    public static final String DEPT_NOTICE_DEC = "dept_notice_dec";

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
