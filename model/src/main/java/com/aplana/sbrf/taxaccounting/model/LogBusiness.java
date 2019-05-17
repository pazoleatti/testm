package com.aplana.sbrf.taxaccounting.model;


import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * Модельный класс для хранения истории изменений
 */
@Data
public class LogBusiness {
    /**
     * Идентификатор
     */
    private Long id;
    /**
     * Дата создания записи истории
     */
    private Date logDate;
    /**
     * Событие
     */
    private FormDataEvent event;
    /**
     * Логин пользователя, инициировавшего событие
     */
    private TAUser user;
    /**
     * Идентификатор формы, к которой относится запись истории событий
     */
    private Long declarationDataId;
    /**
     * Идентификатор ФЛ, к которой относится запись истории событий
     */
    private Long personId;
    /**
     * Описание события
     */
    private String note;
    /**
     * Идентификатор протокола операций
     */
    private String logId;

    public String getUserLogin() {
        return user.getId() == TAUser.SYSTEM_USER_ID ? user.getName() : user.getLogin();
    }

    public String getRoles() {
        StringBuilder roles = new StringBuilder();
        if (user != null) {
            List<TARole> taRoles = user.getRoles();
            for (int i = 0; i < taRoles.size(); i++) {
                roles.append(taRoles.get(i).getName());
                if (i != taRoles.size() - 1) {
                    roles.append(", ");
                }
            }
        }
        return roles.toString();
    }

    // Fluent setters

    public LogBusiness logDate(Date logDate) {
        this.logDate = logDate;
        return this;
    }

    public LogBusiness event(FormDataEvent event) {
        this.event = event;
        return this;
    }

    public LogBusiness user(TAUser user) {
        this.user = user;
        return this;
    }

    public LogBusiness declarationDataId(Long declarationDataId) {
        this.declarationDataId = declarationDataId;
        return this;
    }

    public LogBusiness personId(Long personId) {
        this.personId = personId;
        return this;
    }

    public LogBusiness note(String note) {
        this.note = note;
        return this;
    }

    public LogBusiness logId(String logId) {
        this.logId = logId;
        return this;
    }
}
