package com.aplana.sbrf.taxaccounting.model.result;

import lombok.Getter;
import lombok.Setter;

import java.util.Comparator;
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
    /** Дата окончания отчетного периода */
    private Date endDate;

    public static class CompDate implements Comparator<LogPeriodResult> {
        private int mod = 1;
        public CompDate(boolean desc) {
            if (desc) mod =-1;
        }
        @Override
        public int compare(LogPeriodResult arg0, LogPeriodResult arg1) {
            return mod*arg0.getEndDate().compareTo(arg1.getEndDate());
        }
    }

}
