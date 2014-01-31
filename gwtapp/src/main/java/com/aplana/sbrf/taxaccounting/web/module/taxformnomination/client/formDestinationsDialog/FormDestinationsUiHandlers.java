package com.aplana.sbrf.taxaccounting.web.module.taxformnomination.client.formDestinationsDialog;

import com.aplana.sbrf.taxaccounting.model.FormTypeKind;
import com.gwtplatform.mvp.client.UiHandlers;

import java.util.List;

public interface FormDestinationsUiHandlers extends UiHandlers {
    // логика нажатия формы создать
	void onConfirm();
    void onEdit(List<FormTypeKind> formTypeKinds);
}
