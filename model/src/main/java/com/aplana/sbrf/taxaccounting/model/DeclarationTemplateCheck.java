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
    private int declarationTypeId;

    /**
     * Идентификатор макета, к которому привязана проверка
     */
    private int declarationTemplateId;

    /**
     * Код проверки
     */
    private DeclarationCheckCode code;

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

    public DeclarationCheckCode getCode() {
        return code;
    }

    public void setCode(DeclarationCheckCode code) {
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

    public int getDeclarationTemplateId() {
        return declarationTemplateId;
    }

    public void setDeclarationTemplateId(int declarationTemplateId) {
        this.declarationTemplateId = declarationTemplateId;
    }

    public int getDeclarationTypeId() {
        return declarationTypeId;
    }

    public void setDeclarationTypeId(int declarationTypeId) {
        this.declarationTypeId = declarationTypeId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DeclarationTemplateCheck that = (DeclarationTemplateCheck) o;

        if (isFatal != that.isFatal) return false;
        return id != null ? id.equals(that.id) : that.id == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (isFatal ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "DeclarationTemplateCheck{" +
                "id=" + id +
                ", declarationTypeId=" + declarationTypeId +
                ", declarationTemplateId=" + declarationTemplateId +
                ", code=" + code +
                ", checkType='" + checkType + '\'' +
                ", description='" + description + '\'' +
                ", isFatal=" + isFatal +
                '}';
    }
}
