package com.aplana.sbrf.taxaccounting.web.module.declarationdata.client.move_to_create;

import com.aplana.gwt.client.ModalWindow;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.PopupViewWithUiHandlers;

public class MoveToCreateView extends PopupViewWithUiHandlers<MoveToCreateUiHandlers> implements MoveToCreatePresenter.MyView {

    public interface Binder extends UiBinder<PopupPanel, MoveToCreateView> {
    }

    private final PopupPanel widget;

    @UiField
    ModalWindow modalWindow;
    @UiField
    com.google.gwt.user.client.ui.TextArea note;
    @UiField
    Button saveButton, cancelButton;

    @Inject
    public MoveToCreateView(MoveToCreateView.Binder uiBinder, EventBus eventBus) {
        super(eventBus);
        widget = uiBinder.createAndBindUi(this);
        widget.setAnimationEnabled(true);
        note.getElement().setAttribute("maxLength", "1000");
    }

    @Override
    public Widget asWidget() {
        return widget;
    }

    @UiHandler("saveButton")
    public void onContinueClicked(ClickEvent event) {
        getUiHandlers().onContinue();
    }

    @UiHandler("cancelButton")
    public void onCancleClicked(ClickEvent event) {
        getUiHandlers().onCancel();
    }

    @Override
    public String getText() {
        return note.getText();
    }
}