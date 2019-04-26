package com.aplana.sbrf.taxaccounting.model;

import java.io.Serializable;

/**
 * Модельный класс для работы с отчетами декларации
 */
public class DeclarationDataReportType implements Serializable {
    private static final long serialVersionUID = -1156464397165684964L;

    //Типы отчетов, связанные с асинхронными задачами
    public static final DeclarationDataReportType EXCEL_DEC = new DeclarationDataReportType(AsyncTaskType.EXCEL_DEC, null);
    public static final DeclarationDataReportType XML_DEC = new DeclarationDataReportType(AsyncTaskType.XML_DEC, null);
    public static final DeclarationDataReportType IDENTIFY_PERSON = new DeclarationDataReportType(AsyncTaskType.IDENTIFY_PERSON, null);
    public static final DeclarationDataReportType CONSOLIDATE = new DeclarationDataReportType(AsyncTaskType.CONSOLIDATE, null);
    public static final DeclarationDataReportType PDF_DEC = new DeclarationDataReportType(AsyncTaskType.PDF_DEC, null);
    public static final DeclarationDataReportType ACCEPT_DEC = new DeclarationDataReportType(AsyncTaskType.ACCEPT_DEC, null);
    public static final DeclarationDataReportType CHECK_DEC = new DeclarationDataReportType(AsyncTaskType.CHECK_DEC, null);
    public static final DeclarationDataReportType EXCEL_TEMPLATE_DEC = new DeclarationDataReportType(AsyncTaskType.EXCEL_TEMPLATE_DEC, null);
    public static final DeclarationDataReportType DELETE_DEC = new DeclarationDataReportType(AsyncTaskType.DELETE_DEC, null);
    public static final DeclarationDataReportType UPDATE_PERSONS_DATA = new DeclarationDataReportType(AsyncTaskType.UPDATE_PERSONS_DATA, null);

    //Какая то фигня, которая не связана с асинхронными задачами
    public static final DeclarationDataReportType JASPER_DEC = new DeclarationDataReportType(AsyncTaskType.JASPER_DEC, null);
    //TODO: (dloshkarev) судя по всему это уже не используется, т.к для импорта ставится блокировка с LockData.LockObjects.LOAD_TRANSPORT_DATA
    public static final DeclarationDataReportType IMPORT_TF_DEC = new DeclarationDataReportType(AsyncTaskType.IMPORT_TF_DEC, null);
    public static final DeclarationDataReportType EDIT_FILE_COMMENT_DEC = new DeclarationDataReportType(AsyncTaskType.EDIT_FILE_COMMENT_DEC, null);
    public static final DeclarationDataReportType SPECIFIC_REPORT_DEC = new DeclarationDataReportType(AsyncTaskType.SPECIFIC_REPORT_DEC, null);

    /**
     * Тип задачи
     */
    private AsyncTaskType reportType;
    /**
     * Тип специфичного отчета
     */
    private DeclarationSubreport subreport;

    public DeclarationDataReportType(AsyncTaskType reportType, DeclarationSubreport subreport) {
        if (!AsyncTaskType.EXCEL_DEC.equals(reportType) && subreport != null && AsyncTaskType.EXCEL_DEC.getName().equals(subreport.getAlias()) ||
                !AsyncTaskType.XML_DEC.equals(reportType) && subreport != null && AsyncTaskType.XML_DEC.getName().equals(subreport.getAlias()) ||
                !AsyncTaskType.PDF_DEC.equals(reportType) && subreport != null && AsyncTaskType.PDF_DEC.getName().equals(subreport.getAlias()) ||
                !AsyncTaskType.JASPER_DEC.equals(reportType) && subreport != null && AsyncTaskType.JASPER_DEC.getName().equals(subreport.getAlias()) ||
                !AsyncTaskType.ACCEPT_DEC.equals(reportType) && subreport != null && AsyncTaskType.ACCEPT_DEC.getName().equals(subreport.getAlias()) ||
                !AsyncTaskType.CHECK_DEC.equals(reportType) && subreport != null && AsyncTaskType.CHECK_DEC.getName().equals(subreport.getAlias()) ||
                !AsyncTaskType.EDIT_FILE_COMMENT_DEC.equals(reportType) && subreport != null && AsyncTaskType.EDIT_FILE_COMMENT_DEC.getName().equals(subreport.getAlias()) ||
                !AsyncTaskType.IMPORT_TF_DEC.equals(reportType) && subreport != null && AsyncTaskType.IMPORT_TF_DEC.getName().equals(subreport.getAlias())) {
            throw new IllegalArgumentException("Некорректное тип отчета: " + subreport.getAlias());
        }
        this.reportType = reportType;
        setSubreport(subreport);
    }

    public AsyncTaskType getReportType() {
        return reportType;
    }

    public DeclarationSubreport getSubreport() {
        return subreport;
    }

    public void setSubreport(DeclarationSubreport subreport) {
        if (AsyncTaskType.SPECIFIC_REPORT_DEC.equals(reportType))
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
        return getReportType().equals(AsyncTaskType.SPECIFIC_REPORT_DEC);
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
        } else if (DELETE_DEC.getReportType().getName().equals(name)) {
            return DELETE_DEC;
        } else if (IMPORT_TF_DEC.getReportType().getName().equals(name)) {
            return IMPORT_TF_DEC;
        } else if (EDIT_FILE_COMMENT_DEC.getReportType().getName().equals(name)) {
            return EDIT_FILE_COMMENT_DEC;
        } else if (IDENTIFY_PERSON.getReportType().getName().equals(name)) {
            return IDENTIFY_PERSON;
        } else if (CONSOLIDATE.getReportType().getName().equals(name)) {
            return CONSOLIDATE;
        } else {
            return new DeclarationDataReportType(AsyncTaskType.SPECIFIC_REPORT_DEC, null);
        }
    }

    public static DeclarationDataReportType getDDReportTypeByReportType(AsyncTaskType reportType) {
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
        } else if (DELETE_DEC.getReportType().equals(reportType)) {
            return DELETE_DEC;
        } else if (IMPORT_TF_DEC.getReportType().equals(reportType)) {
            return IMPORT_TF_DEC;
        } else if (EDIT_FILE_COMMENT_DEC.getReportType().equals(reportType)) {
            return EDIT_FILE_COMMENT_DEC;
        } else if (EXCEL_TEMPLATE_DEC.getReportType().equals(reportType)) {
            return EXCEL_TEMPLATE_DEC;
        } else if (IDENTIFY_PERSON.getReportType().equals(reportType)) {
            return IDENTIFY_PERSON;
        } else if (CONSOLIDATE.getReportType().equals(reportType)) {
            return CONSOLIDATE;
        } else if (UPDATE_PERSONS_DATA.getReportType().equals(reportType)) {
            return UPDATE_PERSONS_DATA;
        } else {
            throw new IllegalArgumentException("Некорректный тип отчета: " + reportType.getName());
        }
    }

    @Override
    public String toString() {
        return "DeclarationDataReportType{" +
                "reportType=" + reportType +
                ", subreport=" + subreport +
                '}';
    }
}
