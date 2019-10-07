package com.aplana.sbrf.taxaccounting.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * DTO для {@link DepartmentReportPeriod}
 */
@Getter
@Setter
public class DepartmentReportPeriodJournalItem implements Serializable, SecuredEntity {

    private Integer id;

    private String code;

    private String name;

    private Integer year;

    private Boolean isActive;

    private Date correctionDate;

    private Integer reportPeriodId;

    private Integer departmentId;

    private Date deadline;

    private long permissions;

    private long dictTaxPeriodId;

    private Date endDate;

    private Integer taxFormTypeId;

    /**
     * Объект для грида в окне просмотра списка периодов для группировки по году
     */
    private DepartmentReportPeriodJournalItem parent;

}
