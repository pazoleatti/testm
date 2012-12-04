package com.aplana.sbrf.taxaccounting.web.widget.cell;

import com.aplana.sbrf.taxaccounting.model.dictionary.DictionaryItem;
import com.aplana.sbrf.taxaccounting.web.widget.dictionarypicker.client.DictionaryPickerWidget;
import com.aplana.sbrf.taxaccounting.web.widget.dictionarypicker.client.TextDictionaryWidget;
import com.google.gwt.cell.client.AbstractEditableCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
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
 * Заготовка для редактора ячеек, отображающих значения из справочника
 * Код и идея работы основаны на коде DatePickerCell: при клике на ячейке отображается popup-окно, в котором отображается 
 * виджет для выбора элемента 
 * TODO: сделать генериком, для того, чтобы можно было работать не только со строковыми кодами в справочниках, но и с числовыми
 */
public abstract class DictionaryCell<ValueType extends Serializable> extends AbstractEditableCell<ValueType, String> {

	private static final int ESCAPE = 27;

	private int offsetX = 10;
	private int offsetY = 10;
	private Object lastKey;
	private Element lastParent;
	private int lastIndex;
	private int lastColumn;
	private PopupPanel panel;
	protected final SafeHtmlRenderer<String> renderer;

	public DictionaryCell(String dictionaryCode) {
		this(SimpleSafeHtmlRenderer.getInstance(), dictionaryCode);
	}
	
	public DictionaryCell(SafeHtmlRenderer<String> rendererp, String dictionaryCode) {
		super(CLICK, KEYDOWN);
		if (rendererp == null) {
			throw new IllegalArgumentException("renderer == null");
		}
		
		this.renderer = rendererp;

		DictionaryPickerWidget<ValueType> widget = createWidget(dictionaryCode);
		widget.addValueChangeHandler(new ValueChangeHandler<DictionaryItem<ValueType>>() {

			@Override
			public void onValueChange(ValueChangeEvent<DictionaryItem<ValueType>> event) {
				Element cellParent = lastParent;
				Object key = lastKey;
				int index = lastIndex;
				int column = lastColumn;
				if (event.getValue() != null && event.getValue().getValue() != null) {
					setValue(new Context(index, column, key), cellParent, event.getValue().getValue());
				} else {
					setValue(new Context(index, column, key), cellParent, null);
				}
				panel.hide();

			}
		});
		this.panel = new PopupPanel(true, true) {
			@Override
			protected void onPreviewNativeEvent(NativePreviewEvent event) {
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
		panel.add(widget);
	}

	protected abstract DictionaryPickerWidget<ValueType> createWidget(String dictionaryCode);

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
	public abstract void render(Context context, ValueType value, SafeHtmlBuilder sb);

	@Override
	protected void onEnterKeyDown(Context context, Element parent, ValueType value,
			NativeEvent event, ValueUpdater<ValueType> valueUpdater) {
		this.lastKey = context.getKey();
		this.lastParent = parent;
		this.lastIndex = context.getIndex();
		this.lastColumn = context.getColumn();

		panel.setPopupPositionAndShow(new PositionCallback() {
			public void setPosition(int offsetWidth, int offsetHeight) {
				panel.setPopupPosition(lastParent.getAbsoluteLeft() + offsetX,
						lastParent.getAbsoluteTop() + offsetY);
			}
		});
	}
}
