package com.aplana.sbrf.taxaccounting.model;

public class ReportAvailableResult {
    boolean downloadXmlAvailable;
    boolean downloadXlsxAvailable;
    boolean downloadRnuNdflPersonAllDb;
    boolean downloadReportKppOktmo;

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

    public boolean isDownloadRnuNdflPersonAllDb() {
        return downloadRnuNdflPersonAllDb;
    }

    public void setDownloadRnuNdflPersonAllDb(boolean downloadRnuNdflPersonAllDb) {
        this.downloadRnuNdflPersonAllDb = downloadRnuNdflPersonAllDb;
    }

    public boolean isDownloadReportKppOktmo() {
        return downloadReportKppOktmo;
    }

    public void setDownloadReportKppOktmo(boolean downloadReportKppOktmo) {
        this.downloadReportKppOktmo = downloadReportKppOktmo;
    }
}
