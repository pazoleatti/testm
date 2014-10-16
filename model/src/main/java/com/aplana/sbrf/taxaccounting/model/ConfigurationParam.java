package com.aplana.sbrf.taxaccounting.model;

/**
 * Перечисление типов параметров приложения
 *
 * @author <a href="mailto:Marat.Fayzullin@aplana.com">Файзуллин Марат</a>
 * @since 10.10.13 11:24
 */
public enum ConfigurationParam {
    // Общие
    KEY_FILE("Путь к файлу ключей ЭЦП", ConfigurationParamGroup.COMMON, false, false, 1),
    SIGN_CHECK("Проверять ЭЦП", ConfigurationParamGroup.COMMON, true, false, 0),
    ACCOUNT_PLAN_UPLOAD_DIRECTORY("Путь к каталогу загрузки транспортных файлов, содержащих данные справочника «План счетов»", ConfigurationParamGroup.COMMON, true, true, 1),
    OKATO_UPLOAD_DIRECTORY("Путь к каталогу загрузки транспортных файлов, содержащих данные справочника «ОКАТО»", ConfigurationParamGroup.COMMON, true, true, 1),
    REGION_UPLOAD_DIRECTORY("Путь к каталогу загрузки транспортных файлов, содержащих данные справочника «Субъекты РФ»", ConfigurationParamGroup.COMMON, true, true, 1),
    DIASOFT_UPLOAD_DIRECTORY("Путь к каталогу справочников Diasoft", ConfigurationParamGroup.COMMON, true, true, 2),
    REF_BOOK_ARCHIVE_DIRECTORY("Путь к каталогу архива справочников", ConfigurationParamGroup.COMMON, true, true, 2),
    REF_BOOK_ERROR_DIRECTORY("Путь к каталогу ошибок справочников", ConfigurationParamGroup.COMMON, true, true, 2),
    ENCRYPT_DLL("Путь к библиотеке подписи", ConfigurationParamGroup.COMMON, true, true, 1),
    // Загрузка НФ
    FORM_UPLOAD_DIRECTORY("Путь к каталогу загрузки", ConfigurationParamGroup.FORM, true, true, 2),
    FORM_ARCHIVE_DIRECTORY("Путь к каталогу архива", ConfigurationParamGroup.FORM, true, true, 2),
    FORM_ERROR_DIRECTORY("Путь к каталогу ошибок", ConfigurationParamGroup.FORM, true, true, 2),
    // Электронная почта
    EMAIL_LOGIN("Имя пользователя", ConfigurationParamGroup.EMAIL, true, false, 0),
    EMAIL_PASSWORD("Пароль", ConfigurationParamGroup.EMAIL, true, false, 0),
    EMAIL_SERVER("Адрес сервера исходящей почты", ConfigurationParamGroup.EMAIL, true, false, 0),
    EMAIL_PORT("Порт сервера исходящей почты", ConfigurationParamGroup.EMAIL, true, false, 0);

    private String caption;
    private ConfigurationParamGroup group;
    private boolean unique;
    private boolean folder;
    private int checkAccess;

    /**
     * Параметр
     *
     * @param caption     Имя параметра
     * @param group        Группа параметра
     * @param unique      Признак уникальности
     * @param folder      Признак директории
     * @param checkAccess Признак проверки доступа на: 0 — нет проверки, 1 — чтение, 2 — запись.
     */
    private ConfigurationParam(String caption, ConfigurationParamGroup group, boolean unique, boolean folder, int checkAccess) {
        this.caption = caption;
        this.group = group;
        this.unique = unique;
        this.folder = folder;
        this.checkAccess = checkAccess;
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
    public boolean isFolder() {
        return folder;
    }
}
