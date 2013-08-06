package com.aplana.sbrf.taxaccounting.web.widget.refbookpicker.client;

import java.util.Date;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * Версионный справочник с выбором значения из выпадающего списка с пагинацией
 * @author Dmitriy Levykin
 */
public class RefBookPickerPopupWidget extends Composite implements RefBookPickerPopup {

    interface Binder extends UiBinder<Widget, RefBookPickerPopupWidget> {
    }

    private Long attrId;

    private static Binder binder = GWT.create(Binder.class);

    private PopupPanel popupPanel;

    private RefBookPicker refBookPiker;

    @UiField
    TextBox text;

    @UiField
    Button selectButton;

    public RefBookPickerPopupWidget() {
        initWidget(binder.createAndBindUi(this));
        popupPanel = new PopupPanel(true, true);
        refBookPiker = new RefBookPickerWidget();
        popupPanel.add(refBookPiker);
    }

    @UiHandler("selectButton")
    void onSelectButtonClicked(ClickEvent event){
        refBookPiker.setAcceptableValues(attrId);
        popupPanel.setPopupPosition(text.getAbsoluteLeft(), text.getAbsoluteTop() + text.getOffsetHeight());
        popupPanel.show();
    }

    @Override
    public Long getValue() {
        return refBookPiker.getValue();
    }

    @Override
    public void setValue(Long value) {
        refBookPiker.setValue(value);
    }

    @Override
    public void setValue(Long value, boolean fireEvents) {
        refBookPiker.setValue(value);
        if (fireEvents){
            ValueChangeEvent.fire(this, value);
        }
    }

    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Long> handler) {
        return refBookPiker.addValueChangeHandler(handler);
    }

    @Override
    public boolean isEnabled() {
        return selectButton.isVisible();
    }

    @Override
    public void setEnabled(boolean enabled) {
        // При недоступности кнопка прячется
        selectButton.setVisible(enabled);
    }

	@Override
	public void setAcceptableValues(long refBookAttrId) {
		setAcceptableValues(refBookAttrId, null, null);
	}

    // Для совместимости с uibinder'ом
    /**
     * @see com.aplana.sbrf.taxaccounting.web.widget.refbookpicker.client.RefBookPickerPopupWidget#setAcceptableValues
     * @param refBookAttrId
     */
    public void setAcceptableIntValues(int refBookAttrId) {
        setAcceptableValues((long)refBookAttrId);
    }

	@Override
	public void setAcceptableValues(long refBookAttrId, Date date1, Date date2) {
        attrId = refBookAttrId;

        refBookPiker.addValueChangeHandler(new ValueChangeHandler<Long>() {
            @Override
            public void onValueChange(ValueChangeEvent<Long> event) {
                text.setText(refBookPiker.getDereferenceValue());
                setValue(event.getValue(), true);
                popupPanel.hide();
            }
        });
	}

	@Override
	public String getDereferenceValue() {
		return text.getValue();
	}

	@Override
	public void setDereferenceValue(String value) {
		text.setValue(value);
	}

    /**
     * Id отображаемого атрибута
     * @return
     */
    public Long getAttributeId() {
        return attrId;
    }
}
