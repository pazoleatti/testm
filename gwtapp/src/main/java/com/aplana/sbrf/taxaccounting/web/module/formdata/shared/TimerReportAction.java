package com.aplana.sbrf.taxaccounting.web.module.formdata.shared;

import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

import java.util.List;

/**
 * Получение статуса всех отчетов
 *
 * @author lhaziev
 */
public class TimerReportAction extends UnsecuredActionImpl<TimerReportResult> implements ActionName {

    private long formDataId;
    private boolean isShowChecked;
    private boolean manual;
    private boolean saved;
    private List<String> reportTypes;

    public long getFormDataId() {
        return formDataId;
    }

    public void setFormDataId(long formDataId) {
        this.formDataId = formDataId;
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

    public List<String> getReportTypes() {
        return reportTypes;
    }

    public void setReportTypes(List<String> reportTypes) {
        this.reportTypes = reportTypes;
    }

    @Override
	public String getName() {
		return "Проверка наличия файлов для выгрузки";
	}
}
