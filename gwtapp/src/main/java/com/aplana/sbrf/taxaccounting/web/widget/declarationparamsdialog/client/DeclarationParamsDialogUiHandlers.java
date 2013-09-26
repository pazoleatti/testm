package com.aplana.sbrf.taxaccounting.web.widget.declarationparamsdialog.client;


import com.gwtplatform.mvp.client.UiHandlers;

public interface DeclarationParamsDialogUiHandlers extends UiHandlers {
    void setConfirmHandler(ConfirmHandler confirmHandler);
	void onConfirm();
	void hide();
}
