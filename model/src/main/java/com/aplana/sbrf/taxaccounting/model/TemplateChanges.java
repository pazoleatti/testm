package com.aplana.sbrf.taxaccounting.model;

import java.util.Date;

/**
 * User: avanteev
 */
public class TemplateChanges {
    private int id;

    /**
     * Идентификатор НФ
     */
    private int formTemplateId;

    /**
     * Идентификатор декларации
     */
    private int declarationTemplateId;
    private TemplateChangesEvent event;

    /**
     * Идентификатор пользователя
     */
    private TAUser author;

    /**
     * Дата события
     */
    private Date eventDate;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getFormTemplateId() {
        return formTemplateId;
    }

    public void setFormTemplateId(int formTemplateId) {
        this.formTemplateId = formTemplateId;
    }

    public int getDeclarationTemplateId() {
        return declarationTemplateId;
    }

    public void setDeclarationTemplateId(int declarationTemplateId) {
        this.declarationTemplateId = declarationTemplateId;
    }

    public TemplateChangesEvent getEvent() {
        return event;
    }

    public void setEvent(TemplateChangesEvent event) {
        this.event = event;
    }

    public TAUser getAuthor() {
        return author;
    }

    public void setAuthor(TAUser author) {
        this.author = author;
    }

    public Date getEventDate() {
        return eventDate;
    }

    public void setEventDate(Date eventDate) {
        this.eventDate = eventDate;
    }
}
