package com.aplana.sbrf.taxaccounting.web.widget.cell;


import com.google.gwt.cell.client.*;
import com.google.gwt.core.client.*;
import com.google.gwt.dom.client.*;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.event.logical.shared.*;
import com.google.gwt.i18n.client.*;
import com.google.gwt.safehtml.client.*;
import com.google.gwt.safehtml.shared.*;
import com.google.gwt.text.shared.*;
import com.google.gwt.user.client.ui.*;

import java.util.*;

import static com.google.gwt.dom.client.BrowserEvents.*;

public class DateInputCell extends
		AbstractEditableCell<Date, DateInputCell.ViewData> {

	private final PopupPanel datePickerPanel = new PopupPanel(true, true);
	private final DatePickerWithYearSelector datePicker = new DatePickerWithYearSelector();
	private ValueUpdater<Date> valueUpdater;
	private Context context;
	private Element parent;
	private ViewData viewData;

	interface Template extends SafeHtmlTemplates {
		@Template("<input type=\"text\" value=\"{0}\" tabindex=\"-1\"></input>")
		SafeHtml input(String value);

		@Template("<img imageTypeCalendar=\"calendar\" align=\"right\" src=\"img/calendar_icon.png\"/>")
		SafeHtml calendarIcon();
	}

	/**
	 * The view data object used by this cell. We need to store both the text and
	 * the state because this cell is rendered differently in edit mode. If we did
	 * not store the edit state, refreshing the cell with view data would always
	 * put us in to edit state, rendering a text box instead of the new text
	 * string.
	 */
	static class ViewData {

		private boolean isEditing;

		/**
		 * If true, this is not the first edit.
		 */
		private boolean isEditingAgain;

		/**
		 * Keep track of the original value at the start of the edit, which might be
		 * the edited value from the previous edit and NOT the actual value.
		 */
		private Date original;

		private Date text;

		/**
		 * Construct a new ViewData in editing mode.
		 *
		 * @param text the text to edit
		 */
		public ViewData(Date text) {
			this.original = text;
			this.text = text;
			this.isEditing = true;
			this.isEditingAgain = false;
		}

		@Override
		public boolean equals(Object o) {
			if (o == null) {
				return false;
			}
			ViewData vd = (ViewData) o;
			return equalsOrBothNull(original, vd.original)
					&& equalsOrBothNull(text, vd.text) && isEditing == vd.isEditing
					&& isEditingAgain == vd.isEditingAgain;
		}

		public Date getOriginal() {
			return original;
		}

		public Date getText() {
			return text;
		}

		@Override
		public int hashCode() {
			return original.hashCode() + text.hashCode()
					+ Boolean.valueOf(isEditing).hashCode() * 29
					+ Boolean.valueOf(isEditingAgain).hashCode();
		}

		public boolean isEditing() {
			return isEditing;
		}

		public boolean isEditingAgain() {
			return isEditingAgain;
		}

		public void setEditing(boolean isEditing) {
			boolean wasEditing = this.isEditing;
			this.isEditing = isEditing;

			// This is a subsequent edit, so start from where we left off.
			if (!wasEditing && isEditing) {
				isEditingAgain = true;
				original = text;
			}
		}

		public void setText(Date text) {
			this.text = text;
		}

		private boolean equalsOrBothNull(Object o1, Object o2) {
			return (o1 == null) ? o2 == null : o1.equals(o2);
		}
	}

	private static Template template;

	private final SafeHtmlRenderer<String> renderer;

	/**
	 * Construct a new EditTextCell that will use a
	 * {@link SimpleSafeHtmlRenderer}.
	 */
	public DateInputCell() {
		this(SimpleSafeHtmlRenderer.getInstance());
	}

	/**
	 * Construct a new EditTextCell that will use a given {@link SafeHtmlRenderer}
	 * to render the value when not in edit mode.
	 *
	 * @param renderer a {@link SafeHtmlRenderer SafeHtmlRenderer<String>}
	 *          instance
	 */
	public DateInputCell(SafeHtmlRenderer<String> renderer) {
		super(CLICK, KEYUP, KEYDOWN, BLUR, KEYPRESS);
		if (template == null) {
			template = GWT.create(Template.class);
		}
		if (renderer == null) {
			throw new IllegalArgumentException("renderer == null");
		}
		this.renderer = renderer;

		setupUI();
		addDatePickerHandlers();
	}

	@Override
	public boolean isEditing(Context context, Element parent, Date value) {
		ViewData viewData = getViewData(context.getKey());
		return viewData == null ? false : viewData.isEditing();
	}

	@Override
	public void onBrowserEvent(Context context, Element parent, Date value,
	                           NativeEvent event, ValueUpdater<Date> valueUpdater) {
		Object key = context.getKey();
		ViewData viewData = getViewData(key);
		this.context = context;
		this.parent = parent;
		this.valueUpdater = valueUpdater;
		this.viewData = viewData;
		if (Element.is(event.getEventTarget())) {
			Element icon = Element.as(event.getEventTarget());
			if(icon.hasAttribute("imageTypeCalendar")){
				//Если мы нажали на календарь
				updateDatePickerValue(value);
				datePickerPanel.setPopupPosition(event.getClientX(), event.getClientY() + 10);
				datePickerPanel.show();
			} else {
				if (viewData != null && viewData.isEditing()) {
					//Если мы в режиме редактирования
					editEvent(context, parent, value, viewData, event, valueUpdater);
				} else {
					String type = event.getType();
					int keyCode = event.getKeyCode();
					boolean enterPressed = KEYUP.equals(type)
							&& keyCode == KeyCodes.KEY_ENTER;
					if (CLICK.equals(type) || enterPressed) {
						// Перейти в режим редактирования
						if (viewData == null) {
							viewData = new ViewData(value);
							setViewData(key, viewData);
						} else {
							viewData.setEditing(true);
						}
						edit(context, parent, value);
					}
				}
			}
		}
	}

	@Override
	public void render(Context context, Date value, SafeHtmlBuilder sb) {
		// Get the view data.
		Object key = context.getKey();
		ViewData viewData = getViewData(key);
		if (viewData != null && !viewData.isEditing() && value != null
				&& value.equals(viewData.getText())) {
			clearViewData(key);
			viewData = null;
		}

		Date toRender = value;
		if (viewData != null) {
			Date text = viewData.getText();
			if (viewData.isEditing()) {
        /*
         * Do not use the renderer in edit mode because the value of a text
         * input element is always treated as text. SafeHtml isn't valid in the
         * context of the value attribute.
         */
				if (text == null){
					sb.append(template.input(""));
					return;
				}
				sb.append(template.input(getFormattedDate(text)));
				return;
			} else {
				// The user pressed enter, but view data still exists.
				toRender = text;
			}
		}

		if (toRender != null && toRender.toString().trim().length() > 0) {
			sb.append(renderer.render(getFormattedDate(toRender)));
		}
		sb.append(template.calendarIcon());
	}

	@Override
	public boolean resetFocus(Context context, Element parent, Date value) {

		if (isEditing(context, parent, value)) {
			getInputElement(parent).focus();
			return true;
		}
		return false;
	}

	/**
	 * Convert the cell to edit mode.
	 *
	 * @param context the {@link Context} of the cell
	 * @param parent the parent element
	 * @param value the current value
	 */
	protected void edit(Context context, Element parent, Date value) {
		setValue(context, parent, value);
		InputElement input = getInputElement(parent);
		addInputHandler(input);
		input.focus();
		input.select();

	}

	private native void addInputHandler(Element input)
	/*-{
        var that = this;
        var oldVal = input.value;

        if (input.addEventListener) {    // all browsers except IE before version 9
            input.addEventListener ("input", OnInput, true);
        } else {
            var changeWatcher = {
                timeout: null,
                currentValue: input.value,
                watchForChange: function( el ) {
                    if( el.value != this.currentValue ) {
                        this.changed( el );
                    }
                    this.timeout = setTimeout( function() {
                        changeWatcher.watchForChange(el)
                    }, 20 );
                },
                cancelWatchForChange: function() {
                    clearTimeout( this.timeout );
                    this.timeout = null;
                },
                changed: function( el ) {
                    var res = that.@com.aplana.sbrf.taxaccounting.web.widget.cell.ValidatedInputCell::checkInputtedValue(Ljava/lang/String;)(input.value);
                    if (!res ) {
                        input.value = this.currentValue
                    } else {
                        this.currentValue = el.value;
                    }
                }
            }
            changeWatcher.watchForChange(input);
        }

        function OnInput (event) {
            res = that.@com.aplana.sbrf.taxaccounting.web.widget.cell.ValidatedInputCell::checkInputtedValue(Ljava/lang/String;)( event.target.value);
            if (!res && (input.value !== oldVal)) {
                input.value = oldVal;
            } else {
                oldVal = event.target.value;
            }
        }


    }-*/;

	protected boolean checkInputtedValue(String value) {

		return false;
	}

	/**
	 * Convert the cell to non-edit mode.
	 *
	 * @param context the context of the cell
	 * @param parent the parent Element
	 * @param value the value associated with the cell
	 */
	private void cancel(Context context, Element parent, Date value) {
		clearInput(getInputElement(parent));
		setValue(context, parent, value);
	}

	/**
	 * Clear selected from the input element. Both Firefox and IE fire spurious
	 * onblur events after the input is removed from the DOM if selection is not
	 * cleared.
	 *
	 * @param input the input element
	 */
	private native void clearInput(Element input) /*-{
        if (input.selectionEnd)
            input.selectionEnd = input.selectionStart;
        else if ($doc.selection)
            $doc.selection.clear();
    }-*/;

	/**
	 * Commit the current value.
	 *
	 * @param context the context of the cell
	 * @param parent the parent Element
	 * @param viewData the {@link ViewData} object
	 * @param valueUpdater the {@link ValueUpdater}
	 */
	private void commit(Context context, Element parent, ViewData viewData,
	                    ValueUpdater<Date> valueUpdater) {
		String value = updateViewData(parent, viewData, false);
		clearInput(getInputElement(parent));
		setValue(context, parent, viewData.getOriginal());
		if (valueUpdater != null) {
			valueUpdater.update(new Date(convertInputToLocalDateFormat(value)));
		}
	}

	private void commitDatePickerValue(Context context, Element parent, ViewData viewData, ValueUpdater<Date> valueUpdater, Date dateToUpdate) {
		updateViewData(dateToUpdate, viewData, false);
		clearInput(getInputElement(parent));
		setValue(context, parent, viewData.getOriginal());
		if (valueUpdater != null) {
			valueUpdater.update(dateToUpdate);
		}
	}

	private void editEvent(Context context, Element parent, Date value,
	                       ViewData viewData, NativeEvent event, ValueUpdater<Date> valueUpdater) {

		String type = event.getType();
		boolean keyUp = KEYUP.equals(type);
		boolean keyDown = KEYDOWN.equals(type);

		if (keyUp || keyDown) {
		//*******************
		//Пользователь нажал на клавишу клавиатуры
		//*******************
			int keyCode = event.getKeyCode();
			if (keyUp && keyCode == KeyCodes.KEY_ENTER) {
			//*******************
			// Нажата клавиша Enter
			//*******************
				commitIfValueIsCorrect(context, parent, value, viewData, valueUpdater);
			} else if (keyUp && keyCode == KeyCodes.KEY_ESCAPE) {
			//*******************
			// Нажата клавижа ESCAPE - отменяем редактирование и возвращаем значение, которое было перед редактирование
			//*******************
				cancelCellEditing(context, parent, value, viewData);
			} else {
			//*******************
			// Пользователь печатает текст в поле ввода...
			//*******************
				updateViewData(parent, viewData, true);
			}
		} else if (BLUR.equals(type)) {
			//*******************
			// Фокус потерян (пользователь щелкнул вне поля ввода)
			//*******************
			EventTarget eventTarget = event.getEventTarget();
			if (Element.is(eventTarget)) {
				Element target = Element.as(eventTarget);
				if ("input".equals(target.getTagName().toLowerCase())) {
					commitIfValueIsCorrect(context, parent, value, viewData, valueUpdater);
				}
			}
		}
		datePickerPanel.hide();
	}

	/**
	 * Get the input element in edit mode.
	 */
	public InputElement getInputElement(Element parent) {
		return parent.getFirstChild().<InputElement> cast();
	}

	/**
	 * Update the view data based on the current value.
	 *
	 * @param parent the parent element
	 * @param viewData the {@link ViewData} object to update
	 * @param isEditing true if in edit mode
	 * @return the new value
	 */
	private String updateViewData(Element parent, ViewData viewData,
	                              boolean isEditing) {
		InputElement input = (InputElement) parent.getFirstChild();
		String value = input.getValue();
		viewData.setText(DateTimeFormat.getFormat("dd.MM.yy").parse(value));
		viewData.setEditing(isEditing);
		return value;
	}

	private void updateViewData(Date value, ViewData viewData,
	                              boolean isEditing) {
		viewData.setText(value);
		viewData.setEditing(isEditing);
	}

	private String getFormattedDate(Date date){
		final String DATE_SHORT_START = DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.DATE_SHORT)
				.format(date);

		int startDayIndex = DATE_SHORT_START.lastIndexOf('-');
		int startMonthIndex = DATE_SHORT_START.indexOf('-');

		String startDate =  DATE_SHORT_START.substring(startDayIndex + 1, DATE_SHORT_START.length()) + '.' +
				DATE_SHORT_START.substring(startMonthIndex + 1, startDayIndex) + '.' +
				DATE_SHORT_START.substring(0, startMonthIndex);

		return startDate;
	}

	private String convertInputToLocalDateFormat(String input){
		String result;
		try {
			String day = input.substring(0, input.indexOf('.'));
			String month = input.substring(input.indexOf('.') + 1, input.lastIndexOf('.'));
			String year = input.substring(input.lastIndexOf('.') + 1, input.length());
			result = month + "/" + day + "/" + year;
		} catch (IndexOutOfBoundsException ex){
			result = input;
		}
		return result;
	}

	private void setupUI(){
		datePickerPanel.setWidth("200");
		datePickerPanel.setHeight("200");
		datePickerPanel.add(datePicker);
	}

	private void addDatePickerHandlers(){
		datePicker.addValueChangeHandler(new ValueChangeHandler<Date>() {
			@Override
			public void onValueChange(ValueChangeEvent<Date> event) {
				if(viewData == null){
					Object key = context.getKey();
					viewData = new ViewData(event.getValue());
					setViewData(key, viewData);
				}
				datePickerPanel.hide();
				commitDatePickerValue(context, parent, viewData, valueUpdater, event.getValue());
			}
		});
	}

	private void cancelCellEditing(Context context, Element parent, Date value, ViewData viewData){
		Date originalText = viewData.getOriginal();
		if (viewData.isEditingAgain()) {
			viewData.setText(originalText);
			viewData.setEditing(false);
		} else {
			setViewData(context.getKey(), null);
		}
		cancel(context, parent, value);
		datePickerPanel.hide();
	}

	private void commitEmptyString(Context context, Element parent){
		setViewData(context.getKey(), null);
		cancel(context, parent, null);
		datePickerPanel.hide();
	}

	private boolean isInputIsCorrect(Element parent){
		InputElement input = (InputElement) parent.getFirstChild();
		String inputted = input.getValue();
		try{
			new Date(convertInputToLocalDateFormat(inputted));
		} catch (IllegalArgumentException ex){
			return false;
		}
		return true;
	}

	//true - если пользователь поле ввода содержит пустое значение или строку из пробелов
	private static boolean isBlank(Element parent) {
		InputElement input = (InputElement) parent.getFirstChild();
		String cs = input.getValue();
		int strLen;
		if (cs == null || (strLen = cs.length()) == 0) {
			return true;
		}
		for (int i = 0; i < strLen; i++) {
			if ((" ".equals(cs.charAt(i)) == false)) {
				return false;
			}
		}
		return true;
	}

	private void commitIfValueIsCorrect(Context context, Element parent, Date value, ViewData viewData, ValueUpdater<Date> valueUpdater){
		if(isInputIsCorrect(parent)){
			//Если введенная дата не пустая и корректная - сохраняем ее.
			commit(context, parent, viewData, valueUpdater);
		} else {
			//Иначе отменяем редактирование
			if(isBlank(parent)){
				//Если пользователь стер значение в поле ввода - должны отобразить пустое значение
				commitEmptyString(context, parent);
			} else {
				//Если пользователь ввел некорректную дату - отображаем предыдущее значение даты
				cancelCellEditing(context, parent, value, viewData);
			}
		}
	}

	private void updateDatePickerValue(Date value){
		if(value != null){
			datePicker.setValue(value);
			datePicker.setCurrentMonth(value);
		} else {
			datePicker.setValue(new Date());
			datePicker.setCurrentMonth(new Date());
		}
		datePicker.refreshComponents();
	}
}
