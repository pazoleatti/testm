package com.aplana.sbrf.taxaccounting.web.widget.cell;

import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.web.widget.datepicker.DatePickerWithYearSelector;
import com.google.gwt.cell.client.AbstractEditableCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style;
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
import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.datepicker.client.DateBox;

import java.util.Date;

import static com.google.gwt.dom.client.BrowserEvents.CLICK;
import static com.google.gwt.dom.client.BrowserEvents.KEYDOWN;

/**
 * Класс больше не исользуется, вместо него {@link DateInputCell}
 */
@Deprecated
public class NullableDatePickerCell extends AbstractEditableCell<Date, Date> {

	private static final int ESCAPE = 27;
	private static final String CLEAR_IMAGE_SOURCE = "resources/img/cancel_image.png";
	private static final String CLEAR_IMAGE_WIDTH = "20px";
	private static final String CLEAR_IMAGE_HEIGHT = "20px";
	private static final String CLEAR_IMAGE_BACKGROUND = "#FFFBB1";
	private static final String CLEAR_IMAGE_TITLE = "Очистить значение";
	private static final String ROOT_POPUP_PANEL_HEIGHT = "188px";
	private static final String ROOT_POPUP_PANEL_COLOR = "green";
	private static final String DATE_BOX_WIDTH = "121px";

	private PopupPanel rootPopupPanel;
	private final HorizontalPanel datePickerAndImageComposite;
	private final DateBox dateBox;
	private final Image clearDateImage;

	private final DateTimeFormat format;
	private final SafeHtmlRenderer<String> renderer;
	private ValueUpdater<Date> valueUpdater;

	private static int firstEditedColumn = -1;
	private static int firstEditedIndex = -1;
	private static boolean isValueInCellWasChanged = false;

	private Element lastParent;
	private Object lastKey;
	private int lastIndex;
	private int lastColumn;
	private Date lastValue;

	ColumnContext columnContext;

	public NullableDatePickerCell(DateTimeFormat format, ColumnContext columnContext) {
		this(format, SimpleSafeHtmlRenderer.getInstance());
		this.columnContext = columnContext;
	}

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

		DatePickerWithYearSelector pickerForDateBox = new DatePickerWithYearSelector();
		this.dateBox = new DateBox(pickerForDateBox, null, new DateBox.DefaultFormat(format));
		dateBox.setWidth(DATE_BOX_WIDTH);
		this.dateBox.addValueChangeHandler(new ValueChangeHandler<Date>() {
			public void onValueChange(ValueChangeEvent<Date> event) {
				// Запомнить значения перед тем как скрыть Popup панель
				isValueInCellWasChanged = true;
				Element cellParent = lastParent;
				Date oldValue = lastValue;
				Object key = lastKey;
				int index = lastIndex;
				int column = lastColumn;
				rootPopupPanel.hide();
				// Обновить ячейку и ValueUpdater
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

		this.rootPopupPanel = new PopupPanel(true, false) {
			@Override
			protected void onPreviewNativeEvent(Event.NativePreviewEvent event) {
				if (Event.ONKEYUP == event.getTypeInt()) {
					if (event.getNativeEvent().getKeyCode() == ESCAPE) {
						// Dismiss when escape is pressed
						dateBox.hideDatePicker();
						rootPopupPanel.hide();
					}
				}
			}
		};

		rootPopupPanel.getElement().getStyle().setBorderStyle(Style.BorderStyle.NONE);
		rootPopupPanel.addCloseHandler(new CloseHandler<PopupPanel>() {
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

		clearDateImage = new Image();
		clearDateImage.setUrl(CLEAR_IMAGE_SOURCE);
		clearDateImage.setWidth(CLEAR_IMAGE_WIDTH);
		clearDateImage.setHeight(CLEAR_IMAGE_HEIGHT);
		clearDateImage.setTitle(CLEAR_IMAGE_TITLE);
		clearDateImage.getElement().getStyle().setVerticalAlign(Style.VerticalAlign.MIDDLE);

		clearDateImage.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				ValueChangeEvent.fire(dateBox, null);

			}
		});
		clearDateImage.getElement().getStyle().setBackgroundColor(CLEAR_IMAGE_BACKGROUND);

		datePickerAndImageComposite = new HorizontalPanel();
		datePickerAndImageComposite.add(dateBox);
		datePickerAndImageComposite.add(clearDateImage);
		datePickerAndImageComposite.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		rootPopupPanel.setWidth(datePickerAndImageComposite.getElement().getStyle().getWidth());
		rootPopupPanel.setHeight(ROOT_POPUP_PANEL_HEIGHT);
		rootPopupPanel.getElement().getStyle().setBackgroundColor(ROOT_POPUP_PANEL_COLOR);
		rootPopupPanel.add(datePickerAndImageComposite);
	}

	@Override
	public boolean isEditing(Context context, Element parent, Date value) {
		return lastKey != null && lastKey.equals(context.getKey());
	}

	@Override
	public void onBrowserEvent(Context context, Element parent, Date value,
							   NativeEvent event, ValueUpdater<Date> valueUpdater) {
		DataRow<Cell> dataRow = (DataRow<Cell>)context.getKey();
		if ((columnContext.getMode() == ColumnContext.Mode.EDIT_MODE)
				|| ((columnContext.getMode() != ColumnContext.Mode.READONLY_MODE)
				&& dataRow.getCell(columnContext.getColumn().getAlias()).isEditable())) {
			super.onBrowserEvent(context, parent, value, event, valueUpdater);
			if (CLICK.equals(event.getType())) {
				onEnterKeyDown(context, parent, value, event, valueUpdater);
			}
		}
	}

	@Override
	public void render(Context context, Date value, SafeHtmlBuilder sb) {
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
		dateBox.setValue(date);

		rootPopupPanel.setPopupPositionAndShow(new PopupPanel.PositionCallback() {
			public void setPosition(int offsetWidth, int offsetHeight) {
				rootPopupPanel.setPopupPosition(lastParent.getAbsoluteLeft() - 3,
						lastParent.getAbsoluteTop() - 3);
				rootPopupPanel.show();
				dateBox.showDatePicker();
			}

		});
	}


}
