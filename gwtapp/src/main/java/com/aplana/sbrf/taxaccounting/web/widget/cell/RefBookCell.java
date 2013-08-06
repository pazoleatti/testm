package com.aplana.sbrf.taxaccounting.web.widget.cell;

import static com.google.gwt.dom.client.BrowserEvents.CLICK;
import static com.google.gwt.dom.client.BrowserEvents.KEYDOWN;

import java.util.Date;

import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.RefBookColumn;
import com.aplana.sbrf.taxaccounting.web.widget.refbookpicker.client.RefBookPicker;
import com.aplana.sbrf.taxaccounting.web.widget.refbookpicker.client.RefBookPickerWidget;
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
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.PopupPanel.PositionCallback;


/**
 * Ячейка для редактирования значений из справочника.
 * Код и идея работы основаны на коде DatePickerCell: при клике на ячейке отображается popup-окно, в котором,
 * отображается виджет для выбора элемента.
 *
 * Не просто постичь высокую философию {@link AbstractEditableCell}.
 *
 * @param <ValueType> тип значения справочника
 */
public class RefBookCell extends AbstractEditableCell<Long, String> {

	private int offsetX = 10;
	private int offsetY = 10;

	private Object lastKey;
	private Element lastParent;
	private Long lastValue;

	private int lastIndex;
	private int lastColumn;
	private PopupPanel panel;
	protected final SafeHtmlRenderer<String> renderer;
	//private ValueUpdater<Long> valueUpdater;
	private RefBookPicker refBookPiker = new RefBookPickerWidget();
	private Long refBookAttrId;
	private String columnAlias;
	private boolean refBookPikerAlredyInit;
	private Date startDate;
	private Date endDate;

	public RefBookCell(ColumnContext columnContext) {
		super(CLICK, KEYDOWN);
		this.refBookAttrId = ((RefBookColumn) columnContext.getColumn()).getRefBookAttributeId();
		this.columnAlias = columnContext.getColumn().getAlias();
		this.renderer = SimpleSafeHtmlRenderer.getInstance();
		this.startDate = columnContext.getStartDate();
		this.endDate = columnContext.getEndDate();

		// Create popup panel
		this.panel = new PopupPanel(true, true) {
			@Override
			protected void onPreviewNativeEvent(NativePreviewEvent event) {
				if (Event.ONKEYUP == event.getTypeInt()) {
					if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ESCAPE) {
						// Dismiss when escape is pressed
						panel.hide();
						//selectWidget.clear();
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
				//selectWidget.clear();
			}
		});
		
		
		panel.add(refBookPiker);
	}


	@Override
	public boolean isEditing(Context context, Element parent, Long value) {
		return lastKey != null && lastKey.equals(context.getKey());
	}

	@Override
	public void onBrowserEvent(Context context, Element parent, Long value,
							   NativeEvent event, final ValueUpdater<Long> valueUpdater) {
		super.onBrowserEvent(context, parent, value, event, valueUpdater);
		if (CLICK.equals(event.getType())) {
			refBookPiker.addValueChangeHandler(
					new ValueChangeHandler<Long>() {
						@Override
						public void onValueChange(ValueChangeEvent<Long> event) {
							// Remember the values before hiding the popup.
							Element cellParent = lastParent;
							// Не очень понял зачем запоминать старые значения.
							//Long oldValue = lastValue;
							Object key = lastKey;
							int index = lastIndex;
							int column = lastColumn;
							panel.hide();

							// Update the cell and value updater.
							Long value = event.getValue();
							//setViewData(key, String.valueOf(value));
							
							DataRow<Cell> dataRow = (DataRow<Cell>) key;
							Cell cell = dataRow.getCell(columnAlias);
							cell.setRefBookDereference(refBookPiker.getDereferenceValue());
							
							setValue(new Context(index, column, key), cellParent, value);
							if (valueUpdater != null) {
								valueUpdater.update(value);
							}
						}
					}
			);
			onEnterKeyDown(context, parent, value, event, valueUpdater);
		}
	}


	@Override
	protected void onEnterKeyDown(Context context, Element parent, Long value,
								  NativeEvent event, ValueUpdater<Long> valueUpdater) {
		this.lastKey = context.getKey();
		this.lastParent = parent;
		this.lastValue = value;
		this.lastIndex = context.getIndex();
		this.lastColumn = context.getColumn();
		//this.valueUpdater = valueUpdater;

		if (!refBookPikerAlredyInit){
			refBookPiker.setAcceptableValues(refBookAttrId, startDate, endDate);
			refBookPikerAlredyInit = true;
		}
		
		refBookPiker.setValue(lastValue);

		panel.setPopupPositionAndShow(new PositionCallback() {
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

				panel.setPopupPosition(
						lastParent.getAbsoluteLeft() + exceedOffsetX,
						lastParent.getAbsoluteTop() + exceedOffsetY
				);
			}
		});

		//selectWidget.asWidget().focus();
	}

	@Override
	public void render(Context context, Long value, SafeHtmlBuilder sb) {
		@SuppressWarnings("unchecked")
		DataRow<Cell> dataRow = (DataRow<Cell>) context.getKey();
		Cell cell = dataRow.getCell(columnAlias);
		String rendValue = cell.getRefBookDereference();
		if (rendValue == null){
			rendValue = "";
		}
		sb.append(renderer.render(rendValue));
	}
}
