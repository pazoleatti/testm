package com.aplana.sbrf.taxaccounting.model;

import java.io.Serializable;
import java.util.Date;

/**
 * User: avanteev
 */
public class TemplateChanges implements Serializable {
    private int id;

    /**
     * Идентификатор НФ
     */
    private Integer formTemplateId;

    /**
     * Идентификатор декларации
     */
    private Integer declarationTemplateId;

    /**
     * Идентификатор справочника
     */
    private Integer refBookId;
    private FormDataEvent event;

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

    public Integer getFormTemplateId() {
        return formTemplateId;
    }

    public void setFormTemplateId(Integer formTemplateId) {
        this.formTemplateId = formTemplateId;
    }

    public Integer getDeclarationTemplateId() {
        return declarationTemplateId;
    }

    public void setDeclarationTemplateId(Integer declarationTemplateId) {
        this.declarationTemplateId = declarationTemplateId;
    }

    public Integer getRefBookId() {
        return refBookId;
    }

    public void setRefBookId(Integer refBookId) {
        this.refBookId = refBookId;
    }

    public FormDataEvent getEvent() {
        return event;
    }

    public void setEvent(FormDataEvent event) {
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
