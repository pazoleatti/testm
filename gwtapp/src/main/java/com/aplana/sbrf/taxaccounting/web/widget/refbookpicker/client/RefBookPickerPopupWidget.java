package com.aplana.sbrf.taxaccounting.web.widget.refbookpicker.client;

import java.util.Date;

import com.aplana.sbrf.taxaccounting.web.widget.closabledialog.ClosableDialogBox;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;

/**
 * Версионный справочник с выбором значения из выпадающего списка с пагинацией
 * @author Dmitriy Levykin
 */
public class RefBookPickerPopupWidget extends Composite implements RefBookPickerPopup {

    interface Binder extends UiBinder<Widget, RefBookPickerPopupWidget> {
    }

	private Long attributeId;

	private Date date1;
    private Date date2;
    private String filter;

    private static Binder binder = GWT.create(Binder.class);

    private PopupPanel popupPanel;

    private RefBookPicker refBookPiker;

    @UiField
    TextBox text;

    @UiField
    Image selectButton;

    /** Признак модальности окна */
    private boolean modal;

    @UiConstructor
    public RefBookPickerPopupWidget(boolean modal) {
        initWidget(binder.createAndBindUi(this));
        this.modal = modal;
        if (modal) {
            popupPanel = new ClosableDialogBox(false, true);
        } else {
            popupPanel = new PopupPanel(true, true);
        }
        refBookPiker = new RefBookPickerWidget();
        popupPanel.add(refBookPiker);
        refBookPiker.addValueChangeHandler(new ValueChangeHandler<Long>() {
            @Override
            public void onValueChange(ValueChangeEvent<Long> event) {
                text.setText(refBookPiker.getDereferenceValue());
                setValue(event.getValue(), true);
                popupPanel.hide();
            }
        });
    }

    @UiHandler("selectButton")
    void onSelectButtonClicked(ClickEvent event){
        refBookPiker.setAcceptableValues(this.attributeId, this.filter, this.date1, this.date2);
	    popupPanel.setPopupPositionAndShow(new PopupPanel.PositionCallback() {
		    public void setPosition(int offsetWidth, int offsetHeight) {
			    int windowHeight = Window.getClientHeight();
			    int windowWidth = Window.getClientWidth();

			    int exceedOffsetX = text.getAbsoluteLeft();
			    int exceedOffsetY = text.getAbsoluteTop()+text.getOffsetHeight();
			    // Сдвигаем попап, если он не помещается в окно
			    if ((text.getAbsoluteLeft() + popupPanel.getOffsetWidth()) > windowWidth) {
				    exceedOffsetX -= popupPanel.getOffsetWidth();
			    }

			    if ((text.getAbsoluteTop() + popupPanel.getOffsetHeight()) > windowHeight) {
				    exceedOffsetY -= popupPanel.getOffsetHeight() + text.getOffsetHeight();
			    }
                if (!modal) {
                    popupPanel.setPopupPosition(exceedOffsetX, exceedOffsetY);
                } else {
                    popupPanel.center();
                }
		    }
	    });
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
        return attributeId;
    }
    
    public void setAttributeId(long attributeId) {
		this.attributeId = attributeId;
	}
    

    /**
     * Для совместимости с UiBinder
     * 
     * @param attributeId
     */
    public void setAttributeIdInt(int attributeId) {
		this.attributeId = Long.valueOf(attributeId);
	}

	public Date getDate1() {
		return date1;
	}

	public void setDate1(Date date1) {
		this.date1 = date1;
	}

	public Date getDate2() {
		return date2;
	}

	public void setDate2(Date date2) {
		this.date2 = date2;
	}

	public String getFilter() {
		return filter;
	}

	public void setFilter(String filter) {
		this.filter = filter;
	}

    @Override
    public void setTitle(String title) {
        if (popupPanel instanceof ClosableDialogBox) {
            ((ClosableDialogBox) popupPanel).setText(title);
        }
    }
}
