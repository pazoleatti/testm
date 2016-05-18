package com.aplana.sbrf.taxaccounting.model;

import java.io.Serializable;

/**
 * Модельный класс для работы с отчетами декларации
 * @author lhaziev
 *
 */
public class DeclarationDataReportType implements Serializable {
    private static final long serialVersionUID = -1156464397165684964L;

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
     * Тип специфичного отчета
     */
    private DeclarationSubreport subreport;

    public DeclarationDataReportType(ReportType reportType, DeclarationSubreport subreport) {
        if (!ReportType.EXCEL_DEC.equals(reportType) && subreport != null && ReportType.EXCEL_DEC.getName().equals(subreport.getAlias()) ||
                !ReportType.XML_DEC.equals(reportType) && subreport != null && ReportType.XML_DEC.getName().equals(subreport.getAlias()) ||
                !ReportType.PDF_DEC.equals(reportType) && subreport != null && ReportType.PDF_DEC.getName().equals(subreport.getAlias()) ||
                !ReportType.JASPER_DEC.equals(reportType) && subreport != null && ReportType.JASPER_DEC.getName().equals(subreport.getAlias()) ||
                !ReportType.ACCEPT_DEC.equals(reportType) && subreport != null && ReportType.ACCEPT_DEC.getName().equals(subreport.getAlias()) ||
                !ReportType.CHECK_DEC.equals(reportType) && subreport != null && ReportType.CHECK_DEC.getName().equals(subreport.getAlias())) {
            throw new IllegalArgumentException("Некорректное тип отчета: " + subreport.getAlias());
        }
        this.reportType = reportType;
        setSubreport(subreport);
    }

    public ReportType getReportType() {
        return reportType;
    }

    public DeclarationSubreport getSubreport() {
        return subreport;
    }

    public void setSubreport(DeclarationSubreport subreport) {
        if (ReportType.SPECIFIC_REPORT_DEC.equals(reportType))
            this.subreport = subreport;
        else if (subreport != null)
            throw new IllegalArgumentException("Некорректный тип отчета: " + subreport.getAlias());
    }

    public String getReportAlias() {
        if (isSubreport()) {
            return subreport.getAlias();
        } else {
            return reportType.getName();
        }
    }

    public boolean isSubreport() {
        return getReportType().equals(ReportType.SPECIFIC_REPORT_DEC);
    }

    public static DeclarationDataReportType getDDReportTypeByName(String name) {
        if (EXCEL_DEC.getReportType().getName().equals(name)) {
            return EXCEL_DEC;
        } else if (XML_DEC.getReportType().getName().equals(name)) {
            return XML_DEC;
        } else if (PDF_DEC.getReportType().getName().equals(name)) {
            return PDF_DEC;
        } else if (JASPER_DEC.getReportType().getName().equals(name)) {
            return JASPER_DEC;
        } else if (ACCEPT_DEC.getReportType().getName().equals(name)) {
            return ACCEPT_DEC;
        } else if (CHECK_DEC.getReportType().getName().equals(name)) {
            return CHECK_DEC;
        } else {
            return new DeclarationDataReportType(ReportType.SPECIFIC_REPORT_DEC, null);
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
