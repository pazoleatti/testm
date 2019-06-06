package com.aplana.sbrf.taxaccounting.model.refbook;

import com.aplana.sbrf.taxaccounting.model.ReportPeriodType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

/**
 * Дополнительные интервалы для загрузки данных
 */
@Getter
@Setter
@NoArgsConstructor
public class ReportPeriodImport extends RefBookSimple<Long> {
    // Код периода
    private ReportPeriodType reportPeriodType;
    // Дата начала интервала
    private Date startDate;
    // Дата окончания интервала
    private Date endDate;
    // АСНУ
    private RefBookAsnu asnu;
}
