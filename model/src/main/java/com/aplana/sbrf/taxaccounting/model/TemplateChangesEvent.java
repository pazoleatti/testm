package com.aplana.sbrf.taxaccounting.model;

/**
 * User: avanteev
 * События версий шаблонов налоговых форм/деклараций
 */
public enum TemplateChangesEvent {

    CREATED(1, "Версия создана"),
    MODIFIED(2, "Версия создана"),
    ACTIVATED(3, "Версия создана"),
    DEACTIVATED(4, "Версия создана"),
    DELETED(5, "Версия создана");

    private Integer id;
    private String name;

    private TemplateChangesEvent(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
