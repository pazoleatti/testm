package com.aplana.sbrf.taxaccounting.web.widget.datepicker;

import com.aplana.gwt.client.mask.ui.DateMaskBox;
import com.aplana.sbrf.taxaccounting.web.widget.utils.WidgetUtils;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.editor.client.LeafValueEditor;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.event.logical.shared.ShowRangeEvent;
import com.google.gwt.event.logical.shared.ShowRangeHandler;
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
import com.google.gwt.user.datepicker.client.CalendarUtil;

import java.util.Date;

import static com.aplana.sbrf.taxaccounting.web.widget.utils.WidgetUtils.*;

/**
 * Компонент выбора дат с кнопкой календаря и с маской
 *
 * @author aivanov
 */
public class DateMaskBoxPicker extends Composite implements HasEnabled, HasVisibility, HasValue<Date>, LeafValueEditor<Date> {

    interface DateBoxUiBinder extends UiBinder<Widget, DateMaskBoxPicker> {
    }

    private static DateBoxUiBinder ourUiBinder = GWT.create(DateBoxUiBinder.class);

    @UiField
    DateMaskBox dateBox;
    @UiField
    Image calendarImage,
            clearImage;
    @UiField
    HorizontalPanel mainPanel;

    protected static int POPUP_PANEL_WIDTH_CHECK = 160;
    protected static int POPUP_PANEL_HEIDHT_CHECK = 166;

    private final DatePickerWithYearSelector datePicker = new DatePickerWithYearSelector();
    private final PopupPanel datePickerPanel = new PopupPanel(true, true);
    private boolean widgetEnabled = true;
    private boolean canBeEmpty = false;

    private Date prevValue;

    /* Даты ограничивающего периода */
    private Date startLimitDate;
    private Date endLimitDate;

    public DateMaskBoxPicker() {
        initWidget(ourUiBinder.createAndBindUi(this));

        MouseOverHandler mouseOverHandler = new MouseOverHandler() {
            @Override
            public void onMouseOver(MouseOverEvent event) {
                if (canBeEmpty && dateBox.getValue()!= null) {
                    clearImage.setUrl(WidgetUtils.iconUrl);
                    clearImage.setTitle("Очистить выбор");
                    setPointerCursor(clearImage.getElement(), true);
                }
            }
        };
        MouseOutHandler mouseOutHandler = new MouseOutHandler() {
            @Override
            public void onMouseOut(MouseOutEvent event) {
                if (event.getRelatedTarget()!= null && !event.getRelatedTarget().equals(
                        clearImage.getElement())) {
                    clearImage.setUrl(WidgetUtils.dummyUrl);
                    clearImage.setTitle("");
                    setPointerCursor(clearImage.getElement(), false);
                }
            }
        };

        dateBox.addMouseOverHandler(mouseOverHandler);
        dateBox.addMouseOutHandler(mouseOutHandler);
        calendarImage.addMouseOverHandler(mouseOverHandler);
        calendarImage.addMouseOutHandler(mouseOutHandler);
        clearImage.addMouseOverHandler(mouseOverHandler);
        clearImage.addMouseOutHandler(new MouseOutHandler() {
            @Override
            public void onMouseOut(MouseOutEvent event) {
                if (event.getRelatedTarget()!= null && !event.getRelatedTarget().equals(dateBox.getElement()) &&
                        !event.getRelatedTarget().equals(calendarImage.getElement())) {
                    clearImage.setUrl(WidgetUtils.dummyUrl);
                    clearImage.setTitle("");
                    setPointerCursor(clearImage.getElement(), false);
                }
            }
        });

        dateBox.setMayBeNull(canBeEmpty);
        clearImage.setVisible(canBeEmpty);
        datePickerPanel.add(datePicker);
        addDatePickerHandlers();
        addDateBoxHandlers();
    }

    /**
     * Конструктор с указанием дат ограничивающего перпиода
     *
     * @param startLimitDate начало периода, может быть null
     * @param endLimitDate   конец периода, может быть null
     */
    public DateMaskBoxPicker(Date startLimitDate, Date endLimitDate) {
        this();
        this.startLimitDate = startLimitDate;
        this.endLimitDate = endLimitDate;
    }

    @UiHandler("calendarImage")
    public void onDateImage(ClickEvent event) {
        if (widgetEnabled) {
            // Проверяем помещаеться ли календарь в окно, если нет корректируем координаты
            int leftPosition = 0;
            int topPosition = 0;
            if (Window.getClientWidth() - (event.getClientX() + POPUP_PANEL_WIDTH_CHECK) < 0) {
                leftPosition = Window.getClientWidth() - POPUP_PANEL_WIDTH_CHECK - 10;
            } else {
                leftPosition = event.getClientX();
            }
            if (Window.getClientHeight() - (event.getClientY() + POPUP_PANEL_HEIDHT_CHECK + 10) < 0) {
                topPosition = Window.getClientHeight() - POPUP_PANEL_HEIDHT_CHECK - 10;
            } else {
                topPosition = event.getClientY() + 10;
            }

            if (dateBox.getValue() != null) {
                datePicker.setCurrentMonth(dateBox.getValue());
                datePicker.setValue(dateBox.getValue());
            } else {
                if (startLimitDate != null) {
                    datePicker.setCurrentMonth(startLimitDate);
                    datePicker.setValue(startLimitDate);
                }
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

    // TODO зачем кишки внаружу?
//    public DateMaskBox getDateBox() {
//        return dateBox;
//    }

    private void addDatePickerHandlers() {
        datePicker.addValueChangeHandler(new ValueChangeHandler<Date>() {
            @Override
            public void onValueChange(ValueChangeEvent<Date> event) {
                DateMaskBoxPicker.this.setValue(event.getValue(), true);
                datePickerPanel.hide();
            }
        });

        datePicker.addShowRangeHandler(new ShowRangeHandler<Date>() {
            @Override
            public void onShowRange(final ShowRangeEvent<Date> event) {
                if (startLimitDate != null || endLimitDate != null) {
                    Date start = getDateWithOutTime(event.getStart());
                    Date end = getDateWithOutTime(event.getEnd());
                    while (start.getTime() <= end.getTime()) {      // сравнивается так потому что в GWT свой порядок переключаения календарей в зависимости от значнения даты
                        if (!isInLimitPeriod(startLimitDate, endLimitDate, start)) {
                            datePicker.setTransientEnabledOnDates(false, start);
                        }
                        CalendarUtil.addDaysToDate(start, 1);
                    }
                }
            }
        });
    }

    private void addDateBoxHandlers() {
        dateBox.addValueChangeHandler(new ValueChangeHandler<Date>() {
            @Override
            public void onValueChange(ValueChangeEvent<Date> event) {
                DateMaskBoxPicker.this.setValue(event.getValue(), true);
            }
        });
        dateBox.addKeyPressHandler(new KeyPressHandler() {
            @Override
            public void onKeyPress(KeyPressEvent event) {
                if (event.getUnicodeCharCode() == KeyCodes.KEY_ENTER) {
                    dateBox.trySetValue();
                }
            }
        });
    }

    @Override
    public Date getValue() {
        return dateBox.getValue();
    }

    @Override
    public void setValue(Date value) {
        this.setValue(value, false);
    }

    @Override
    public void setValue(Date value, boolean fireEvents) {
        if (isDateWasChange(prevValue, value)) {
            if (isInLimitPeriod(startLimitDate, endLimitDate, value)) {
                setCheckedValue(value, fireEvents);
            } else {
                if (value == null) {
                    setCheckedValue(canBeEmpty ? null : prevValue, fireEvents);
                } else {
                    if (startLimitDate != null && compareDates(startLimitDate, value) == 1) {
                        setCheckedValue(startLimitDate, fireEvents);
                    } else if (endLimitDate != null && compareDates(endLimitDate, value) == -1) {
                        setCheckedValue(endLimitDate, fireEvents);
                    }
                }
            }
        }
    }

    private void setCheckedValue(Date value, boolean fireEvents) {
        dateBox.setValue(value, false);
        prevValue = value;
        clearImage.setVisible(value != null && canBeEmpty);
        if (fireEvents) {
            ValueChangeEvent.fire(this, value);
        }
    }

    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Date> handler) {
        return addHandler(handler, ValueChangeEvent.getType());
    }

    public Date getEndLimitDate() {
        return endLimitDate;
    }

    public void setEndLimitDate(Date endLimitDate) {
        this.endLimitDate = endLimitDate;
    }

    public Date getStartLimitDate() {
        return startLimitDate;
    }

    public void setStartLimitDate(Date startLimitDate) {
        this.startLimitDate = startLimitDate;
    }

    public void setLimitDates(Date startLimitDate, Date endLimitDate) {
        this.startLimitDate = startLimitDate;
        this.endLimitDate = endLimitDate;
    }

    public void setCanBeEmpty(boolean canBeEmpty) {
        this.canBeEmpty = canBeEmpty;
        dateBox.setMayBeNull(canBeEmpty);
    }

    public boolean isCanBeEmpty() {
        return canBeEmpty;
    }

    @Override
    public void setWidth(String width) {
        mainPanel.setWidth(width);
    }

    @Override
    public boolean isEnabled() {
        return widgetEnabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.widgetEnabled = enabled;
        dateBox.setEnabled(enabled);
        calendarImage.setUrl(enabled ? "resources/img/picker/calendar-icon.png" : "resources/img/picker/calendar-icon-disable.png");
        calendarImage.getElement().getStyle().setCursor(enabled ? Style.Cursor.POINTER : Style.Cursor.DEFAULT);
        clearImage.setVisible(false);
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