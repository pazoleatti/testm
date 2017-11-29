package com.aplana.sbrf.taxaccounting.model;

import java.io.Serializable;

/**
 * Данные о фатальности проверок для формы
 *
 * @author dloshkarev
 */
public class DeclarationTemplateCheck implements Serializable {

    private Integer id;

    /**
     * Идентификатор типа формы, к которому привязана проверка по-умолчанию
     */
    private int typeId;

    /**
     * Идентификатор макета, к которому привязана проверка
     */
    private int templateId;

    /**
     * Код проверки
     */
    private FormCheckCode code;

    /**
     * Тип проверки
     */
    private String checkType;

    /**
     * Описание
     */
    private String description;

    /**
     * Проверка фатальна?
     */
    private boolean isFatal;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public FormCheckCode getCode() {
        return code;
    }

    public void setCode(FormCheckCode code) {
        this.code = code;
    }

    public String getCheckType() {
        return checkType;
    }

    public void setCheckType(String checkType) {
        this.checkType = checkType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isFatal() {
        return isFatal;
    }

    public void setFatal(boolean fatal) {
        isFatal = fatal;
    }

    public int getTemplateId() {
        return templateId;
    }

    public void setTemplateId(int templateId) {
        this.templateId = templateId;
    }

    public int getTypeId() {
        return typeId;
    }

    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DeclarationTemplateCheck that = (DeclarationTemplateCheck) o;

        return id != null ? id.equals(that.id) : that.id == null;

    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
