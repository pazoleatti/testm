package com.aplana.sbrf.taxaccounting.web.module.formdata.shared;

import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

/**
 * Модеь запроса источников/приемников
 * @author auldanov
 */
public class SourcesAction extends UnsecuredActionImpl<SourcesResult> implements ActionName {
    private FormData formData;
    private boolean showSources;
    private boolean showDestinations;
    private boolean showUncreated;

    public FormData getFormData() {
        return formData;
    }

    public void setFormData(FormData formData) {
        this.formData = formData;
    }

    public boolean isShowSources() {
        return showSources;
    }

    public void setShowSources(boolean showSources) {
        this.showSources = showSources;
    }

    public boolean isShowDestinations() {
        return showDestinations;
    }

    public void setShowDestinations(boolean showDestinations) {
        this.showDestinations = showDestinations;
    }

    public boolean isShowUncreated() {
        return showUncreated;
    }

    public void setShowUncreated(boolean showUncreated) {
        this.showUncreated = showUncreated;
    }

    @Override
    public String getName() {
        return "Обработка запроса на получение источников/приемников формы";
    }
}
