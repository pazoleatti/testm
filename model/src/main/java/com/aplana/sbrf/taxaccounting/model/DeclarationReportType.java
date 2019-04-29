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
public class DeclarationReportType implements Serializable {
    private static final long serialVersionUID = -1156464397165684964L;

    public static final DeclarationReportType EXCEL_DEC = new DeclarationReportType(0, "EXCEL_DEC");
    public static final DeclarationReportType XML_DEC = new DeclarationReportType(1, "XML_DEC");
    public static final DeclarationReportType PDF_DEC = new DeclarationReportType(2, "PDF_DEC");
    public static final DeclarationReportType JASPER_DEC = new DeclarationReportType(3, "JASPER_DEC");
    public static final DeclarationReportType SPECIFIC_REPORT_DEC = new DeclarationReportType(4, "SPECIFIC_REPORT_DEC");
    public static final DeclarationReportType EXCEL_TEMPLATE_DEC = new DeclarationReportType(13, "EXCEL_TEMPLATE_DEC");

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

    public DeclarationReportType(int id, String code) {
        this.id = id;
        this.code = code;
    }

    public void setSubreport(DeclarationSubreport subreport) {
        if (this.id != SPECIFIC_REPORT_DEC.id && subreport != null) {
            throw new IllegalArgumentException("Некорректный тип отчета: " + subreport.getAlias());
        }
        this.subreport = subreport;
    }

    public static DeclarationReportType createSpecificReport() {
        return new DeclarationReportType(SPECIFIC_REPORT_DEC.id, SPECIFIC_REPORT_DEC.code);
    }

    public static DeclarationReportType createSpecificReport(DeclarationSubreport subreport) {
        DeclarationReportType reportType = createSpecificReport();
        reportType.setSubreport(subreport);
        return reportType;
    }
}
