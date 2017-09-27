package com.aplana.sbrf.taxaccounting.model;

/**
 * Модельный класс для скрипта события макета
 */
public class DeclarationTemplateEventScript extends IdentityObject<Long> {

    private Integer declarationTemplateId;

    private Integer eventId;

    private String script;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DeclarationTemplateEventScript that = (DeclarationTemplateEventScript) o;

        if (!declarationTemplateId.equals(that.declarationTemplateId)) return false;
        return eventId.equals(that.eventId);
    }

    @Override
    public int hashCode() {
        int result = declarationTemplateId.hashCode();
        result = 31 * result + eventId.hashCode();
        return result;
    }

    public Integer getDeclarationTemplateId() {
        return declarationTemplateId;
    }

    public void setDeclarationTemplateId(Integer declarationTemplateId) {
        this.declarationTemplateId = declarationTemplateId;
    }

    public Integer getEventId() {
        return eventId;
    }

    public void setEventId(Integer eventId) {
        this.eventId = eventId;
    }

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }
}
