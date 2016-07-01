package com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.client;

import com.aplana.sbrf.taxaccounting.web.widget.codemirror.client.CodeMirror;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

public class DeclarationTemplateScriptView extends ViewWithUiHandlers<DeclarationTemplateScriptUiHandlers>
		implements DeclarationTemplateScriptPresenter.MyView {

    public interface Binder extends UiBinder<Widget, DeclarationTemplateScriptView> { }

    private final static int DEFAULT_TABLE_TOP_POSITION = 33;
    private final static int LOCK_INFO_BLOCK_HEIGHT = 25;

    @UiField
    CodeMirror createScript;

    @Inject
	@UiConstructor
	public DeclarationTemplateScriptView(Binder binder) {
		initWidget(binder.createAndBindUi(this));
        init();
	}

    @UiHandler("createScript")
    void onDecNameChanged(ChangeEvent event) {
        if (getUiHandlers() != null) {
            getUiHandlers().onInfoChanged();
        }
    }

	private void init() {
    }

    //@Override
    public void changeTableTopPosition(Boolean isLockInfoVisible) {
        Style formDataTableStyle = createScript.getElement().getStyle();
        int downShift = 0;
        if (isLockInfoVisible){
            downShift = LOCK_INFO_BLOCK_HEIGHT;
        }
        formDataTableStyle.setProperty("top", DEFAULT_TABLE_TOP_POSITION + downShift, Style.Unit.PX);
    }

    @Override
    public void setScriptCode(String text) {
        createScript.setText(text);
    }

    @Override
    public String getScriptCode() {
        return createScript.getText();
    }
}
