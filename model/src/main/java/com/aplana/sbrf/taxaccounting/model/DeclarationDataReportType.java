package com.aplana.sbrf.taxaccounting.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * Типы отчетов форм
 */
@Getter
@Setter
@ToString
public class DeclarationDataReportType implements Serializable {
    private static final long serialVersionUID = -1156464397165684964L;

    public static final DeclarationDataReportType EXCEL_DEC = new DeclarationDataReportType(0, "EXCEL_DEC");
    public static final DeclarationDataReportType XML_DEC = new DeclarationDataReportType(1, "XML_DEC");
    public static final DeclarationDataReportType PDF_DEC = new DeclarationDataReportType(2, "PDF_DEC");
    public static final DeclarationDataReportType JASPER_DEC = new DeclarationDataReportType(3, "JASPER_DEC");
    public static final DeclarationDataReportType SPECIFIC_REPORT_DEC = new DeclarationDataReportType(4, "SPECIFIC_REPORT_DEC");
    public static final DeclarationDataReportType EXCEL_TEMPLATE_DEC = new DeclarationDataReportType(13, "EXCEL_TEMPLATE_DEC");

    /**
     * Идентификатор
     */
    private int id;
    /**
     * Код
     */
    private String code;
    /**
     * Тип специфичного отчета
     */
    private DeclarationSubreport subreport;

    public DeclarationDataReportType(int id, String code) {
        this.id = id;
        this.code = code;
    }

    public void setSubreport(DeclarationSubreport subreport) {
        if (this.id != SPECIFIC_REPORT_DEC.id && subreport != null) {
            throw new IllegalArgumentException("Некорректный тип отчета: " + subreport.getAlias());
        }
        this.subreport = subreport;
    }

    public static DeclarationDataReportType createSpecificReport() {
        return new DeclarationDataReportType(SPECIFIC_REPORT_DEC.id, SPECIFIC_REPORT_DEC.code);
    }

    public static DeclarationDataReportType createSpecificReport(DeclarationSubreport subreport) {
        DeclarationDataReportType reportType = createSpecificReport();
        reportType.setSubreport(subreport);
        return reportType;
    }
}
