package com.aplana.sbrf.taxaccounting.web.module.formtemplate.shared;

import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.gwtplatform.dispatch.shared.Result;

import java.util.List;

/**
 * @author Vitalii Samolovskikh
 */
public class UpdateFormResult implements Result {
	private List<LogEntry> logEntries;

    private int formTemplateId;

	public List<LogEntry> getLogEntries() {
		return logEntries;
	}

	public void setLogEntries(List<LogEntry> logEntries) {
		this.logEntries = logEntries;
	}

    public int getFormTemplateId() {
        return formTemplateId;
    }

    public void setFormTemplateId(int formTemplateId) {
        this.formTemplateId = formTemplateId;
    }
}
