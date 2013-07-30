package com.aplana.sbrf.taxaccounting.web.widget.refbookpicker.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.LeafValueEditor;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.*;

/**
 * Версионный справочник с выбором значения из выпадающего списка с пагинацией
 * @author Dmitriy Levykin
 */
public class RefBookPickerPopupWidget extends Composite implements HasValue<Long>, HasEnabled, LeafValueEditor<Long> {

    interface Binder extends UiBinder<Widget, RefBookPickerPopupWidget> {
    }

    private static Binder binder = GWT.create(Binder.class);

    private PopupPanel popupPanel;

    private RefBookPickerWidget popupWidget;

    @UiField
    TextBox text;

    @UiField
    Button selectButton;

    public RefBookPickerPopupWidget() {
        initWidget(binder.createAndBindUi(this));
        popupPanel = new PopupPanel(true, true);
    }

    public void setRefBookId(int id) {
        popupPanel.clear();
        popupWidget = new RefBookPickerWidget(id);
        popupPanel.add(popupWidget);

        popupWidget.addValueChangeHandler(new ValueChangeHandler<Long>() {
            @Override
            public void onValueChange(ValueChangeEvent<Long> event) {
                // TODO Переделать на текстовое представление
                text.setText(String.valueOf(event.getValue()));
                popupPanel.hide();
            }
        });
    }

    @UiHandler("selectButton")
    void onSelectButtonClicked(ClickEvent event){
        popupPanel.setPopupPosition(text.getAbsoluteLeft(), text.getAbsoluteTop() + text.getOffsetHeight());
        popupPanel.show();
    }

    @Override
    public Long getValue() {
        return popupWidget.getValue();
    }

    @Override
    public void setValue(Long value) {
        // TODO Переделать на текстовое представление
        text.setText(String.valueOf(value));
    }

    @Override
    public void setValue(Long value, boolean fireEvents) {
        // TODO Переделать на текстовое представление
        setValue(value);
    }

    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Long> handler) {
        return popupWidget.addValueChangeHandler(handler);
    }

    @Override
    public boolean isEnabled() {
        return selectButton.isEnabled();
    }

    @Override
    public void setEnabled(boolean enabled) {
        selectButton.setEnabled(enabled);
    }
}
