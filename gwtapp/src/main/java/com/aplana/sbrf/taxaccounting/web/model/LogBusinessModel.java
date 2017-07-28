package com.aplana.sbrf.taxaccounting.web.model;

import com.aplana.sbrf.taxaccounting.model.LogBusiness;

import java.util.Date;

/**
 * Модель данных для списка измений по налоговой форме в нормальном виде
 */
public class LogBusinessModel {
    //дата правки формы
    private Date logDate;

    //роль пользователя
    private String roles;

    //id декларации
    private Long declarationId;

    //наименование подразделения
    private String departmentName;

    //примечание
    private String note;

    //название события правки
    private String eventName;

    //полное имя пользователя
    private String userFullName;

    public LogBusinessModel(LogBusiness logBusiness, String eventName, String userFullName) {
        this.logDate = logBusiness.getLogDate();
        this.roles = logBusiness.getRoles();
        this.declarationId = logBusiness.getDeclarationId();
        this.departmentName = logBusiness.getDepartmentName();
        this.note = logBusiness.getNote();
        this.eventName = eventName;
        this.userFullName = userFullName;
    }

    public Date getLogDate() {
        return logDate;
    }

    public String getRoles() {
        return roles;
    }

    public Long getDeclarationId() {
        return declarationId;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public String getNote() {
        return note;
    }

    public String getEventName() {
        return eventName;
    }

    public String getUserFullName() {
        return userFullName;
    }
}