package com.aplana.sbrf.taxaccounting.web.widget.declarationparamsdialog.client;

import com.aplana.sbrf.taxaccounting.web.main.api.client.GINContextHolder;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.IntegerBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;
import com.gwtplatform.mvp.client.PopupViewWithUiHandlers;

/**
 * Диалог ввода параметров уведомления
 *
 * @author Dmitriy Levykin
 */
public class DeclarationParamsDialog extends PopupViewWithUiHandlers<DeclarationParamsDialogUiHandlers> implements DeclarationParamsDialogPresenter.MyView {

    public interface Binder extends UiBinder<PopupPanel, DeclarationParamsDialog> {
    }
    private static Binder binder = GWT.create(Binder.class);

    @UiField
    Button okButton;

    @UiField
    Button cancelButton;

    @UiField
    IntegerBox pagesCountBox;

    private final PopupPanel widget;

    private DeclarationParamsDialogPresenter presenter;

    public DeclarationParamsDialog() {
        super(GINContextHolder.getEventBus());
        widget = binder.createAndBindUi(this);
        widget.setAnimationEnabled(true);
        presenter = new DeclarationParamsDialogPresenter(this);
        // По Enter передача значения
        pagesCountBox.addKeyPressHandler(new KeyPressHandler() {
            @Override
            public void onKeyPress(KeyPressEvent event) {
                if (event != null && event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
                    save();
                }
            }
        });
    }

    @Override
    public Widget asWidget() {
        return widget;
    }

    @Override
    public void clearInput() {
        pagesCountBox.setValue(null);
        pagesCountBox.setFocus(true);
    }

    @Override
    public Integer getPagesCount() {
        return pagesCountBox.getValue();
    }

    private void save() {
        if (getUiHandlers() != null) {
            getUiHandlers().onConfirm();
        }
    }

    @UiHandler("okButton")
    public void onSave(ClickEvent event) {
        save();
    }

    @UiHandler("cancelButton")
    public void onCancel(ClickEvent event) {
        if (getUiHandlers() != null) {
            getUiHandlers().hide();
        }
    }

    public void setConfirmHandler(ConfirmHandler confirmHandler) {
        if (getUiHandlers() != null) {
            getUiHandlers().setConfirmHandler(confirmHandler);
        }
    }

    public DeclarationParamsDialogPresenter getPresenter() {
        return presenter;
    }
}
