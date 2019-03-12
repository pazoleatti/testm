package com.aplana.sbrf.taxaccounting.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Перечисление типов параметров приложения
 */
public enum ConfigurationParam {
    /**
     * Путь к файлу/каталогу ключей ЭП
     */
    KEY_FILE("Путь к файлу/каталогу ключей ЭП (БОК)", ConfigurationParamGroup.COMMON, false, null, 1),

    /**
     * Проверять ЭП
     */
    SIGN_CHECK("Проверять ЭП (1 - проверять, 0 - не проверять)", ConfigurationParamGroup.COMMON, true, false, 0),
    ACCOUNT_PLAN_UPLOAD_DIRECTORY("Путь к каталогу загрузки транспортных файлов, содержащих данные справочника «План счетов»", ConfigurationParamGroup.COMMON, true, true, 1),
    OKATO_UPLOAD_DIRECTORY("Путь к каталогу загрузки транспортных файлов, содержащих данные справочника «ОКАТО»", ConfigurationParamGroup.COMMON, true, true, 1),
    REGION_UPLOAD_DIRECTORY("Путь к каталогу загрузки транспортных файлов, содержащих данные справочника «Субъекты РФ»", ConfigurationParamGroup.COMMON, true, true, 1),
    DIASOFT_UPLOAD_DIRECTORY("Путь к каталогу загрузки справочников Diasoft", ConfigurationParamGroup.COMMON, true, true, 2),
    FIAS_UPLOAD_DIRECTORY("Путь к каталогу загрузки транспортных файлов, содержащих данные справочника ФИАС", ConfigurationParamGroup.COMMON, true, true, 1),
    REF_BOOK_ARCHIVE_DIRECTORY("Путь к каталогу архива справочников", ConfigurationParamGroup.COMMON, true, true, 2),
    REF_BOOK_ERROR_DIRECTORY("Путь к каталогу ошибок справочников", ConfigurationParamGroup.COMMON, true, true, 2),

    /**
     * Путь к библиотеке подписи
     */
    ENCRYPT_DLL("Путь к библиотеке подписи", ConfigurationParamGroup.COMMON, true, true, 1),

    /**
     * Путь к руководству
     */
    MANUAL_PATH("Путь к руководству пользователя и настройщика", ConfigurationParamGroup.COMMON, true, true, 1),

    /**
     * Загрузка НФ
     */
    FORM_UPLOAD_DIRECTORY("Путь к каталогу загрузки", ConfigurationParamGroup.FORM, true, true, 2),
    FORM_ARCHIVE_DIRECTORY("Путь к каталогу архива", ConfigurationParamGroup.FORM, true, true, 2),
    FORM_ERROR_DIRECTORY("Путь к каталогу ошибок", ConfigurationParamGroup.FORM, true, true, 2),

    /**
     * Общие параметры
     */
    SBERBANK_INN("ИНН ПАО Сбербанк", ConfigurationParamGroup.COMMON_PARAM, true, false, 2),
    NO_CODE("Код НО (пром.)", ConfigurationParamGroup.COMMON_PARAM, true, false, 2),
    SHOW_TIMING("Отображать сообщения о времени выполнения", ConfigurationParamGroup.COMMON_PARAM, true, false, 2),
    LIMIT_IDENT("Порог схожести ФЛ", ConfigurationParamGroup.COMMON_PARAM, true, false, 2),
    ENABLE_IMPORT_PERSON("Загружать справочник ФЛ", ConfigurationParamGroup.COMMON_PARAM, true, false, 2),
    CONSOLIDATION_DATA_SELECTION_DEPTH("Горизонт отбора данных консолидации", ConfigurationParamGroup.COMMON_PARAM, true, null, 0),
    REPORT_PERIOD_YEAR_MIN("Минимальное значение отчетного года", ConfigurationParamGroup.COMMON_PARAM, true, null, 0),
    REPORT_PERIOD_YEAR_MAX("Максимальное значение отчетного года", ConfigurationParamGroup.COMMON_PARAM, true, null, 0),
    WEIGHT_LAST_NAME("Вес для идентификации поля Фамилия", ConfigurationParamGroup.COMMON_PARAM, true, null, 0),
    WEIGHT_FIRST_NAME("Вес для идентификации поля Имя", ConfigurationParamGroup.COMMON_PARAM, true, null, 0),
    WEIGHT_MIDDLE_NAME("Вес для идентификации поля Отчество", ConfigurationParamGroup.COMMON_PARAM, true, null, 0),
    WEIGHT_BIRTHDAY("Вес для идентификации поля Дата рождения", ConfigurationParamGroup.COMMON_PARAM, true, null, 0),
    WEIGHT_CITIZENSHIP("Вес для идентификации поля Гражданство", ConfigurationParamGroup.COMMON_PARAM, true, null, 0),
    WEIGHT_INP("Вес для идентификации поля ИНП", ConfigurationParamGroup.COMMON_PARAM, true, null, 0),
    WEIGHT_INN("Вес для идентификации поля ИНН в РФ", ConfigurationParamGroup.COMMON_PARAM, true, null, 0),
    WEIGHT_INN_FOREIGN("Вес для идентификации поля ИНН в стране гражданства", ConfigurationParamGroup.COMMON_PARAM, true, null, 0),
    WEIGHT_SNILS("Вес для идентификации поля СНИЛС", ConfigurationParamGroup.COMMON_PARAM, true, null, 0),
    WEIGHT_TAX_PAYER_STATUS("Вес для идентификации поля Статус налогоплательщика", ConfigurationParamGroup.COMMON_PARAM, true, null, 0),
    WEIGHT_DUL("Вес для идентификации поля ДУЛ", ConfigurationParamGroup.COMMON_PARAM, true, null, 0),
    WEIGHT_ADDRESS("Вес для идентификации поля Адрес в РФ", ConfigurationParamGroup.COMMON_PARAM, true, null, 0),
    WEIGHT_ADDRESS_INO("Вес для идентификации поля Адрес в стране регистрации", ConfigurationParamGroup.COMMON_PARAM, true, null, 0),
    DECLARATION_ROWS_BULK_EDIT_MAX_COUNT("Максимальное количество строк РНУ для массового изменения", ConfigurationParamGroup.COMMON_PARAM, true, null, 0),
    ASYNC_SERIAL_MODE("Включить возможность последовательного выполнения асинхронных задач", ConfigurationParamGroup.COMMON_PARAM, true, null, 0);

    private String caption;
    private ConfigurationParamGroup group;
    private boolean unique;
    private Boolean folder;
    private int checkAccess;

    /**
     * Параметр
     *
     * @param caption     Имя параметра
     * @param group       Группа параметра
     * @param unique      Признак уникальности
     * @param folder      Признак директории: false - файл, true - каталог, null - нет ограничения
     * @param checkAccess Признак проверки доступа на: 0 — нет проверки, 1 — чтение, 2 — запись.
     */
    ConfigurationParam(String caption, ConfigurationParamGroup group, boolean unique, Boolean folder, int checkAccess) {
        this.caption = caption;
        this.group = group;
        this.unique = unique;
        this.folder = folder;
        this.checkAccess = checkAccess;
    }

    /**
     * Находит имя параметра хранящееся в бд по имени, которое видит пользователь
     */
    public static String getNameValueAsDB(String nameParamsForUser) {
        for (ConfigurationParam configParams : ConfigurationParam.values()) {
            if (nameParamsForUser.equals(configParams.getCaption())) {
                return configParams.name();
            }
        }
        if (nameParamsForUser == null) {
            nameParamsForUser = "";
        }
        return nameParamsForUser;
    }

    /**
     * Получение списка кофигурационных параметров по их принадлежносте к группе из {@link ConfigurationParamGroup}
     *
     * @param group группа параметров {@link ConfigurationParamGroup}
     * @return список {@link ConfigurationParam} или пустой список
     */
    public static List<ConfigurationParam> getParamsByGroup(ConfigurationParamGroup group) {
        List<ConfigurationParam> params = new ArrayList<>();
        for (ConfigurationParam configurationParam : ConfigurationParam.values()) {
            if (configurationParam.group.equals(group)) {
                params.add(configurationParam);
            }
        }
        return params;
    }

    /**
     * Имя параметра
     */
    public String getCaption() {
        return caption;
    }

    /**
     * Группа параметра
     */
    public ConfigurationParamGroup getGroup() {
        return group;
    }

    /**
     * Признак уникальности
     */
    public boolean isUnique() {
        return unique;
    }

    /**
     * Признак обязательности проверки пути на доступ на запись
     */
    public boolean hasWriteCheck() {
        return checkAccess == 2;
    }

    /**
     * Признак обязательности проверки пути на доступ на чтение
     */
    public boolean hasReadCheck() {
        return checkAccess == 1;
    }

    /**
     * Признак того, что значеним параметра является путь к какому-либо каталогу
     */
    public Boolean isFolder() {
        return folder;
    }

    public static ConfigurationParam getValueByCode(String code) {
        for (ConfigurationParam configurationParam : ConfigurationParam.values()) {
            if (configurationParam.name().equals(code)) {
                return configurationParam;
            }
        }
        return null;
    }
}
