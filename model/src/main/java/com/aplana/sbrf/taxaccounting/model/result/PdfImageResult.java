package com.aplana.sbrf.taxaccounting.model.result;

public class PdfImageResult {

    private String base64String;

    private int pagesTotal;

    public void setBase64String(String base64String) {
        this.base64String = base64String;
    }

    public String getBase64String() {
        return base64String;
    }

    public int getPagesTotal() {
        return pagesTotal;
    }

    public void setPagesTotal(int pagesTotal) {
        this.pagesTotal = pagesTotal;
    }
}
