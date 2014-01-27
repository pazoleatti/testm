package com.aplana.sbrf.taxaccounting.web.widget.datepicker;

import com.aplana.gwt.client.mask.ui.DateMaskBox;
import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.LeafValueEditor;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.*;

import java.util.Date;

/**
 * Компонент выбора дат с кнопкой календаря и с маской
 *
 * @author aivanov
 */
public class DateMaskBoxPicker extends Composite implements HasEnabled, HasVisibility, HasValue<Date>, LeafValueEditor<Date> {

    interface DateBoxUiBinder extends UiBinder<Widget, DateMaskBoxPicker> {
	}

	private final PopupPanel datePickerPanel = new PopupPanel(true, true);
	private final DatePickerWithYearSelector datePicker = new DatePickerWithYearSelector();
	private static DateBoxUiBinder ourUiBinder = GWT.create(DateBoxUiBinder.class);

    public static interface Icon extends ClientBundle {
        @Source("clear.png")
        ImageResource icon();
    }

	@UiField
    DateMaskBox dateBox;

	@UiField
	Image dateImage;

    @UiField
    Image clearImage;

    private boolean canBeEmpty = false;

	public DateMaskBoxPicker() {
		initWidget(ourUiBinder.createAndBindUi(this));

        dateBox.setMayBeNull(canBeEmpty);
        clearImage.setVisible(canBeEmpty);
        datePickerPanel.add(datePicker);
		addDatePickerHandlers();
        addDateBoxHandlers();
	}

    @UiHandler("dateImage")
    public void onDateImage(ClickEvent event) {
        datePickerPanel.setPopupPosition(event.getClientX(), event.getClientY() + 10);
        datePickerPanel.show();
    }

    @UiHandler("clearImage")
    public void onClearImage(ClickEvent event){
        setValue(null, true);
    }

    public void setWidth(String width){
        dateBox.setWidth(width);
    }

	private void addDatePickerHandlers() {
		datePicker.addValueChangeHandler(new ValueChangeHandler<Date>() {
			@Override
			public void onValueChange(ValueChangeEvent<Date> event) {
                DateMaskBoxPicker.this.setValue(event.getValue(), true);
				datePickerPanel.hide();
			}
		});
	}

    private void addDateBoxHandlers() {
        dateBox.addValueChangeHandler(new ValueChangeHandler<Date>() {
            @Override
            public void onValueChange(ValueChangeEvent<Date> event) {
                DateMaskBoxPicker.this.setValue(dateBox.getValue(), true);
            }
        });
    }

	@Override
	public Date getValue() {
		return dateBox.getValue();
	}

    public void setCanBeEmpty(boolean canBeEmpty) {
        this.canBeEmpty = canBeEmpty;
        dateBox.setMayBeNull(canBeEmpty);
    }

    public boolean isCanBeEmpty() {
        return canBeEmpty;
    }

	@Override
	public void setValue(Date value) {
        this.setValue(value, false);
	}

    @Override
	public void setValue(Date value, boolean fireEvents) {
        dateBox.setValue(value, false);
        clearImage.setVisible(value != null && canBeEmpty);
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