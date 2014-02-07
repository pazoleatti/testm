package com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.client;

import java.util.Date;
import java.util.List;

import com.aplana.gwt.client.DoubleStateComposite;
import com.aplana.gwt.client.ModalWindow;
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
public class RefBookMultiPickerModalWidget extends DoubleStateComposite implements RefBookMultiPickerModal {

    interface Binder extends UiBinder<Widget, RefBookMultiPickerModalWidget> {
    }

	private Long attributeId;

	private Date startDate;
    private Date endDate;
    private String filter;

    private static Binder binder = GWT.create(Binder.class);

    private PopupPanel popupPanel;

    private RefBookMultiPicker refBookPiker;

    @UiField
    TextBox text;

    @UiField
    Image selectButton;

    /** Признак модальности окна */
    private boolean modal;

    @UiConstructor
    public RefBookMultiPickerModalWidget(boolean modal, boolean multiSelect) {
        initWidget(binder.createAndBindUi(this));
        this.modal = modal;
        if (modal) {
            popupPanel = new ModalWindow();
        } else {
            popupPanel = new PopupPanel(true, true);
        }
        refBookPiker = new RefBookMultiPickerView(multiSelect);
        popupPanel.add(refBookPiker);
        refBookPiker.addValueChangeHandler(new ValueChangeHandler<List<Long>>() {
            @Override
            public void onValueChange(ValueChangeEvent<List<Long>> event) {
                text.setText(refBookPiker.getDereferenceValue());
                text.setTitle(refBookPiker.getDereferenceValue());
                setValue(event.getValue(), true);
                popupPanel.hide();
            }
        });
    }

    @UiHandler("selectButton")
    void onSelectButtonClicked(ClickEvent event){
        refBookPiker.setAcceptableValues(this.attributeId, this.filter, this.startDate, this.endDate);
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
    public List<Long> getValue() {
        return refBookPiker.getValue();
    }

    @Override
    public void setValue(List<Long> value) {
        refBookPiker.setValue(value);
    }

    @Override
    public void setValue(List<Long> value, boolean fireEvents) {
        refBookPiker.setValue(value);
        if (fireEvents){
            ValueChangeEvent.fire(this, value);
        }
    }

    @Override
    public Long getSingleValue() {
        return refBookPiker.getSingleValue();
    }

    @Override
    public void setValue(Long value) {
        refBookPiker.setValue(value);
    }

    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<List<Long>> handler) {
        return refBookPiker.addValueChangeHandler(handler);
    }

	@Override
	public String getDereferenceValue() {
		return text.getValue();
	}

	@Override
	public void setDereferenceValue(String value) {
		text.setValue(value);
		setLabelValue(value);
	}

    /**
     * @return Id отображаемого атрибута
     */
    public Long getAttributeId() {
        return attributeId;
    }
    
    public void setAttributeId(long attributeId) {
		this.attributeId = attributeId;
	}

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    /**
     * Для совместимости с UiBinder
     * 
     * @param attributeId
     */
    public void setAttributeIdInt(int attributeId) {
		this.attributeId = Long.valueOf(attributeId);
	}

	public String getFilter() {
		return filter;
	}

	public void setFilter(String filter) {
		this.filter = filter;
	}

    public void setPeriodDates(Date startDate, Date endDate){
        this.startDate = startDate;
        this.endDate = endDate;
    }

    @Override
    public void setTitle(String title) {
        if (popupPanel instanceof ModalWindow) {
            ((ModalWindow) popupPanel).setText(title);
        }
    }

    @Override
    protected void setLabelValue(Object value) {
        String stringValue;
        if (value == null || (value instanceof List && ((List) value).isEmpty())) {
            stringValue = EMPTY_STRING_VALUE;
        } else {
            stringValue = value.toString();
            if (stringValue.trim().isEmpty()) {
                stringValue = EMPTY_STRING_VALUE;
            }
        }
        label.setText(stringValue);
        if (stringValue.equals(EMPTY_STRING_VALUE))
            label.setTitle(EMPTY_STRING_TITLE);
        else
            label.setTitle(stringValue);
    }
}
