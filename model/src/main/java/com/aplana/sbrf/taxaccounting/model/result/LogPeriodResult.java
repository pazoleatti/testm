package com.aplana.sbrf.taxaccounting.model.result;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * Содержит сформированное поле периода для лога
 */
@Getter
@Setter
public class LogPeriodResult {
    /** Уникальный идентификатор ПНФ */
    private Integer id;
    /** Дата периода корректировки */
    private Date correctionDate;
    /** Наименование периода */
    private String name;
    /** Год периода */
    private Integer year;
}
