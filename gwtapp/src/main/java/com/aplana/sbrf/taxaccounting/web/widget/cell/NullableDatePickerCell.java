package com.aplana.sbrf.taxaccounting.web.widget.cell;

import com.google.gwt.cell.client.AbstractEditableCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.text.shared.SafeHtmlRenderer;
import com.google.gwt.text.shared.SimpleSafeHtmlRenderer;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

import java.util.Date;

import static com.google.gwt.dom.client.BrowserEvents.CLICK;
import static com.google.gwt.dom.client.BrowserEvents.KEYDOWN;

public class NullableDatePickerCell extends AbstractEditableCell<Date, Date> {

	private static final String BUTTON_WIDTH = "140px";  //8.313em
	private static final String BUTTON_HEIGHT = "25px";  //1.563em
	private static final String BUTTON_CAPTION = "Очистить";

	private static final int ESCAPE = 27;
	private final DatePickerWithYearSelector datePicker;
	private final DateTimeFormat format;
	private int offsetX = 10;
	private int offsetY = 10;
	private Object lastKey;
	private Element lastParent;
	private int lastIndex;
	private int lastColumn;
	private Date lastValue;
	private PopupPanel panel;
	private Button clearDateButton;
	private VerticalPanel datePickerAndButtonComposite = new VerticalPanel();
	private final SafeHtmlRenderer<String> renderer;
	private ValueUpdater<Date> valueUpdater;

	private static int firstEditedColumn = -1;
	private static int firstEditedIndex = -1;
	private static boolean isValueInCellWasChanged = false;

	/**
	 * Constructs a new DatePickerCell that uses the date/time format given by
	 * {@link DateTimeFormat#getFullDateFormat}.
	 */
	@SuppressWarnings("deprecation")
	public NullableDatePickerCell() {
		this(DateTimeFormat.getFullDateFormat(),
				SimpleSafeHtmlRenderer.getInstance());
	}

	/**
	 * Constructs a new DatePickerCell that uses the given date/time format and a
	 * {@link SimpleSafeHtmlRenderer}.
	 *
	 * @param format a {@link DateTimeFormat} instance
	 */
	public NullableDatePickerCell(DateTimeFormat format) {
		this(format, SimpleSafeHtmlRenderer.getInstance());
	}

	/**
	 * Constructs a new DatePickerCell that uses the date/time format given by
	 * {@link DateTimeFormat#getFullDateFormat} and the given
	 * {@link SafeHtmlRenderer}.
	 *
	 * @param renderer a {@link SafeHtmlRenderer SafeHtmlRenderer<String>} instance
	 */
	public NullableDatePickerCell(SafeHtmlRenderer<String> renderer) {
		this(DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.DATE_FULL), renderer);
	}

	/**
	 * Constructs a new DatePickerCell that uses the given date/time format and
	 * {@link SafeHtmlRenderer}.
	 *
	 * @param format a {@link DateTimeFormat} instance
	 * @param renderer a {@link SafeHtmlRenderer SafeHtmlRenderer<String>} instance
	 */
	public NullableDatePickerCell(DateTimeFormat format, SafeHtmlRenderer<String> renderer) {
		super(CLICK, KEYDOWN);
		if (format == null) {
			throw new IllegalArgumentException("format == null");
		}
		if (renderer == null) {
			throw new IllegalArgumentException("renderer == null");
		}
		this.format = format;
		this.renderer = renderer;

		this.datePicker = new DatePickerWithYearSelector();
		this.panel = new PopupPanel(true, false) {
			@Override
			protected void onPreviewNativeEvent(Event.NativePreviewEvent event) {
				if (Event.ONKEYUP == event.getTypeInt()) {
					if (event.getNativeEvent().getKeyCode() == ESCAPE) {
						// Dismiss when escape is pressed
						panel.hide();
					}
				}
			}
		};
		panel.addCloseHandler(new CloseHandler<PopupPanel>() {
			public void onClose(CloseEvent<PopupPanel> event) {
				lastKey = null;
				lastValue = null;
				lastIndex = -1;
				lastColumn = -1;
				if (lastParent != null && !event.isAutoClosed()) {
					// Refocus on the containing cell after the user selects a value, but
					// not if the popup is auto closed.
					lastParent.focus();
				}
				lastParent = null;
			}
		});

		this.clearDateButton = new Button();

		clearDateButton.setWidth(BUTTON_WIDTH);
		clearDateButton.setHeight(BUTTON_HEIGHT);
		clearDateButton.setText(BUTTON_CAPTION);

		clearDateButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				ValueChangeEvent.fire(datePicker, null);
			}

		});

		datePickerAndButtonComposite.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		datePickerAndButtonComposite.add(datePicker);
		datePickerAndButtonComposite.add(clearDateButton);
		panel.add(datePickerAndButtonComposite);


		// Hide the panel and call valueUpdater.update when a date is selected
		datePicker.addValueChangeHandler(new ValueChangeHandler<Date>() {
			public void onValueChange(ValueChangeEvent<Date> event) {
				// Remember the values before hiding the popup.
				isValueInCellWasChanged = true;
				Element cellParent = lastParent;
				Date oldValue = lastValue;
				Object key = lastKey;
				int index = lastIndex;
				int column = lastColumn;
				panel.hide();
				// Update the cell and value updater.
				Date date = event.getValue();
				setViewData(key, date);

				if(date != null){
					setValue(new Context(index, column, key), cellParent, oldValue);
				} else {
					setValue(new Context(index, column, key), cellParent, date);
				}

				if (valueUpdater != null) {
					valueUpdater.update(date);
				}
			}
		});
	}

	@Override
	public boolean isEditing(Context context, Element parent, Date value) {
		return lastKey != null && lastKey.equals(context.getKey());
	}

	@Override
	public void onBrowserEvent(Context context, Element parent, Date value,
							   NativeEvent event, ValueUpdater<Date> valueUpdater) {
		super.onBrowserEvent(context, parent, value, event, valueUpdater);
		if (CLICK.equals(event.getType())) {
			onEnterKeyDown(context, parent, value, event, valueUpdater);
		}
	}

	@Override
	public void render(Context context, Date value, SafeHtmlBuilder sb) {
		// Get the view data.
		Object key = context.getKey();
		Date viewData = getViewData(key);

		if (viewData != null && viewData.equals(value)) {
			clearViewData(key);
			viewData = null;
		}

		String s = null;
		if (viewData != null) {
			s = format.format(viewData);
		} else if (value != null) {
			s = format.format(value);
		}
		if (s != null) {
			sb.append(renderer.render(s));
		} else {
			sb.append(renderer.render(null));
		}

		if (value == null) {
			// Если значение в ячейке равно null, то стандартная реализация не рендерит ничего
			// Из-за этого обрамляющий div получается нулевой длины и нажать на него невозможно.
			// Для того, чтобы исправить ситуацию, в div выводим nonbreaking-пробел
			// nbsp win(alt+255)
			sb.appendHtmlConstant(" ");
		}
	}

	@Override
	protected void onEnterKeyDown(Context context, Element parent, Date value,
								  NativeEvent event, ValueUpdater<Date> valueUpdater) {
		/* Исправление ошибки (SBRFACCTAX-627 Проблема с DatePickerCell)
		* На данный момент это больше как work around, пока не найдена главная причина проблемы.
		* TODO: разобраться, почему при первом вызове любой функции у lastParent решает данную проблему и придумать
		* наиболее 'красивый' способ решения.*/
		if(!isValueInCellWasChanged && (!(firstEditedColumn == context.getColumn())
				|| !(firstEditedIndex == context.getIndex()))){
			firstEditedColumn = context.getColumn();
			firstEditedIndex = context.getIndex();
			lastParent.getAbsoluteLeft();
		}

		value = value == null ? new Date() : value;
		this.lastKey = context.getKey();
		this.lastParent = parent;
		this.lastValue = value;
		this.lastIndex = context.getIndex();
		this.lastColumn = context.getColumn();
		this.valueUpdater = valueUpdater;

		Date viewData = getViewData(lastKey);
		Date date = (viewData == null) ? lastValue : viewData;
		datePicker.setCurrentMonth(date);
		datePicker.setValue(date);
		panel.setPopupPositionAndShow(new PopupPanel.PositionCallback() {
			public void setPosition(int offsetWidth, int offsetHeight) {
				int windowHeight= Window.getClientHeight();
				int windowWidth=Window.getClientWidth();

				int exceedOffsetX = offsetX;
				int exceedOffsetY = offsetY;

				// Сдвигаем попап, если он не помещается в окно
				if ((lastParent.getAbsoluteRight() + panel.getOffsetWidth()) > windowWidth) {

					exceedOffsetX -= panel.getOffsetWidth();
				}

				if ((lastParent.getAbsoluteTop() + panel.getOffsetHeight()) > windowHeight) {
					exceedOffsetY -= panel.getOffsetHeight();
				}
				panel.setPopupPosition(lastParent.getAbsoluteLeft() + exceedOffsetX,
						lastParent.getAbsoluteTop() + exceedOffsetY);
			}
		});
	}


}
