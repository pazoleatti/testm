package com.aplana.sbrf.taxaccounting.web.module.formdata.shared;

import com.aplana.sbrf.taxaccounting.model.DepartmentReportPeriod;
import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.FormDataAccessParams;
import com.aplana.sbrf.taxaccounting.model.FormStyle;
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
	private static final long serialVersionUID = 5032224706310298119L;

    public static enum FormMode {
		READ_UNLOCKED, // Открыта для чтения
		READ_LOCKED, // Открыта для чтения и заблокирована другим пользователем
		EDIT // Открыта для редактирования
	}

    private DepartmentReportPeriod departmentReportPeriod;
    private DepartmentReportPeriod comparativPeriod;
	
	private FormData formData;
	
	private String templateFormName;
	
	private String uuid;
	
	private String departmentName;

    private String departmentFullName;

	private FormDataAccessParams formDataAccessParams;

	private List<FormStyle> allStyles;

    private boolean existManual;
    private boolean isBankSummaryForm;
    private boolean canCreatedManual;
    private boolean correctionDiff;
    private boolean readOnly;

	/* Т.к. на GWT уровне сложно преобразовать Date в дату формата dd.mm.yyyy hh:mm (из-за того что функции работы
	с временем в Date - Deprecated, а Calendar не поддерживается), то клиенту мы будем посылать уже сформировнную в
	таком формате строку*/
	private String lockDate;
	private String lockedByUser;

	private FormMode formMode;
	
	private boolean fixedRows;

    public boolean canCreatedManual() {
        return canCreatedManual;
    }

    public void setCanCreatedManual(boolean canCreatedManual) {
        this.canCreatedManual = canCreatedManual;
    }

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

	public String getDepartmentName() {
		return departmentName;
	}

	public void setDepartmentName(String departmentName) {
		this.departmentName = departmentName;
	}

    public String getDepartmentFullName() {
        return departmentFullName;
    }

    public void setDepartmentFullName(String departmentFullName) {
        this.departmentFullName = departmentFullName;
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

    public boolean existManual() {
        return existManual;
    }

    public void setExistManual(boolean existManual) {
        this.existManual = existManual;
    }

    public boolean isBankSummaryForm() {
        return isBankSummaryForm;
    }

    public void setBankSummaryForm(boolean bankSummaryForm) {
        isBankSummaryForm = bankSummaryForm;
    }

    public DepartmentReportPeriod getDepartmentReportPeriod() {
        return departmentReportPeriod;
    }

    public void setDepartmentReportPeriod(DepartmentReportPeriod departmentReportPeriod) {
        this.departmentReportPeriod = departmentReportPeriod;
    }

    public boolean isCorrectionDiff() {
        return correctionDiff;
    }

    public void setCorrectionDiff(boolean correctionDiff) {
        this.correctionDiff = correctionDiff;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    public DepartmentReportPeriod getComparativPeriod() {
        return comparativPeriod;
    }

    public void setComparativPeriod(DepartmentReportPeriod comparativPeriod) {
        this.comparativPeriod = comparativPeriod;
    }
}