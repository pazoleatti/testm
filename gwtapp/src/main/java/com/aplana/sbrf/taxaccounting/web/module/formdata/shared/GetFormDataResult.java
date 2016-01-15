package com.aplana.sbrf.taxaccounting.web.module.formdata.shared;

import com.aplana.sbrf.taxaccounting.model.*;
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
	private static final long serialVersionUID = 1564846831548468989L;

    public enum FormMode {
		READ_UNLOCKED, // Открыта для чтения
		READ_LOCKED, // Открыта для чтения и заблокирована другим пользователем
		EDIT // Открыта для редактирования
	}

    private DepartmentReportPeriod departmentReportPeriod;
    private DepartmentReportPeriod comparativePeriod;
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
    private List<String> specificReportTypes;
	/* Т.к. на GWT уровне сложно преобразовать Date в дату формата dd.mm.yyyy hh:mm (из-за того что функции работы
	с временем в Date - Deprecated, а Calendar не поддерживается), то клиенту мы будем посылать уже сформировнную в
	таком формате строку*/
	private String lockDate;
	private String lockedByUser;
	private FormMode formMode;
	private boolean fixedRows;
    private boolean updating;
    private boolean editLock;

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

    public List<String> getSpecificReportTypes() {
        return specificReportTypes;
    }

    public void setSpecificReportTypes(List<String> specificReportTypes) {
        this.specificReportTypes = specificReportTypes;
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

    public DepartmentReportPeriod getComparativePeriod() {
        return comparativePeriod;
    }

    public void setComparativePeriod(DepartmentReportPeriod comparativPeriod) {
        this.comparativePeriod = comparativPeriod;
    }

    public boolean isUpdating() {
        return updating;
    }

    public void setUpdating(boolean updating) {
        this.updating = updating;
    }

    public boolean isEditLock() {
        return editLock;
    }

    public void setEditLock(boolean editLock) {
        this.editLock = editLock;
    }
}