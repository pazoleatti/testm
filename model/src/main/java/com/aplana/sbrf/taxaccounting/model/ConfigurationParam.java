package com.aplana.sbrf.taxaccounting.model;

/**
 * Перечисление типов параметров приложения
 *
 * @author <a href="mailto:Marat.Fayzullin@aplana.com">Файзуллин Марат</a>
 * @since 10.10.13 11:24
 */

public enum ConfigurationParam {

    FORM_DATA_KEY_FILE ("Путь к файлу ключей ЭЦП для форм"),
    REF_BOOK_KEY_FILE ("Путь к файлу ключей ЭЦП для справочников"),
    ACCOUNT_PLAN_TRANSPORT_DIRECTORY ("Путь к каталогу загрузки транспортных файлов, содержащих данные справочника «План счетов»"),
    OKATO_TRANSPORT_DIRECTORY ("Путь к каталогу загрузки транспортных файлов, содержащих данные справочника «ОКАТО»"),
    REGION_TRANSPORT_DIRECTORY ("Путь к каталогу загрузки транспортных файлов, содержащих данные справочника «Субъекты РФ»"),
    UPLOAD_DIRECTORY("Путь к каталогу загрузки", false),
    ARCHIVE_DIRECTORY("Путь к каталогу архива", false),
    ERROR_DIRECTORY ("Путь к каталогу ошибок", false);

    private String caption;
    private boolean common;

    private ConfigurationParam(String caption) {
        this(caption, true);
    }

    private ConfigurationParam(String caption, boolean common) {
        this.caption = caption;
        this.common = common;
    }

    public String getCaption() {
        return caption;
    }

    public boolean isCommon() {
        return common;
    }
}
