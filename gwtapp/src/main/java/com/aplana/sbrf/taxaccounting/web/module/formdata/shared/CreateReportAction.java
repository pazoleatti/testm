package com.aplana.sbrf.taxaccounting.web.module.formdata.shared;

import com.aplana.sbrf.taxaccounting.model.FormDataReportType;
import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

/**
 * Создание задачи для генерации отчетов
 *
 * @author lhaziev
 */
public class CreateReportAction extends UnsecuredActionImpl<CreateReportResult> implements ActionName {

    private boolean force;
    private long formDataId;
    private FormDataReportType type;
    private boolean isShowChecked;
    private boolean manual;
    private boolean saved;

    public boolean isForce() {
        return force;
    }

    public void setForce(boolean force) {
        this.force = force;
    }

    public long getFormDataId() {
        return formDataId;
    }

    public void setFormDataId(long formDataId) {
        this.formDataId = formDataId;
    }

    public FormDataReportType getType() {
        return type;
    }

    public void setType(FormDataReportType type) {
        this.type = type;
    }

    public boolean isShowChecked() {
        return isShowChecked;
    }

    public void setShowChecked(boolean showChecked) {
        isShowChecked = showChecked;
    }

    public boolean isManual() {
        return manual;
    }

    public void setManual(boolean manual) {
        this.manual = manual;
    }

    public boolean isSaved() {
        return saved;
    }

    public void setSaved(boolean saved) {
        this.saved = saved;
    }

    @Override
	public String getName() {
		return "";
	}
}
