package com.aplana.sbrf.taxaccounting.web.module.formdata.shared;

import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.FormDataAccessParams;
import com.aplana.sbrf.taxaccounting.model.FormStyle;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.gwtplatform.dispatch.shared.Result;

import java.util.List;
/**
 * 
 * @author Eugene Stetsenko
 * Результат запроса для получения даных формы.
 * Возвращает даные формы и флаги доступа для текущего пользователя.
 * 
 */
public class GetFormDataResult implements Result {
	
	public static enum FormMode {
		READ_UNLOCKED, // Открыта для чтения
		READ_LOCKED, // Открыта для чтения и заблокирована другим пользователем
		EDIT // Открыта для редактирования
	}
	
	private FormData formData;
	
	private List<LogEntry> logEntries;
	
	private String departmenName;
	
	private String reportPeriod;
	
	private FormDataAccessParams formDataAccessParams;

	private boolean numberedHeader;

	private List<FormStyle> allStyles;

	/* Т.к. на GWT уровне сложно преобразовать Date в дату формата dd.mm.yyyy hh:mm (из-за того что функции работы
	с временем в Date - Deprecated, а Calendar не поддерживается), то клиенту мы будем посылать уже сформировнную в
	таком формате строку*/
	private String lockDate;
	private String lockedByUser;

	private FormMode formMode;
	
	private boolean fixedRows;

	public FormData getFormData() {
		return formData;
	}
	
	public void setFormData(FormData formData) {
		this.formData = formData;
	}
	
	public String getDepartmenName() {
		return departmenName;
	}

	public void setDepartmenName(String departmenName) {
		this.departmenName = departmenName;
	}

	public String getReportPeriod() {
		return reportPeriod;
	}

	public void setReportPeriod(String reportPeriod) {
		this.reportPeriod = reportPeriod;
	}

	public FormDataAccessParams getFormDataAccessParams() {
		return formDataAccessParams;
	}

	public void setFormDataAccessParams(FormDataAccessParams formDataAccessParams) {
		this.formDataAccessParams = formDataAccessParams;
	}

	public List<LogEntry> getLogEntries() {
		return logEntries;
	}

	public void setLogEntries(List<LogEntry> logEntries) {
		this.logEntries = logEntries;
	}

	public boolean isNumberedHeader() {
		return numberedHeader;
	}

	public void setNumberedHeader(boolean numberedHeader) {
		this.numberedHeader = numberedHeader;
	}

	public List<FormStyle> getAllStyles() {
		return allStyles;
	}

	public void setAllStyles(List<FormStyle> allStyles) {
		this.allStyles = allStyles;
	}

	public String getLockDate() {
		return lockDate;
	}

	public void setLockDate(String lockDate) {
		this.lockDate = lockDate;
	}

	public String getLockedByUser() {
		return lockedByUser;
	}

	public void setLockedByUser(String lockedByUser) {
		this.lockedByUser = lockedByUser;
	}

	public FormMode getFormMode() {
		return formMode;
	}

	public void setFormMode(FormMode formMode) {
		this.formMode = formMode;
	}

	public boolean isFixedRows() {
		return fixedRows;
	}

	public void setFixedRows(boolean fixedRows) {
		this.fixedRows = fixedRows;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("GetFormDataResult [formData=");
		builder.append(formData);
		builder.append(", logEntries=");
		builder.append(logEntries);
		builder.append(", departmenName=");
		builder.append(departmenName);
		builder.append(", reportPeriod=");
		builder.append(reportPeriod);
		builder.append(", formDataAccessParams=");
		builder.append(formDataAccessParams);
		builder.append(", numberedHeader=");
		builder.append(numberedHeader);
		builder.append(", allStyles=");
		builder.append(allStyles);
		builder.append(", lockDate=");
		builder.append(lockDate);
		builder.append(", lockedByUser=");
		builder.append(lockedByUser);
		builder.append(", formMode=");
		builder.append(formMode);
		builder.append(", fixedRows=");
		builder.append(fixedRows);
		builder.append("]");
		return builder.toString();
	}
	
	

}