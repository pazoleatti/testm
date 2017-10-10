package com.aplana.sbrf.taxaccounting.model;

public class ReportAvailableResult {
    boolean downloadXmlAvailable;
    boolean downloadXlsxAvailable;
    boolean downloadSpecificAvailable;

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

    public boolean isDownloadSpecificAvailable() {
        return downloadSpecificAvailable;
    }

    public void setDownloadSpecificAvailable(boolean downloadSpecificAvailable) {
        this.downloadSpecificAvailable = downloadSpecificAvailable;
    }
}
