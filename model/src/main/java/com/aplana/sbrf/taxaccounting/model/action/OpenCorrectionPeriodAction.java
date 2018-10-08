package com.aplana.sbrf.taxaccounting.model.action;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

/**
 * Данные по открытию корректирующего периода
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OpenCorrectionPeriodAction {
    // основной период
    private int departmentReportPeriodId;
    // период сдачи корректировки
    private Date correctionDate;
}
