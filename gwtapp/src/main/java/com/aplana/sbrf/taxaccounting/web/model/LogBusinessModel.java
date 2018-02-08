package com.aplana.sbrf.taxaccounting.web.model;

import com.aplana.sbrf.taxaccounting.model.LogBusiness;

import java.util.Date;

/**
 * Модель данных для истории изменений декларации
 */
public class LogBusinessModel {

    /**
     * Дата
     */
    private Date logDate;

    /**
     * Роль пользователя
     */
    private String roles;

    /**
     * Идентификатор декларации
     */
    private Long declarationDataId;

    /**
     * Подразделение пользователя
     */
    private String departmentName;

    /**
     * Текст сообщения
     */
    private String note;

    /**
     * Наименование события
     */
    private String eventName;

    /**
     * Полное имя пользователя
     */
    private String userFullName;

    public LogBusinessModel(LogBusiness logBusiness, String eventName, String userFullName) {
        this.logDate = logBusiness.getLogDate();
        this.roles = logBusiness.getRoles();
        this.declarationDataId = logBusiness.getDeclarationId();
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
        return declarationDataId;
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