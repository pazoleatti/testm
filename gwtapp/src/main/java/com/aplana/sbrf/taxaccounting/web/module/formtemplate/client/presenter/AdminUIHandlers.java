package com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.presenter;

import com.aplana.sbrf.taxaccounting.web.module.formtemplate.shared.FormTypeTemplate;
import com.gwtplatform.mvp.client.UiHandlers;

public interface AdminUIHandlers  extends UiHandlers {
    void onCreateClicked();
    void onDeleteClick();
    void onSelectionChanged(FormTypeTemplate selectedItem);
}
