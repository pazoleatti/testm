package com.aplana.sbrf.taxaccounting.web.module.formdata.shared;

import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.FormDataAccessParams;
import com.aplana.sbrf.taxaccounting.model.FormStyle;
import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.gwtplatform.dispatch.shared.Result;

import java.util.Date;
import java.util.List;
/**
 * 
 * @author Eugene Stetsenko
 * Результат запроса для получения даных формы.
 * Возвращает даные формы и флаги доступа для текущего пользователя.
 * 
 */
public class GetFormDataResult implements Result {
	private static final long serialVersionUID = 5032224706310298119L;

	public static enum FormMode {
		READ_UNLOCKED, // Открыта для чтения
		READ_LOCKED, // Открыта для чтения и заблокирована другим пользователем
		EDIT // Открыта для редактирования
	}
	
	private FormData formData;
	
	private String templateFormName;
	
	private String uuid;
	
	private String departmenName;
	
	private ReportPeriod reportPeriod;
	
	private FormDataAccessParams formDataAccessParams;

	private boolean numberedHeader;

	private List<FormStyle> allStyles;
	
	private boolean balancePeriod;

	/* Т.к. на GWT уровне сложно преобразовать Date в дату формата dd.mm.yyyy hh:mm (из-за того что функции работы
	с временем в Date - Deprecated, а Calendar не поддерживается), то клиенту мы будем посылать уже сформировнную в
	таком формате строку*/
	private String lockDate;
	private String lockedByUser;

	private FormMode formMode;
	
	private boolean fixedRows;

	private Date reportPeriodStartDate;

	private Date reportPeriodEndDate;

    private Integer reportPeriodYear;

	public FormData getFormData() {
		return formData;
	}
	
	public void setFormData(FormData formData) {
		this.formData = formData;
	}
	
	public String getTemplateFormName() {
		return templateFormName;
	}

	public void setTemplateFormName(String templateFormName) {
		this.templateFormName = templateFormName;
	}

	public String getDepartmenName() {
		return departmenName;
	}

	public void setDepartmenName(String departmenName) {
		this.departmenName = departmenName;
	}

	public ReportPeriod getReportPeriod() {
		return reportPeriod;
	}

	public void setReportPeriod(ReportPeriod reportPeriod) {
		this.reportPeriod = reportPeriod;
	}

	public FormDataAccessParams getFormDataAccessParams() {
		return formDataAccessParams;
	}

	public void setFormDataAccessParams(FormDataAccessParams formDataAccessParams) {
		this.formDataAccessParams = formDataAccessParams;
	}

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
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

	public Date getReportPeriodStartDate() {
		return reportPeriodStartDate;
	}

	public void setReportPeriodStartDate(Date reportPeriodStartDate) {
		this.reportPeriodStartDate = reportPeriodStartDate;
	}

	public Date getReportPeriodEndDate() {
		return reportPeriodEndDate;
	}

	public void setReportPeriodEndDate(Date reportPeriodEndDate) {
		this.reportPeriodEndDate = reportPeriodEndDate;
	}

	public boolean isBalancePeriod() {
		return balancePeriod;
	}

	public void setBalancePeriod(boolean balancePeriod) {
		this.balancePeriod = balancePeriod;
	}

    public Integer getReportPeriodYear() {
        return reportPeriodYear;
    }

    public void setReportPeriodYear(Integer reportPeriodYear) {
        this.reportPeriodYear = reportPeriodYear;
    }

    @Override
    public String toString() {
        return "GetFormDataResult{" +
                "formData=" + formData +
                ", templateFormName='" + templateFormName + '\'' +
                ", uuid='" + uuid + '\'' +
                ", departmenName='" + departmenName + '\'' +
                ", reportPeriod=" + reportPeriod +
                ", formDataAccessParams=" + formDataAccessParams +
                ", numberedHeader=" + numberedHeader +
                ", allStyles=" + allStyles +
                ", balancePeriod=" + balancePeriod +
                ", lockDate='" + lockDate + '\'' +
                ", lockedByUser='" + lockedByUser + '\'' +
                ", formMode=" + formMode +
                ", fixedRows=" + fixedRows +
                ", reportPeriodStartDate=" + reportPeriodStartDate +
                ", reportPeriodEndDate=" + reportPeriodEndDate +
                ", reportPeriodYear=" + reportPeriodYear +
                '}';
    }
}