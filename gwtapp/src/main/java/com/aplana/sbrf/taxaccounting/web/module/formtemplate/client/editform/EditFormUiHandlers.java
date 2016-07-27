package com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.editform;

import com.aplana.sbrf.taxaccounting.web.module.formtemplate.shared.FormTypeTemplate;
import com.gwtplatform.mvp.client.UiHandlers;

public interface EditFormUiHandlers extends UiHandlers {
    boolean onSave();
    void onCancel();
    void setModel(FormTypeTemplate model);
}
