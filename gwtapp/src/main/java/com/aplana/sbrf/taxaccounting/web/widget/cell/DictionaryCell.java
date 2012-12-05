package com.aplana.sbrf.taxaccounting.web.widget.cell;

import com.aplana.sbrf.taxaccounting.web.widget.dictionarypicker.client.DictionaryPickerWidget;
import com.google.gwt.cell.client.AbstractEditableCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.text.shared.SafeHtmlRenderer;
import com.google.gwt.text.shared.SimpleSafeHtmlRenderer;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.PopupPanel.PositionCallback;

import java.io.Serializable;

import static com.google.gwt.dom.client.BrowserEvents.CLICK;
import static com.google.gwt.dom.client.BrowserEvents.KEYDOWN;


/**
 * Ячейка для редактирования значений из справочника.
 * Код и идея работы основаны на коде DatePickerCell: при клике на ячейке отображается popup-окно, в котором,
 * отображается виджет для выбора элемента.
 *
 * Не просто постичь высокую философию {@link AbstractEditableCell}.
 *
 * @param <ValueType> тип значения справочника
 */
public abstract class DictionaryCell<ValueType extends Serializable> extends AbstractEditableCell<ValueType, String> {

	private int offsetX = 10;
	private int offsetY = 10;

	private Object lastKey;
	private Element lastParent;
	private ValueType lastValue;

	private int lastIndex;
	private int lastColumn;
	private PopupPanel panel;
	protected final SafeHtmlRenderer<String> renderer;
	private ValueUpdater<ValueType> valueUpdater;
	private final DictionaryPickerWidget<ValueType> selectWidget;

	public DictionaryCell(String dictionaryCode) {
		super(CLICK, KEYDOWN);
		this.renderer = SimpleSafeHtmlRenderer.getInstance();

		// Create selectWidget
		selectWidget = createWidget(dictionaryCode);

		// Create popup panel
		this.panel = new PopupPanel(true, true) {
			@Override
			protected void onPreviewNativeEvent(NativePreviewEvent event) {
				if (Event.ONKEYUP == event.getTypeInt()) {
					if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ESCAPE) {
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

		// Put selectWidget on panel
		panel.add(selectWidget);

		selectWidget.addValueChangeHandler(
				new ValueChangeHandler<ValueType>() {
					@Override
					public void onValueChange(ValueChangeEvent<ValueType> event) {
						// Remember the values before hiding the popup.
						Element cellParent = lastParent;
						ValueType oldValue = lastValue;
						Object key = lastKey;
						int index = lastIndex;
						int column = lastColumn;
						panel.hide();

						// Update the cell and value updater.
						ValueType value = event.getValue();
						setViewData(key, valueToString(value));
						setValue(new Context(index, column, key), cellParent, oldValue);
						if (valueUpdater != null) {
							valueUpdater.update(value);
						}

						panel.hide();
					}
				}
		);
	}

	protected abstract DictionaryPickerWidget<ValueType> createWidget(String dictionaryCode);

	protected abstract String valueToString(ValueType value);

	@Override
	public boolean isEditing(Context context, Element parent, ValueType value) {
		return lastKey != null && lastKey.equals(context.getKey());
	}

	@Override
	public void onBrowserEvent(Context context, Element parent, ValueType value,
							   NativeEvent event, ValueUpdater<ValueType> valueUpdater) {
		super.onBrowserEvent(context, parent, value, event, valueUpdater);
		if (CLICK.equals(event.getType())) {
			onEnterKeyDown(context, parent, value, event, valueUpdater);
		}
	}

	@Override
	protected void onEnterKeyDown(Context context, Element parent, ValueType value,
								  NativeEvent event, ValueUpdater<ValueType> valueUpdater) {
		this.lastKey = context.getKey();
		this.lastParent = parent;
		this.lastValue = value;
		this.lastIndex = context.getIndex();
		this.lastColumn = context.getColumn();
		this.valueUpdater = valueUpdater;

		selectWidget.setValue(lastValue);

		panel.setPopupPositionAndShow(new PositionCallback() {
			public void setPosition(int offsetWidth, int offsetHeight) {
				panel.setPopupPosition(
						lastParent.getAbsoluteLeft() + offsetX,
						lastParent.getAbsoluteTop() + offsetY
				);
			}
		});
		selectWidget.init();
	}

	@Override
	public void render(Context context, ValueType value, SafeHtmlBuilder sb) {
		// Get the view data.
		Object key = context.getKey();
		String viewData = getViewData(key);
		String stringValue = valueToString(value);

		if (viewData != null && viewData.equals(stringValue)) {
			clearViewData(key);
			viewData = null;
		}

		String s;
		if (viewData != null) {
			s = viewData;
		} else {
			s = stringValue;
		}

		sb.append(renderer.render(s));
	}
}
