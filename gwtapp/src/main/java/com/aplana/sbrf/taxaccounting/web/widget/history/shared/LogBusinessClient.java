package com.aplana.sbrf.taxaccounting.web.widget.history.shared;

import com.aplana.sbrf.taxaccounting.model.LogBusiness;

import java.io.Serializable;
import java.util.Date;

/**
 * User: avanteev
 */
public class LogBusinessClient implements Serializable {

    private Date logDate;
    private int eventId;
    private String userName;
    private String roles;
    private Long declarationId;
    private String departmentName;
    private String note;

    public LogBusinessClient() {
    }

    public LogBusinessClient(LogBusiness logBusiness) {
        eventId = logBusiness.getEventId();
        roles = logBusiness.getRoles();
        declarationId = logBusiness.getDeclarationId();
        note = logBusiness.getNote();
        logDate = logBusiness.getLogDate();
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public Date getLogDate() {
        return logDate;
    }

    public int getEventId() {
        return eventId;
    }

    public String getRoles() {
        return roles;
    }

    public Long getDeclarationId() {
        return declarationId;
    }

    public String getNote() {
        return note;
    }
}
