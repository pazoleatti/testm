package com.aplana.sbrf.taxaccounting.web.module.formdata.shared;

import java.util.List;

import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.gwtplatform.dispatch.shared.Result;
/**
 * 
 * @author Eugene Stetsenko
 * Результат запроса для пересчета формы.
 *
 */
public class RecalculateFormDataResult implements Result {
	private List<LogEntry> logEntries;
	private FormData formData;

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