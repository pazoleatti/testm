package com.aplana.sbrf.taxaccounting.script;

/**
 * Класс содержит константы используемые в скриптах
 */
public class SharedConstants {
    public final static String DATE_ZERO_AS_STRING = "00.00.0000";
    public final static String DATE_ZERO_AS_DATE = "01.01.1901";
    public final static String DATE_FORMAT = "dd.MM.yyyy";

    //Полное наименование параметров Раздела 1 РНУ - Реквизиты
    public final static String INP_FULL = "Налогоплательщик.ИНП";
    public final static String LAST_NAME_FULL = "Налогоплательщик.Фамилия";
    public final static String FIRST_NAME_FULL = "Налогоплательщик.Имя";
    public final static String MIDDLE_NAME_FULL = "Налогоплательщик.Отчество";
    public final static String BIRTH_DAY_FULL = "Налогоплательщик.Дата рождения";
    public final static String CITIZENSHIP_FULL = "Гражданство (код страны)";
    public final static String INN_FULL = "ИНН.В Российской федерации";
    public final static String INN_FOREIGN_FULL = "ИНН.В стране гражданства";
    public final static String ID_DOC_TYPE_FULL = "Документ удостоверяющий личность.Код";
    public final static String ID_DOC_NUMBER_FULL = "Документ удостоверяющий личность.Номер";
    public final static String STATUS_FULL = "Статус (код)";
    public final static String REGION_CODE_FULL = "Адрес регистрации в Российской Федерации.Код субъекта";
    public final static String POST_INDEX_FULL = "Адрес регистрации в Российской Федерации.Индекс";
    public final static String AREA_FULL = "Адрес регистрации в Российской Федерации.Район";
    public final static String CITY_FULL = "Адрес регистрации в Российской Федерации.Город";
    public final static String LOCALITY_FULL = "Адрес регистрации в Российской Федерации.Населенный пункт";
    public final static String STREET_FULL = "Адрес регистрации в Российской Федерации.Улица";
    public final static String HOUSE_FULL = "Адрес регистрации в Российской Федерации.Дом";
    public final static String BUILDING_FULL = "Адрес регистрации в Российской Федерации.Корпус";
    public final static String FLAT_FULL = "Адрес регистрации в Российской Федерации.Квартира";
    public final static String SNILS_FULL = "Налогоплательщик.СНИЛС";
    public final static String COUNTRY_CODE_FULL = "Код страны проживания вне РФ";
    public final static String ADDRESS_FULL = "Адрес проживания вне РФ";

    //Наименование параметров в справочнике "Физические лица"
    public final static String REF_PERSON_REC_ID = "Идентификатор ФЛ";
    public final static String REF_PERSON_LAST_NAME = "Фамилия";
    public final static String REF_PERSON_FIRST_NAME = "Имя";
    public final static String REF_PERSON_MIDDLE_NAME = "Отчество";
    public final static String REF_PERSON_BIRTH_DAY = "Дата рождения";
    public final static String REF_PERSON_CITIZENSHIP = "Гражданство";
    public final static String REF_PERSON_INN = "ИНН в Российской Федерации";
    public final static String REF_PERSON_INN_FOREIGN = "ИНН в стране гражданства";
    public final static String REF_PERSON_STATUS = "Статус налогоплательщика ";
    public final static String REF_PERSON_SNILS = "СНИЛС";

    //Наименование параметров в справочнике "Документ, удостоверяющий личность"
    public final static String REF_ID_DOC_TYPE = "Код ДУЛ";
    public final static String REF_ID_DOC_NUMBER = "Серия и номер ДУЛ";

    //Наименование параметров в справочниках "Адрес места жительства в Российской Федерации" и "Адрес за пределами Российской Федерации"
    public final static String ADDRESS_REGION_CODE = "Код региона";
    public final static String ADDRESS_POST_INDEX = "Индекс";
    public final static String ADDRESS_AREA = "Район";
    public final static String ADDRESS_CITY = "Город";
    public final static String ADDRESS_LOCALITY = "Населенный пункт (село, поселок)";
    public final static String ADDRESS_STREET = "Улица (проспект, переулок)";
    public final static String ADDRESS_HOUSE = "Номер дома (владения)";
    public final static String ADDRESS_BUILDING = "Номер корпуса (строения)";
    public final static String ADDRESS_FLAT = "Номер квартиры";
    public final static String ADDRESS_COUNTRY_CODE = "Код страны проживания";
    public final static String ADDRESS_ADDRESS = "Адрес";
}
