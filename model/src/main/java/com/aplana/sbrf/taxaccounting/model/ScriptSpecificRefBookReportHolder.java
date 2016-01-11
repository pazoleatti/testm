package com.aplana.sbrf.taxaccounting.model;

import com.aplana.sbrf.taxaccounting.model.formdata.HeaderCell;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;

import java.io.OutputStream;
import java.util.Date;
import java.util.List;

/**
 * Объект-хранилище данных для формирования специфичных отчетов в скрипте
 * @author lhaziev
 */
public class ScriptSpecificRefBookReportHolder {

    /**
     * Тип специфичного отчета
     */
    private String specificReportType;
    private OutputStream fileOutputStream;
    private Date version;
    private String filter;
    private String searchPattern;
    private RefBookAttribute sortAttribute;
    private boolean isSortAscending;
    /**
     * Скрипт должен вернуть название файла для сформированного отчета
     */
    private String fileName;

    public String getSpecificReportType() {
        return specificReportType;
    }

    public void setSpecificReportType(String specificReportType) {
        this.specificReportType = specificReportType;
    }

    public OutputStream getFileOutputStream() {
        return fileOutputStream;
    }

    public void setFileOutputStream(OutputStream fileOutputStream) {
        this.fileOutputStream = fileOutputStream;
    }

    public Date getVersion() {
        return version;
    }

    public void setVersion(Date version) {
        this.version = version;
    }

    public String getFilter() {
        return filter;
    }

    public String getSearchPattern() {
        return searchPattern;
    }

    public void setSearchPattern(String searchPattern) {
        this.searchPattern = searchPattern;
    }

    public RefBookAttribute getSortAttribute() {
        return sortAttribute;
    }

    public void setSortAttribute(RefBookAttribute sortAttribute) {
        this.sortAttribute = sortAttribute;
    }

    public boolean isSortAscending() {
        return isSortAscending;
    }

    public void setSortAscending(boolean isSortAscending) {
        this.isSortAscending = isSortAscending;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
