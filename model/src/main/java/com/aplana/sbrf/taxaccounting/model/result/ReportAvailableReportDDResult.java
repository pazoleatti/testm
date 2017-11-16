package com.aplana.sbrf.taxaccounting.model.result;

public class ReportAvailableReportDDResult {

    private boolean availablePdf;

    private boolean downloadXmlAvailable;
    private boolean downloadXlsxAvailable;

    private boolean declarationDataExist = true;

    public boolean isAvailablePdf() {
        return availablePdf;
    }

    public void setAvailablePdf(boolean availablePdf) {
        this.availablePdf = availablePdf;
    }

    public boolean isDeclarationDataExist() {
        return declarationDataExist;
    }

    public void setDeclarationDataExist(boolean declarationDataExist) {
        this.declarationDataExist = declarationDataExist;
    }

    public boolean isDownloadXmlAvailable() {
        return downloadXmlAvailable;
    }

    public void setDownloadXmlAvailable(boolean downloadXmlAvailable) {
        this.downloadXmlAvailable = downloadXmlAvailable;
    }

    public boolean isDownloadXlsxAvailable() {
        return downloadXlsxAvailable;
    }

    public void setDownloadXlsxAvailable(boolean downloadXlsxAvailable) {
        this.downloadXlsxAvailable = downloadXlsxAvailable;
    }
}
