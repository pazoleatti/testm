package com.aplana.sbrf.taxaccounting.web.widget.cell;

import static com.google.gwt.dom.client.BrowserEvents.CLICK;
import static com.google.gwt.dom.client.BrowserEvents.KEYDOWN;

import com.aplana.sbrf.taxaccounting.model.dictionary.SimpleDictionaryItem;
import com.aplana.sbrf.taxaccounting.web.widget.dictionarypicker.client.DictionaryPickerWidget;
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


/**
 * Заготовка для редактора ячеек, отображающих значения из справочника
 * Код и идея работы основаны на коде DatePickerCell: при клике на ячейке отображается popup-окно, в котором отображается 
 * виджет для выбора элемента 
 * TODO: сделать генериком, для того, чтобы можно было работать не только со строковыми кодами в справочниках, но и с числовыми
 */
public class DictionaryCell extends AbstractEditableCell<String, String> {

	private static final int ESCAPE = 27;

	private final DictionaryPickerWidget widget;
	private int offsetX = 10;
	private int offsetY = 10;
	private Object lastKey;
	private Element lastParent;
	private int lastIndex;
	private int lastColumn;
	private String lastValue;
	private PopupPanel panel;
	private final SafeHtmlRenderer<String> renderer;
	private ValueUpdater<String> valueUpdater;

	public DictionaryCell() {
		this(SimpleSafeHtmlRenderer.getInstance());
	}
	
	public DictionaryCell(SafeHtmlRenderer<String> rendererp) {
		super(CLICK, KEYDOWN);
		if (rendererp == null) {
			throw new IllegalArgumentException("renderer == null");
		}
		
		this.renderer = rendererp;
		
		
		valueUpdater = new ValueUpdater<String>() {
			@Override
	        public void update(String value) {
//	          fieldUpdater.update(index, rowValue, value);
//				renderer.render("123456");
//				Element cellParent = lastParent;
//				String oldValue = lastValue;
//				Object key = lastKey;
//				int index = lastIndex;
//				int column = lastColumn;
//				setValue(new Context(index, column, key), cellParent, "12345");
//				Window.alert("HELLO");
				
	        }
		};

		this.widget = new DictionaryPickerWidget();
		widget.addValueChangeHandler(new ValueChangeHandler<SimpleDictionaryItem<Long>>() {
			
			@Override
			public void onValueChange(ValueChangeEvent<SimpleDictionaryItem<Long>> event) {
//				Window.alert("My value = " + event.getValue());
				if (event.getValue() != null && event.getValue().getValue() != null) {
					renderer.render(event.getValue().getName());
				} else {
					renderer.render("&nbsp;");
				}
				Element cellParent = lastParent;
				String oldValue = lastValue;
				Object key = lastKey;
				int index = lastIndex;
				int column = lastColumn;
				if (event.getValue() != null && event.getValue().getValue() != null) {
					setValue(new Context(index, column, key), cellParent, event.getValue().getName());
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
		panel.add(widget);

		// Hide the panel and call valueUpdater.update when a date is selected
		/**
		widget.addValueChangeHandler(new ValueChangeHandler<Date>() {
			public void onValueChange(ValueChangeEvent<Date> event) {
				// Remember the values before hiding the popup.
				Element cellParent = lastParent;
				String oldValue = lastValue;
				Object key = lastKey;
				int index = lastIndex;
				int column = lastColumn;
				panel.hide();

				// Update the cell and value updater.
				Date date = event.getValue();
				setViewData(key, date.toString());
				setValue(new Context(index, column, key), cellParent, oldValue);
				if (valueUpdater != null) {
					valueUpdater.update(date.toString());
				}
			}
		});
		*/
	}

	@Override
	public boolean isEditing(Context context, Element parent, String value) {
		return lastKey != null && lastKey.equals(context.getKey());
	}

	@Override
	public void onBrowserEvent(Context context, Element parent, String value,
			NativeEvent event, ValueUpdater<String> valueUpdater) {
		super.onBrowserEvent(context, parent, value, event, valueUpdater);
		if (CLICK.equals(event.getType())) {
			onEnterKeyDown(context, parent, value, event, valueUpdater);
		}
	}

	@Override
	public void render(Context context, String value, SafeHtmlBuilder sb) {
		// Get the view data.
		Object key = context.getKey();
		String viewData = getViewData(key);
		if (viewData != null && viewData.equals(value)) {
			clearViewData(key);
			viewData = null;
		}

		String s = null;
		if (viewData != null) {
			s = viewData;
		} else if (value != null) {
			s = value;
		}
		if (s != null) {
			sb.append(renderer.render(s));
		} else {
			sb.append(renderer.render("&nbsp;"));
		}
		
	}

	@Override
	protected void onEnterKeyDown(Context context, Element parent, String value,
			NativeEvent event, ValueUpdater<String> valueUpdater) {
		this.lastKey = context.getKey();
		this.lastParent = parent;
		this.lastValue = value;
		this.lastIndex = context.getIndex();
		this.lastColumn = context.getColumn();
		this.valueUpdater = valueUpdater;

		// TODO: инициализация поискового виджета
		//String viewData = getViewData(lastKey);
		//String searchItem = (viewData == null) ? lastValue : viewData;
		// widget.setSearchText(searchItem)
		
		panel.setPopupPositionAndShow(new PositionCallback() {
			public void setPosition(int offsetWidth, int offsetHeight) {
				panel.setPopupPosition(lastParent.getAbsoluteLeft() + offsetX,
						lastParent.getAbsoluteTop() + offsetY);
			}
		});
	}
}
