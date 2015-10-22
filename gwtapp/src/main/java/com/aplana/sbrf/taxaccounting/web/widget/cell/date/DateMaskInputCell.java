package com.aplana.sbrf.taxaccounting.web.widget.cell.date;

import com.aplana.gwt.client.mask.MaskUtils;
import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.web.widget.cell.ColumnContext;
import com.aplana.sbrf.taxaccounting.web.widget.utils.TextUtils;
import com.google.gwt.cell.client.AbstractEditableCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.text.shared.SafeHtmlRenderer;
import com.google.gwt.text.shared.SimpleSafeHtmlRenderer;

import java.util.Date;

import static com.google.gwt.dom.client.BrowserEvents.*;

/**
 * Редакрируемая ячейчка с датой и маской
 * @author unknown
 * @author add mask aivanov
 */
public class DateMaskInputCell extends
        AbstractEditableCell<Date, DateMaskInputCell.ViewData> {

	public static final String DEFAULT_DATE_FORMAT = "dd.MM.yyyy";
	public static final String DEFAULT_DATE_MASK = "99.99.9999";

	private ValueUpdater<Date> valueUpdater;

    private String dateFormat;
    private String mask;
    private String maskPicture;

    protected ColumnContext columnContext;

	interface Template extends SafeHtmlTemplates {
		@Template("<input type=\"text\" value=\"{0}\" tabindex=\"-1\"></input>")
		SafeHtml input(String value);
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
	 * {@link com.google.gwt.text.shared.SimpleSafeHtmlRenderer}.
	 */
	public DateMaskInputCell() {
		this(SimpleSafeHtmlRenderer.getInstance(), new ColumnContext());
	}

	/**
	 * Construct a new EditTextCell that will use a given {@link com.google.gwt.text.shared.SafeHtmlRenderer}
	 * to render the value when not in edit mode.
	 *
     * @param renderer a {@link com.google.gwt.text.shared.SafeHtmlRenderer SafeHtmlRenderer<String>}
     *          instance
     * @param columnContext
     */
	public DateMaskInputCell(SafeHtmlRenderer<String> renderer, ColumnContext columnContext) {
		super(CLICK, KEYDOWN, BLUR, KEYPRESS);
		if (template == null) {
			template = GWT.create(Template.class);
		}
		if (renderer == null) {
			throw new IllegalArgumentException("renderer == null");
		}
		this.renderer = renderer;
        this.columnContext = columnContext;

        this.dateFormat = DEFAULT_DATE_FORMAT;
        this.mask = DEFAULT_DATE_MASK;
        this.maskPicture = MaskUtils.createMaskPicture(this.mask);
	}

    public DateMaskInputCell(SafeHtmlRenderer<String> renderer, ColumnContext columnContext, String mask, String dateFormat) {
        this(renderer, columnContext);
        this.dateFormat = dateFormat;
        this.mask = mask;
        this.maskPicture = MaskUtils.createMaskPicture(mask);
    }

	@Override
	public boolean isEditing(Context context, Element parent, Date value) {
		ViewData vd = getViewData(context.getKey());
		return isEditing(vd);
	}

    private boolean isEditing(ViewData vd){
        return vd != null && vd.isEditing();
    }

	@Override
	public void render(Context context, Date value, SafeHtmlBuilder sb) {
		Object key = context.getKey();
		ViewData vd = getViewData(key);
		if (vd != null && !vd.isEditing() && value != null
				&& value.equals(vd.getText())) {
			clearViewData(key);
			vd = null;
		}

		Date toRender = value;
		if (vd != null) {
			Date text = vd.getText();
			if (vd.isEditing()) {
        /*
         * Do not use the renderer in edit mode because the value of a text
         * input element is always treated as text. SafeHtml isn't valid in the
         * context of the value attribute.
         */
				if (text == null){
					sb.append(template.input(maskPicture));
					return;
				}
				sb.append(template.input(getFormattedDate(text)));
				return;
			} else {
				// The user pressed enter, but view data still exists.
				toRender = text;
			}
		}

		if (toRender != null && !toRender.toString().trim().isEmpty()) {
			sb.append(renderer.render(getFormattedDate(toRender)));
		}
    }

	@Override
	public boolean resetFocus(Context context, Element parent, Date value) {
        if (isEditing(context, parent, value)) {
			getInputElement(parent).focus();
			return true;
		}
		return false;
	}

    @Override
    public void onBrowserEvent(Context context, Element parent, Date value, NativeEvent event, ValueUpdater<Date> valueUpdater) {
        Object key = context.getKey();
        ViewData vd = getViewData(key);

        this.valueUpdater = valueUpdater;

        if (Element.is(event.getEventTarget())) {
            if (isEditing(vd)) {
                editEvent(context, parent, value, vd, event, valueUpdater);
            } else {
                if (CLICK.equals(event.getType()) || event.getKeyCode() == KeyCodes.KEY_ENTER) {
                    // Перейти в режим редактирования
                    if (vd == null) {
                        vd = new ViewData(value);
                        setViewData(key, vd);
                    } else {
                        vd.setEditing(true);
                    }
                    edit(context, parent, value);
                }
            }
        }
    }

    /*
     * Обработка события нажатия если у ячейки режим редактирования
     */
    private void editEvent(Context context, Element parent, Date value, ViewData viewData, NativeEvent event, ValueUpdater<Date> valueUpdater) {
        String type = event.getType();
        int keyCode = event.getKeyCode();

        if (KEYDOWN.equals(type)) {
            // обработка нажатий специальных клавиш
            if (keyCode == KeyCodes.KEY_ENTER) {
                commitIfValueIsCorrect(context, parent, value, viewData, valueUpdater);
            } else if (keyCode == KeyCodes.KEY_ESCAPE) {
                // отменяем редактирование и возвращаем значение, которое было перед редактирование
                cancelCellEditing(context, parent, value, viewData);
            } else if (keyCode == KeyCodes.KEY_BACKSPACE || keyCode == KeyCodes.KEY_DELETE){
                onDelete(getInputElement(parent), event, mask, maskPicture);
                event.preventDefault();
                event.stopPropagation();
            }

        } else if (KEYPRESS.equals(type)) {
            // обрабатываем
            if (keyCode == KeyCodes.KEY_ENTER) {
                //игнорим нажатие по ентеру потому что он ловится на onPress'е
                return;
            }
            // обработка нажатий текстовых клавиш
            onPress(getInputElement(parent), event, mask, maskPicture);
            event.preventDefault();
            event.stopPropagation();
            updateViewData(parent, viewData, true);

        } else if (BLUR.equals(type)) {   // Фокус потерян
            EventTarget eventTarget = event.getEventTarget();
            if (Element.is(eventTarget)) {
                Element target = Element.as(eventTarget);
                if ("input".equals(target.getTagName().toLowerCase())) {
                    commitIfValueIsCorrect(context, parent, value, viewData, valueUpdater);
                }
            }
        }
    }

    private void onDelete(InputElement elem, NativeEvent event, String mask, String picture) {

        String inputText = elem.getValue();
        int cursor = TextUtils.getCursorPos(elem);

        if (!elem.getValue().isEmpty() && TextUtils.getSelectionLength(elem) > 0) {
            String tmp = prepareTextWithSelection(elem, inputText, mask);
            cursor = TextUtils.getSelectionStart(elem);
            // Если получивщийся текст пустой то вставляем маску для пользователя
            inputText = tmp.length() != 0 ? tmp : picture;
        }

        //Вручную сдвигаем курсор, так как событие нажатия на бекспей не прокидывается дальше
        if (event.getKeyCode() == KeyCodes.KEY_BACKSPACE)
            cursor--;

        if (cursor < 0) {
            elem.setValue(picture);
            TextUtils.setCursorPos(elem, 0);
            return;
        }

        StringBuffer applied = new StringBuffer();
        // если курсор в самом конце - не обратаываем нажатие
        if(cursor >= mask.length()){
            return;
        }

        char mc = mask.charAt(cursor);  // символ в маске под текущей позицией курсора

        //Заменяем введенный символ на прочерк если он попадает под маску
        if ((mc == '9' || mc == 'X')  && !inputText.isEmpty()) {
            applied.append(inputText.substring(0, cursor));
            applied.append("_");
            applied.append(inputText.substring(cursor + 1));
        } else
            applied.append(inputText);

        elem.setValue(applied.length() != 0 ? applied.toString() : picture);
        if (applied.length() != 0) {
            TextUtils.setCursorPos(elem, cursor);
        }
    }

    private void onPress(InputElement elem, NativeEvent event, String mask, String picture){
        String inputText;

        String value = elem.getValue();
        inputText = value == null || value.isEmpty() ? picture : elem.getValue();

        int cursor = TextUtils.getCursorPos(elem);

        if (!elem.getValue().isEmpty() && TextUtils.getSelectionLength(elem) > 0) {
            inputText = prepareTextWithSelection(elem, inputText, mask);
            cursor = TextUtils.getSelectionStart(elem);
        }

        StringBuffer applied = new StringBuffer(inputText); // буфер

        if (cursor >= mask.length()) {
            return;
        }

        char mc = mask.charAt(cursor);          // символ в маске под текущей позицией курсора
        char ch = (char) event.getCharCode();   // клавиша которую нажал пользователь

        boolean loop;
        do {
            loop = false;
            switch (mc) {
                case '9':
                    if (Character.isDigit(ch)) {
                        applied = applied.deleteCharAt(cursor);
                        applied.insert(cursor, ch);
                    } else
                        cursor--;
                    break;
                case 'X':
                    if (Character.isLetterOrDigit(ch)) {
                        applied = applied.deleteCharAt(cursor);
                        applied.insert(cursor, ch);
                    } else
                        cursor--;
                    break;
                default:
                    // Перескакивание разделителей
                    if (mc != ch) {
                        loop = true;
                        cursor++;
                        mc = mask.charAt(cursor);
                    }
            }
        } while (loop);

        cursor++;

        elem.setValue(applied.toString());
        TextUtils.setCursorPos(elem, cursor);
    }

    /**
     * Если в тексте есть выделение заменяем на символы из maskPicture
     * @param elem элемент ввода
     * @param inputText введенные символы
     * @param mask маска
     * @return текст с замененым символами
     */
    private String prepareTextWithSelection(InputElement elem, String inputText, String mask){
        StringBuffer applied = new StringBuffer();

        int selectStart = TextUtils.getSelectionStart(elem);     // Начальная позиция выделения
        int selectEnd = selectStart + TextUtils.getSelectionLength(elem);               // Конечная

        applied.append(inputText.substring(0, selectStart));  // Копируем в буфер  часть что до начала позиции выделения

        for (int i = 0; i < TextUtils.getSelectionLength(elem); i++) {
            if (mask.toCharArray()[applied.length()] == '9' || mask.toCharArray()[applied.length()] == 'X')
                applied.append('_');
            else
                applied.append(mask.toCharArray()[applied.length()]);
        }

        applied.append(inputText.substring(selectEnd));    // Копируем часть ввода после выбора в буфер

        return applied.toString();
    }

	/**
	 * Convert the cell to edit mode.
	 *
	 * @param context the {@link com.google.gwt.cell.client.Cell.Context} of the cell
	 * @param parent the parent element
	 * @param value the current value
	 */
	protected void edit(Context context, Element parent, Date value) {
		setValue(context, parent, value);
		InputElement input = getInputElement(parent);
		input.focus();
        TextUtils.setCursorPos(input, 0);
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
	 * @param valueUpdater the {@link com.google.gwt.cell.client.ValueUpdater}
	 */
	private void commit(Context context, Element parent, ViewData viewData, ValueUpdater<Date> valueUpdater) {
		String value = updateViewData(parent, viewData, false);
		clearInput(getInputElement(parent));
		setValue(context, parent, viewData.getOriginal());
		if (valueUpdater != null) {
			valueUpdater.update(DateTimeFormat.getFormat(dateFormat).parseStrict(value));
		}
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
	private String updateViewData(Element parent, ViewData viewData, boolean isEditing) {
		InputElement input = (InputElement) parent.getFirstChild();
		String value = input.getValue();
		try {
			viewData.setText(DateTimeFormat.getFormat(dateFormat).parseStrict(value));
		} catch (IllegalArgumentException ex) {
			viewData.setText(viewData.getOriginal());
		}
		viewData.setEditing(isEditing);
		return value;
	}

	private String getFormattedDate(Date date){
		return DateTimeFormat.getFormat(dateFormat).format(date);
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
	}

	private void commitEmptyString(Context context, Element parent){
		setViewData(context.getKey(), null);
		cancel(context, parent, null);
		valueUpdater.update(null);
	}

	private boolean isInputIsCorrect(Element parent){
		InputElement input = (InputElement) parent.getFirstChild();
		String inputted = input.getValue();
        try{
            if (inputted.length() > dateFormat.length()) {
                throw new IllegalArgumentException();
            }
            Date date = DateTimeFormat.getFormat(dateFormat).parseStrict(inputted);
            if (date.before(Cell.DATE_1900)) return false;
        } catch (IllegalArgumentException ex){
            return false;
        }
		return true;
	}

	/**
	 * Проверяет наличие значения в поле ввода
	 * @param parent parent Element
	 * @return true, если пользователь поле ввода содержит пустое значение или строку из пробелов
	 */
	private boolean isBlank(Element parent) {
		InputElement input = (InputElement) parent.getFirstChild();
		String text = input.getValue();
		return text.equals(maskPicture) || text == null || text.trim().isEmpty();
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
}
