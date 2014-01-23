package com.aplana.sbrf.taxaccounting.model;

/**
 * User: avanteev
 * События версий шаблонов налоговых форм/деклараций
 */
public enum TemplateChangesEvent {

    CREATED(1, "Версия создана"),
    MODIFIED(2, "Версия изменена"),
    ACTIVATED(3, "Версия введена в действие"),
    DEACTIVATED(4, "Версия выведена в действия"),
    DELETED(5, "Версия удалена");

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

    public static TemplateChangesEvent fromId(int eventId) {
        for (TemplateChangesEvent event : values()) {
            if (event.getId() == eventId) {
                return event;
            }
        }
        throw new IllegalArgumentException("Wrong VersionedObjectStatus id: " + eventId);
    }
}
