package com.aplana.sbrf.taxaccounting.web.widget.datepicker;

import com.aplana.gwt.client.mask.ui.DateMaskBox;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
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
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.datepicker.client.DatePicker;

import java.util.Date;

/**
 * Компонент выбора дат с кнопкой календаря и с маской
 *
 * @author aivanov
 */
public class DateMaskBoxPicker extends Composite implements HasEnabled, HasVisibility, HasValue<Date>, LeafValueEditor<Date> {

    interface DateBoxUiBinder extends UiBinder<Widget, DateMaskBoxPicker> {
	}

	private static DateBoxUiBinder ourUiBinder = GWT.create(DateBoxUiBinder.class);

    public static interface Icons extends ClientBundle {
        @Source("clear.png")
        public ImageResource clearBtn();

        @Source("clear_disable.png")
        public ImageResource clearBtnDisable();

        @Source("calendar_picker.png")
        public ImageResource calPickerBtn();

        @Source("calendar_picker_disable.png")
        public ImageResource calPickerBtnDisable();
    }

	@UiField
    DateMaskBox dateBox;

	@UiField
	Image calendarImage;

    @UiField
    Image clearImage;

    @UiField
    HTMLPanel mainPanel;

    Icons iconsRecources = GWT.create(Icons.class);

    protected static int POPUP_PANEL_WIDTH_CHECK = 160;
    protected static int POPUP_PANEL_HEIDHT_CHECK = 166;

    private final DatePickerWithYearSelector datePicker = new DatePickerWithYearSelector();
    private final PopupPanel datePickerPanel = new PopupPanel(true, true);
    private boolean widgetEnabled = true;
    private boolean canBeEmpty = false;

	public DateMaskBoxPicker() {
		initWidget(ourUiBinder.createAndBindUi(this));

        dateBox.setMayBeNull(canBeEmpty);
        clearImage.setVisible(canBeEmpty);
        datePickerPanel.add(datePicker);
		addDatePickerHandlers();
        addDateBoxHandlers();
	}

    @UiHandler("calendarImage")
    public void onDateImage(ClickEvent event) {
        if (widgetEnabled) {
            //Window.alert("Window.getClientWidth() = "+String.valueOf(Window.getClientWidth()) + "; event.getClientX() = " + String.valueOf(event.getClientX())+ "; datePickerPanel.getOffsetWidth() = " + String.valueOf(datePickerPanel.getOffsetWidth()));

            // Проверяем помещаеться ли календарь в окно, если нет корректируем координаты
            int leftPosition = 0;
            int topPosition = 0;
            if (Window.getClientWidth() - (event.getClientX() + POPUP_PANEL_WIDTH_CHECK) < 0){
                leftPosition = Window.getClientWidth()-POPUP_PANEL_WIDTH_CHECK - 10;
            }
            else{
                leftPosition = event.getClientX();
            }
            if (Window.getClientHeight() - (event.getClientY() + POPUP_PANEL_HEIDHT_CHECK + 10) < 0){
                topPosition = Window.getClientHeight() - POPUP_PANEL_HEIDHT_CHECK - 10;
            }
            else{
                topPosition = event.getClientY() + 10;
            }

            datePickerPanel.setPopupPosition(leftPosition, topPosition);
            datePickerPanel.show();
        }
    }

    @UiHandler("clearImage")
    public void onClearImage(ClickEvent event) {
        if (widgetEnabled) {
            setValue(null, true);
        }
    }

    public DatePicker getDatePicker() {
        return datePicker;
    }

    public void setWidth(String width){
        mainPanel.setWidth(width);
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
		return widgetEnabled;
	}

	@Override
	public void setEnabled(boolean enabled) {
        this.widgetEnabled = enabled;
		dateBox.setEnabled(enabled);
        calendarImage.setResource(enabled ? iconsRecources.calPickerBtn() : iconsRecources.calPickerBtnDisable());
        calendarImage.getElement().getStyle().setCursor(enabled ? Style.Cursor.POINTER : Style.Cursor.DEFAULT);
        clearImage.setVisible(getValue()!= null && enabled && canBeEmpty);
        clearImage.setResource(enabled ? iconsRecources.clearBtn() : iconsRecources.clearBtnDisable());
        clearImage.getElement().getStyle().setCursor(enabled ? Style.Cursor.POINTER : Style.Cursor.DEFAULT);
	}

	@Override
	public boolean isVisible() {
		return mainPanel.isVisible();
	}

	@Override
	public void setVisible(boolean visible) {
        mainPanel.setVisible(visible);
	}
}