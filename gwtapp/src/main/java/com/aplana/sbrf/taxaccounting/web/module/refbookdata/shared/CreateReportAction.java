package com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared;

import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

import java.util.Date;

/**
 * Создать задачу на формирование отчета
 * @author lhaziev
 */
public class CreateReportAction extends UnsecuredActionImpl<CreateReportResult> implements ActionName {
    private long refBookId;
    private String reportName;
    private Date version;
    private String searchPattern;
    private int sortColumnIndex;
    private boolean ascSorting;

    public long getRefBookId() {
        return refBookId;
    }

    public void setRefBookId(long refBookId) {
        this.refBookId = refBookId;
    }

    public String getReportName() {
        return reportName;
    }

    public void setReportName(String reportName) {
        this.reportName = reportName;
    }

    public Date getVersion() {
        return version;
    }

    public void setVersion(Date version) {
        this.version = version;
    }

    public String getSearchPattern() {
        return searchPattern;
    }

    public void setSearchPattern(String searchPattern) {
        this.searchPattern = searchPattern;
    }

    public int getSortColumnIndex() {
        return sortColumnIndex;
    }

    public void setSortColumnIndex(int sortColumnIndex) {
        this.sortColumnIndex = sortColumnIndex;
    }

    public boolean isAscSorting() {
        return ascSorting;
    }

    public void setAscSorting(boolean ascSorting) {
        this.ascSorting = ascSorting;
    }

    @Override
    public String getName() {
        return "";
    }
}
