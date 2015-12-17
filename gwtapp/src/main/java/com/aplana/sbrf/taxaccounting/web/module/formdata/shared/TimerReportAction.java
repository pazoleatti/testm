package com.aplana.sbrf.taxaccounting.web.module.formdata.shared;

import com.aplana.sbrf.taxaccounting.model.ReportType;
import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

/**
 * Действие добавления строки.
 *
 * @author lhaziev
 */
public class TimerReportAction extends UnsecuredActionImpl<TimerReportResult> implements ActionName {

    private long formDataId;
    private ReportType type;
    private boolean isShowChecked;
    private boolean manual;
    private boolean saved;
    private String specificReportType;

    public long getFormDataId() {
        return formDataId;
    }

    public void setFormDataId(long formDataId) {
        this.formDataId = formDataId;
    }

    public ReportType getType() {
        return type;
    }

    public void setType(ReportType type) {
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

    public String getSpecificReportType() {
        return specificReportType;
    }

    public void setSpecificReportType(String specificReportType) {
        this.specificReportType = specificReportType;
    }

    @Override
	public String getName() {
		return "Проверка наличия файлов для выгрузки";
	}
}
