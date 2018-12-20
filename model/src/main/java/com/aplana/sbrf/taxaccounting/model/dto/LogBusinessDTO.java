package com.aplana.sbrf.taxaccounting.model.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * DTO истории изменений
 */
@Setter
@Getter
public class LogBusinessDTO {
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
    private String eventName;
    /**
     * Имя пользователя, инициировавшего событие
     */
    private String userName;
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
}