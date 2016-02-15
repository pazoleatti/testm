package com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared;

import com.aplana.sbrf.taxaccounting.web.widget.pdfviewer.shared.Pdf;
import com.gwtplatform.dispatch.shared.Result;

public class GetPdfResult implements Result {
    private static final long serialVersionUID = 7859961980147513071L;

    private Pdf pdf;

    public Pdf getPdf() {
        return pdf;
    }

    public void setPdf(Pdf pdf) {
        this.pdf = pdf;
    }
}
