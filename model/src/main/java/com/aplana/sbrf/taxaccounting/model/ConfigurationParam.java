package com.aplana.sbrf.taxaccounting.model;

/**
 * Перечисление типов параметров приложения
 *
 * @author <a href="mailto:Marat.Fayzullin@aplana.com">Файзуллин Марат</a>
 * @since 10.10.13 11:24
 */
public enum ConfigurationParam {
    // Общие
    KEY_FILE("Путь к файлу ключей ЭЦП", true, false, false, 1),
    ACCOUNT_PLAN_UPLOAD_DIRECTORY("Путь к каталогу загрузки транспортных файлов, содержащих данные справочника «План счетов»", true, true, true, 1),
    OKATO_UPLOAD_DIRECTORY("Путь к каталогу загрузки транспортных файлов, содержащих данные справочника «ОКАТО»", true, true, true, 1),
    REGION_UPLOAD_DIRECTORY("Путь к каталогу загрузки транспортных файлов, содержащих данные справочника «Субъекты РФ»", true, true, true, 1),
    DIASOFT_UPLOAD_DIRECTORY("Путь к каталогу справочников Diasoft", true, true, true, 2),
    REF_BOOK_ARCHIVE_DIRECTORY("Путь к каталогу архива справочников", true, true, true, 2),
    REF_BOOK_ERROR_DIRECTORY("Путь к каталогу ошибок справочников", true, true, true, 2),
    // Загрузка НФ
    FORM_UPLOAD_DIRECTORY("Путь к каталогу загрузки", false, true, true, 2),
    FORM_ARCHIVE_DIRECTORY("Путь к каталогу архива", false, true, true, 2),
    FORM_ERROR_DIRECTORY("Путь к каталогу ошибок", false, true, true, 2);

    private String caption;
    private boolean common;
    private boolean unique;
    private boolean folder;
    private int checkAccess;

    /**
     * Параметр
     *
     * @param caption     Имя параметра
     * @param common      Признак общих параметров. true — общие, false — загрузка НФ.
     * @param unique      Признак уникальности
     * @param folder      Признак директории
     * @param checkAccess Признак проверки доступа на: 0 — нет проверки, 1 — чтение, 2 — запись.
     */
    private ConfigurationParam(String caption, boolean common, boolean unique, boolean folder, int checkAccess) {
        this.caption = caption;
        this.common = common;
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
     * Признак общих параметров. true — общие, false — загрузка НФ
     */
    public boolean isCommon() {
        return common;
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
