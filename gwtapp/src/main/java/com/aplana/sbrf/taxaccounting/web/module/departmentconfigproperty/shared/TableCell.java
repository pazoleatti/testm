package com.aplana.sbrf.taxaccounting.web.module.departmentconfigproperty.shared;

import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;

import java.io.Serializable;
import java.util.Date;

public class TableCell implements Serializable {
    String stringValue;
    Number numberValue;
    Date dateValue;
    Long refValue;
    String deRefValue;

    RefBookAttributeType type;

    public String getStringValue() {
        return stringValue;
    }

    public void setStringValue(String stringValue) {
        this.stringValue = stringValue;
    }

    public Number getNumberValue() {
        return numberValue;
    }

    public void setNumberValue(Number numberValue) {
        this.numberValue = numberValue;
    }

    public Date getDateValue() {
        return dateValue;
    }

    public void setDateValue(Date dateValue) {
        this.dateValue = dateValue;
    }

    public Long getRefValue() {
        return refValue;
    }

    public void setRefValue(Long refValue) {
        this.refValue = refValue;
    }

    public RefBookAttributeType getType() {
        return type;
    }

    public void setType(RefBookAttributeType type) {
        this.type = type;
    }

    public String getDeRefValue() {
        return deRefValue;
    }

    public void setDeRefValue(String deRefValue) {
        this.deRefValue = deRefValue;
    }
}
