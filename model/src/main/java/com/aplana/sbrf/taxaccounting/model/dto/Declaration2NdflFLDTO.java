package com.aplana.sbrf.taxaccounting.model.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * Элемент журнала форм 2-НДФЛ (ФЛ)
 */
@Getter
@Setter
public class Declaration2NdflFLDTO {
    /**
     * Идентификатор записи с данными декларации
     */
    private Long declarationDataId;
    /**
     * Вид налоговой форы
     */
    private String declarationType;
    /**
     * Ид ФЛ
     */
    private long personId;
    /**
     * Вид налоговой форы
     */
    private String person;
    /**
     * Название подразделения
     */
    private String department;
    /**
     * Период
     */
    private String reportPeriod;
    /**
     * Статус налоговой формы
     */
    private String state;
    /**
     * КПП
     */
    private String kpp;
    /**
     * ОКТМО
     */
    private String oktmo;
    /**
     * Дата и время создания формы
     */
    private Date creationDate;
    /**
     * Создал
     */
    private String creationUserName;
    /**
     * Примечание
     */
    private String note;
    /**
     * Права
     */
    private long permissions;
}
