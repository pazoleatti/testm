package com.aplana.sbrf.taxaccounting.web.module.audit.ws;

import com.aplana.sbrf.taxaccounting.model.TAUserInfo;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AuditLog", propOrder = {
        "userInfo",
        "note"
})
public class AuditLog {

    @XmlElement(required = true)
    protected TAUserInfo userInfo;
    @XmlElement(required = true)
    protected String note;

    public TAUserInfo getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(TAUserInfo userInfo) {
        this.userInfo = userInfo;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String value) {
        this.note = value;
    }

}

