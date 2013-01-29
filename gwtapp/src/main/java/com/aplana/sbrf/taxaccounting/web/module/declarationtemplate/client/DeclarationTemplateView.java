package com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.client;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;


public class DeclarationTemplateView extends ViewWithUiHandlers<DeclarationTemplateUiHandlers>
		implements DeclarationTemplatePresenter.MyView {

	public interface Binder extends UiBinder<Widget, DeclarationTemplateView> { }

	private final Widget widget;

	@UiField
	Label titleLabel;

	@UiField
	Button saveButton;

	@UiField
	Button resetButton;

	@UiField
	Button cancelButton;

	@Inject
	public DeclarationTemplateView(Binder uiBinder) {
		widget = uiBinder.createAndBindUi(this);
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@UiHandler("saveButton")
	public void onSave(ClickEvent event){
		if(getUiHandlers()!=null){
			getUiHandlers().save();
		}
	}

	@UiHandler("resetButton")
	public void onReset(ClickEvent event){
		if(getUiHandlers()!=null){
			getUiHandlers().reset();
		}
	}

	@UiHandler("cancelButton")
	public void onCancel(ClickEvent event){
		getUiHandlers().close();
	}

	@Override
	public void setTitle(String title) {
		titleLabel.setText(title);
	}
}