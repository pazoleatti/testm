package com.aplana.sbrf.taxaccounting.web.module.taxformnomination.client.declarationDestinationsDialog;

import com.aplana.sbrf.taxaccounting.model.DeclarationTypeAssignment;
import com.gwtplatform.mvp.client.UiHandlers;

import java.util.List;

public interface DeclarationDestinationsUiHandlers extends UiHandlers {
    // логика нажатия формы создать
	void onConfirm();
    void onEdit(List<DeclarationTypeAssignment> declarationTypeAssignments);
}
