package com.aplana.sbrf.taxaccounting.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * DTO-Класс, содержащий информацию о параметрах декларации и связанных с ним объектов в "плоском" виде
 * Используется для таблицы "Список налоговых форм"
 */
@Getter
@Setter
public class DeclarationDataJournalItem implements Serializable {
    private static final long serialVersionUID = -5255606476850599681L;

    // Идентификатор записи с данными декларации
    private Long declarationDataId;

    /**
     * Тип налоговой форы
     */
    private String declarationKind;

    /**
     * Вид налоговой форы
     */
    private String declarationType;
    /**
     * Тип КНФ
     */
    private String knfTypeName;
    /**
     * Название подразделения
     */
    private String department;

    /**
     * АСНУ
     */
    private String asnuName;
    /**
     * Период
     */
    private String reportPeriod;
    /**
     * Статус налоговой формы
     */
    private String state;
    /**
     * Файл
     */
    private String fileName;
    /**
     * Дата и время создания формы
     */
    private Date creationDate;

    /**
     * Создал
     */
    private String creationUserName;

    /**
     * КПП
     */
    private String kpp;
    /**
     * ОКТМО
     */
    private String oktmo;
    /**
     * Налоговый орган
     */
    private String taxOrganCode;
    /**
     * Статус документа
     */
    private String docState;
    /**
     * Примечание
     */
    private String note;
    /**
     * Номер корректировки
     */
    private Integer correctionNum;
    /**
     * Права
     */
    private long permissions;
}
