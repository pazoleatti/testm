package com.aplana.sbrf.taxaccounting.model;

/**
 * Шаблоны для описания объектов и событий системы
 * @author dloshkarev
 */
public enum DescriptionTemplate {
    DECLARATION("%s: Период: \"%s%s\", Подразделение: \"%s\", Вид: \"%s\"%s%s%s%s"),
    REF_BOOK_EDIT("Редактирование справочника \"%s\""),
    FILE("Загрузка ТФ \"%s\""),
    DECLARATION_TEMPLATE("Редактирование версии макета налоговой формы \"%s\" (%s) за период с %s по %s"),
    CONFIGURATION_PARAMS("Блокировка конфигурационных параметров при загрузке ТФ"),
    LOAD_TRANSPORT_DATA("Импорт ТФ из каталога загрузки"),
    IMPORT_TRANSPORT_DATA("Загрузка файла \"%s\"");

    private String text;

    DescriptionTemplate(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
