package com.aplana.sbrf.taxaccounting.model.refbook;

import java.io.Serializable;
import java.util.Date;

/**
 * Модель описывающая место использования ссылки на справочник. Нужна для корректного формирования сообщений
 * @author dloshkarev
 */
public class RefBookLinkModel implements Serializable {
    private static final long serialVersionUID = 5844855927168599415L;

    /** Номер строки. Может быть null для нетабличных настроек и справочников*/
    private Integer index;
    /** Алиас справочного аттрибута */
    private String attributeAlias;
    /** Значение ссылки на справочник */
    private Long referenceValue;
    /** Специальный идентификатор записи, который позволяет выделить ее в группе других */
    private String specialId;
    /** Дата начала периода для проверки актуальности ссылки на справочник */
    private Date versionFrom;
    /** Дата окончания периода для проверки актуальности ссылки на справочник */
    private Date versionTo;

    public RefBookLinkModel() {
    }

    public RefBookLinkModel(Integer index, String attributeAlias, Long referenceValue, String specialId, Date versionFrom, Date versionTo) {
        this.index = index;
        this.attributeAlias = attributeAlias;
        this.referenceValue = referenceValue;
        this.specialId = specialId;
        this.versionFrom = versionFrom;
        this.versionTo = versionTo;
    }

    public Date getVersionFrom() {
        return versionFrom;
    }

    public void setVersionFrom(Date versionFrom) {
        this.versionFrom = versionFrom;
    }

    public Date getVersionTo() {
        return versionTo;
    }

    public void setVersionTo(Date versionTo) {
        this.versionTo = versionTo;
    }

    public String getSpecialId() {
        return specialId;
    }

    public void setSpecialId(String specialId) {
        this.specialId = specialId;
    }

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public String getAttributeAlias() {
        return attributeAlias;
    }

    public void setAttributeAlias(String attributeAlias) {
        this.attributeAlias = attributeAlias;
    }

    public Long getReferenceValue() {
        return referenceValue;
    }

    public void setReferenceValue(Long referenceValue) {
        this.referenceValue = referenceValue;
    }

    @Override
    public String toString() {
        return "RefBookLinkModel{" +
                "index=" + index +
                ", attributeAlias='" + attributeAlias + '\'' +
                ", referenceValue=" + referenceValue +
                '}';
    }
}
