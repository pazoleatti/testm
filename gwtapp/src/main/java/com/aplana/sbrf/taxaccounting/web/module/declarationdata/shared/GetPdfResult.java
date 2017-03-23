package com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared;

import com.aplana.sbrf.taxaccounting.web.widget.pdfviewer.shared.Pdf;

public class GetPdfResult extends DeclarationDataResult {
    private static final long serialVersionUID = 7859961980147513071L;

    private Pdf pdf;

    public Pdf getPdf() {
        return pdf;
    }

    public void setPdf(Pdf pdf) {
        this.pdf = pdf;
    }
}
