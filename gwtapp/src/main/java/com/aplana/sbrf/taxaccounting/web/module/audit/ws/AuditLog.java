package com.aplana.sbrf.taxaccounting.web.module.audit.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AddAuditLog", propOrder = {
        "login",
        "name",
        "note"
})
public class AuditLog {
    @XmlElement(required = true)
    protected String login;
    @XmlElement(required = true)
    protected String name;
    @XmlElement(required = true)
    protected String note;

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
