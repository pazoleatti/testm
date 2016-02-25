package com.aplana.sbrf.taxaccounting.model;

import java.io.Serializable;

/**
 * Модельный класс для работы с отчетами НФ
 * @author lhaziev
 *
 */
public class FormDataReportType implements Serializable {
    private static final long serialVersionUID = -1641316873791354845L;

    public static final FormDataReportType EXCEL = new FormDataReportType(ReportType.EXCEL, null);
    public static final FormDataReportType CSV = new FormDataReportType(ReportType.CSV, null);

    /**
     * Тип задачи
     */
    private ReportType reportType;
    /**
     * Название специфичного отчета
     */
    private String name;

    public FormDataReportType() {
    }

    public FormDataReportType(ReportType reportType, String name) {
        this.reportType = reportType;
        this.name = name;
    }

    public ReportType getReportType() {
        return reportType;
    }

    public String getName() {
        return name;
    }

    public String getReportName() {
        if (reportType.equals(ReportType.SPECIFIC_REPORT)) {
            return name;
        } else {
            return reportType.getName();
        }
    }

    public static FormDataReportType getFDReportTypeByName(String name) {
        if (EXCEL.getReportName().equals(name)) {
            return EXCEL;
        } else if (CSV.getReportName().equals(name)) {
            return CSV;
        } else {
            return new FormDataReportType(ReportType.SPECIFIC_REPORT, name);
        }
    }
}
