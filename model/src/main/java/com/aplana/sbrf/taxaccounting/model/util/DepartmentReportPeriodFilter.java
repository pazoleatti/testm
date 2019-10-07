package com.aplana.sbrf.taxaccounting.model.util;

import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Фильтр очетных периодов подразделений, null-значения соответствуют отсутствию фильтрации
 */
@Getter
@Setter
public class DepartmentReportPeriodFilter implements Serializable {
    private Integer id;
    private Boolean isActive;
    private Boolean isCorrection;
    private Date correctionDate;
    private List<Integer> departmentIdList;
    private List<Integer> reportPeriodIdList;
    private List<TaxType> taxTypeList;
    private Integer yearStart;
    private Integer yearEnd;
    private Integer departmentId;
    private ReportPeriod reportPeriod;
    private Date deadline;
    private Long dictTaxPeriodId;
    private boolean withChild;

    public Boolean isActive() {
        return isActive;
    }

    public Boolean isCorrection() {
        return isCorrection;
    }

    /**
     *
     * @param isCorrection false - если требуется найти обычные(не корректирующие) периоды
     */
    public void setIsCorrection(Boolean isCorrection) {
        this.isCorrection = isCorrection;
    }

    public boolean isWithChild() {
        return withChild;
    }

}
