package com.aplana.sbrf.taxaccounting.web.widget.datepicker;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.datepicker.client.DatePicker;

import java.util.Date;

public class CustomDateBox extends Composite implements HasEnabled, HasVisibility, HasValue<Date>{

    interface DateBoxUiBinder extends UiBinder<Widget, CustomDateBox> {
	}

	private static final DateTimeFormat format = DateTimeFormat.getFormat("dd.MM.yyyy");
	private final PopupPanel datePickerPanel = new PopupPanel(true, true);
	private final DatePickerWithYearSelector datePicker = new DatePickerWithYearSelector();
    private final Button clearButton = new Button();
	private static DateBoxUiBinder ourUiBinder = GWT.create(DateBoxUiBinder.class);
	private String lastValidDate;

	@UiField
	TextBox dateBox;

	@UiField
	Image dateImage;

    private Date value;
    private boolean canBeEmpty = false;

	public CustomDateBox() {
		initWidget(ourUiBinder.createAndBindUi(this));
        VerticalPanel vPanel = new VerticalPanel();

//      (aivanov) 8.1.14. Убрал потому как в ие размеры не уменьшались, если что то не то сделал - сообщите
//		datePickerPanel.setWidth("200");
//		datePickerPanel.setHeight("200");
        vPanel.add(datePicker);

        clearButton.setText("Очистить");
        clearButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                value = null;
                dateBox.setValue(null);
                datePickerPanel.hide();
            }
        });
        clearButton.setVisible(canBeEmpty);
        vPanel.add(clearButton);
        datePickerPanel.add(vPanel);
		addDatePickerHandlers();
		addDateBoxHandlers();
	}

	@UiHandler("dateImage")
	public void onDateImage(ClickEvent event){
		datePickerPanel.setPopupPosition(event.getClientX(), event.getClientY() + 10);
		datePickerPanel.show();
	}

    public DatePicker getDatePicker() {
        return datePicker;
    }

	private void addDatePickerHandlers() {
		datePicker.addValueChangeHandler(new ValueChangeHandler<Date>() {
			@Override
			public void onValueChange(ValueChangeEvent<Date> event) {
                CustomDateBox.this.setValue(event.getValue(), true);
				datePickerPanel.hide();
			}
		});
	}

	private void addDateBoxHandlers() {
		dateBox.addBlurHandler(new BlurHandler() {
			@Override
			public void onBlur(BlurEvent event) {
				try {
					if (dateBox.getValue().isEmpty()) {
						dateBox.setValue("");
						lastValidDate = "";
					} else {
						Date formattedDate = format.parseStrict(dateBox.getValue());
						dateBox.setValue(format.format(formattedDate));
						lastValidDate = format.format(formattedDate);
					}
				} catch (IllegalArgumentException e) {
					dateBox.setValue(lastValidDate);
				}
			}
		});
	}

	@Override
	public Date getValue() {
		return value;
	}

	@Override
	public void setValue(Date value) {
        this.setValue(value, false);
	}

    public void setCanBeEmpty(boolean canBeEmpty) {
        this.canBeEmpty = canBeEmpty;
        clearButton.setVisible(canBeEmpty);
    }

	@Override
	public void setValue(Date value, boolean fireEvents) {
        if (value != null) {
            lastValidDate = format.format(value);
            dateBox.setValue(format.format(value), fireEvents);
        } else {
            dateBox.setValue(null, fireEvents);
        }
        this.value = value;
        if (fireEvents) {
            ValueChangeEvent.fire(this, value);
        }
	}

	@Override
	public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Date> handler) {
		return addHandler(handler, ValueChangeEvent.getType());
	}

	@Override
	public boolean isEnabled() {
		return dateBox.isEnabled();
	}

	@Override
	public void setEnabled(boolean enabled) {
		dateBox.setEnabled(enabled);
	}

	@Override
	public boolean isVisible() {
		return dateImage.isVisible();
	}

	@Override
	public void setVisible(boolean visible) {
		dateImage.setVisible(visible);
	}
}