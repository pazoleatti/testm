package com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared;

import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

import java.util.Date;

/**
 * @author lhaziev
 */
public class PreLoadCheckRefBookAction extends UnsecuredActionImpl<PreLoadCheckRefBookResult> implements ActionName {

    private long refBookId;
    private String fileName;
    private Date dateFrom, dateTo;

    public long getRefBookId() {
        return refBookId;
    }

    public void setRefBookId(long refBookId) {
        this.refBookId = refBookId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Date getDateFrom() {
        return dateFrom;
    }

    public void setDateFrom(Date dateFrom) {
        this.dateFrom = dateFrom;
    }

    public Date getDateTo() {
        return dateTo;
    }

    public void setDateTo(Date dateTo) {
        this.dateTo = dateTo;
    }

    @Override
	public String getName() {
		return "Загрузка справочника";
	}
}
