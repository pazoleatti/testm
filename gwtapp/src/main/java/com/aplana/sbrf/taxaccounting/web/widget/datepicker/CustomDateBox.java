package com.aplana.sbrf.taxaccounting.web.widget.datepicker;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.*;

import java.util.Date;

public class CustomDateBox extends Composite implements HasEnabled, HasVisibility, HasValue<Date>{
	interface DateBoxUiBinder extends UiBinder<Widget, CustomDateBox> {
	}

	private static final DateTimeFormat format = DateTimeFormat.getFormat("dd.MM.yyyy");
	private final PopupPanel datePickerPanel = new PopupPanel(true, true);
	private final DatePickerWithYearSelector datePicker = new DatePickerWithYearSelector();
	private static DateBoxUiBinder ourUiBinder = GWT.create(DateBoxUiBinder.class);
	private String lastValidDate;

	@UiField
	TextBox dateBox;

	@UiField
	Image dateImage;

	public CustomDateBox() {
		initWidget(ourUiBinder.createAndBindUi(this));

		datePickerPanel.setWidth("200");
		datePickerPanel.setHeight("200");
		datePickerPanel.add(datePicker);
		addDatePickerHandlers();
		addDateBoxHandlers();
	}

	@UiHandler("dateImage")
	public void onDateImage(ClickEvent event){
		datePickerPanel.setPopupPosition(event.getClientX(), event.getClientY() + 10);
		datePickerPanel.show();
	}

	private void addDatePickerHandlers() {
		datePicker.addValueChangeHandler(new ValueChangeHandler<Date>() {
			@Override
			public void onValueChange(ValueChangeEvent<Date> event) {
				dateBox.setValue(format.format(event.getValue()));
				lastValidDate = format.format(event.getValue());
				datePickerPanel.hide();
			}
		});
	}

	private void addDateBoxHandlers() {
		dateBox.addBlurHandler(new BlurHandler() {
			@Override
			public void onBlur(BlurEvent event) {
				try {
					Date formattedDate = format.parseStrict(dateBox.getValue());
					dateBox.setValue(format.format(formattedDate));
					lastValidDate = format.format(formattedDate);
				} catch (IllegalArgumentException e) {
					dateBox.setValue(lastValidDate);
				}
			}
		});
	}

	@Override
	public Date getValue() {
		return format.parse(dateBox.getValue());
	}

	@Override
	public void setValue(Date value) {
		lastValidDate = format.format(value);
		dateBox.setValue(format.format(value));
	}

	@Override
	public void setValue(Date value, boolean fireEvents) {
		dateBox.setValue(format.format(value), fireEvents);
	}

	@Override
	public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Date> handler) {
		return addHandler(handler, ValueChangeEvent.getType());
	}

	@Override
	public void fireEvent(GwtEvent<?> event) {
		dateBox.fireEvent(event);
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