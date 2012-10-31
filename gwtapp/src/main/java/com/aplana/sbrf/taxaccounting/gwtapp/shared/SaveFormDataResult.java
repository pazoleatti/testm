package com.aplana.sbrf.taxaccounting.gwtapp.shared;

import java.util.List;

import com.aplana.sbrf.taxaccounting.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.FormData;
import com.gwtplatform.dispatch.shared.Result;

/** @author Vitalii Samolovskikh */
public class SaveFormDataResult implements Result {
    private List<LogEntry> logEntries;
    private FormData formData;
    
    public SaveFormDataResult() {
    }

    public List<LogEntry> getLogEntries() {
        return logEntries;
    }

    public void setLogEntries(List<LogEntry> logEntries) {
        this.logEntries = logEntries;
    }

	public FormData getFormData() {
		return formData;
	}

	public void setFormData(FormData formData) {
		this.formData = formData;
	}

}
