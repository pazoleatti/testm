package com.aplana.sbrf.taxaccounting.model;

import java.io.Serializable;

/**
 * Модельный класс для работы с отчетами декларации
 * @author lhaziev
 *
 */
public class DeclarationDataReportType implements Serializable {
    private static final long serialVersionUID = -1981651313165464964L;

    public static final DeclarationDataReportType EXCEL_DEC = new DeclarationDataReportType(ReportType.EXCEL_DEC, null);
    public static final DeclarationDataReportType XML_DEC = new DeclarationDataReportType(ReportType.XML_DEC, null);
    public static final DeclarationDataReportType PDF_DEC = new DeclarationDataReportType(ReportType.PDF_DEC, null);
    public static final DeclarationDataReportType JASPER_DEC = new DeclarationDataReportType(ReportType.JASPER_DEC, null);
    public static final DeclarationDataReportType ACCEPT_DEC = new DeclarationDataReportType(ReportType.ACCEPT_DEC, null);
    public static final DeclarationDataReportType CHECK_DEC = new DeclarationDataReportType(ReportType.CHECK_DEC, null);

    /**
     * Тип задачи
     */
    private ReportType reportType;
    /**
     * Название специфичного отчета
     */
    private String name;

    public DeclarationDataReportType(ReportType reportType, String name) {
        if (!ReportType.EXCEL_DEC.equals(reportType) && ReportType.EXCEL_DEC.getName().equals(name) ||
                !ReportType.XML_DEC.equals(reportType) && ReportType.XML_DEC.getName().equals(name) ||
                !ReportType.PDF_DEC.equals(reportType) && ReportType.PDF_DEC.getName().equals(name) ||
                !ReportType.JASPER_DEC.equals(reportType) && ReportType.JASPER_DEC.getName().equals(name) ||
                !ReportType.ACCEPT_DEC.equals(reportType) && ReportType.ACCEPT_DEC.getName().equals(name) ||
                !ReportType.CHECK_DEC.equals(reportType) && ReportType.CHECK_DEC.getName().equals(name)) {
            throw new IllegalArgumentException("Некорректное название отчета: " + name);
        }
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
        if (reportType.equals(ReportType.SPECIFIC_REPORT_DEC)) {
            return name;
        } else {
            return reportType.getName();
        }
    }

    public static DeclarationDataReportType getDDReportTypeByName(String name) {
        if (EXCEL_DEC.getReportName().equals(name)) {
            return EXCEL_DEC;
        } else if (XML_DEC.getReportName().equals(name)) {
            return XML_DEC;
        } else if (PDF_DEC.getReportName().equals(name)) {
            return PDF_DEC;
        } else if (JASPER_DEC.getReportName().equals(name)) {
            return JASPER_DEC;
        } else if (ACCEPT_DEC.getReportName().equals(name)) {
            return ACCEPT_DEC;
        } else if (CHECK_DEC.getReportName().equals(name)) {
            return CHECK_DEC;
        } else {
            return new DeclarationDataReportType(ReportType.SPECIFIC_REPORT_DEC, name);
        }
    }

    public static DeclarationDataReportType getDDReportTypeByReportType(ReportType reportType) {
        if (EXCEL_DEC.getReportType().equals(reportType)) {
            return EXCEL_DEC;
        } else if (XML_DEC.getReportType().equals(reportType)) {
            return XML_DEC;
        } else if (PDF_DEC.getReportType().equals(reportType)) {
            return PDF_DEC;
        } else if (JASPER_DEC.getReportType().equals(reportType)) {
            return JASPER_DEC;
        } else if (ACCEPT_DEC.getReportType().equals(reportType)) {
            return ACCEPT_DEC;
        } else if (CHECK_DEC.getReportType().equals(reportType)) {
            return CHECK_DEC;
        } else {
            throw new IllegalArgumentException("Некорректный тип отчета: " + reportType.getName());
        }
    }
}
