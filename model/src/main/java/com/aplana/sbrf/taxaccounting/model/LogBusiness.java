package com.aplana.sbrf.taxaccounting.model;


import lombok.Data;

import java.util.Date;

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
    private int eventId;
    /**
     * Логин пользователя, инициировавшего событие
     */
    private String userLogin;
    /**
     * Роли пользователя
     */
    private String roles;
    /**
     * Подразделение пользователя
     */
    private String userDepartmentName;
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
}
