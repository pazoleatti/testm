package com.aplana.sbrf.taxaccounting.web.widget.cell;

import static com.google.gwt.dom.client.BrowserEvents.CLICK;
import static com.google.gwt.dom.client.BrowserEvents.KEYDOWN;

import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.RefBookColumn;
import com.aplana.sbrf.taxaccounting.model.formdata.AbstractCell;
import com.aplana.sbrf.taxaccounting.web.widget.refbookpicker.client.RefBookPicker;
import com.aplana.sbrf.taxaccounting.web.widget.refbookpicker.client.RefBookPickerWidget;
import com.google.gwt.cell.client.AbstractEditableCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
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
 * 
 * @author sgoryachkin
 *
 */
public class RefBookCell extends AbstractEditableCell<Long, String> {

	interface Template extends SafeHtmlTemplates {
		@Template("<img align=\"right\" src=\"resources/img/reference-16.gif\"/>")
		SafeHtml referenceIcon();
	}
	
	protected static final SafeHtmlRenderer<String> renderer = SimpleSafeHtmlRenderer.getInstance();

	private PopupPanel panel;
	private RefBookPicker refBookPiker = new RefBookPickerWidget();
	
	private HandlerRegistration changeHandlerRegistration;

	private boolean refBookPikerAlredyInit;

	private ColumnContext columnContext;
	private RefBookColumn column;
	private static Template template;
	public RefBookCell(ColumnContext columnContext) {
		super(CLICK, KEYDOWN);
		this.columnContext = columnContext;
		this.column = (RefBookColumn) columnContext.getColumn();
		if (template == null) {
			template = GWT.create(Template.class);
		}
		// Create popup panel
		this.panel = new PopupPanel(true, true) {
			@Override
			protected void onPreviewNativeEvent(NativePreviewEvent event) {
				if (Event.ONKEYUP == event.getTypeInt() 
						&& event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ESCAPE) {
					panel.hide();
				}
			}
		};
		
		panel.addCloseHandler(new CloseHandler<PopupPanel>() {
			public void onClose(CloseEvent<PopupPanel> event) {
				changeHandlerRegistration.removeHandler();
			}
		});

		panel.add(refBookPiker);
	}

	@Override
	public boolean isEditing(Context context, Element parent, Long value) {
		return false;
	}

	@Override
	public void onBrowserEvent(final Context context, final Element parent, final Long nvalue,
			final NativeEvent nevent, final ValueUpdater<Long> valueUpdater) {
		

		AbstractCell editableCell = ((DataRow<?>) context.getKey()).getCell(column.getAlias());
		if (!DataRowEditableCellUtils.editMode(columnContext, editableCell)) {
			return;
		}
			
	    String eventType = nevent.getType();
	    if ((BrowserEvents.KEYDOWN.equals(eventType) && nevent.getKeyCode() == KeyCodes.KEY_ENTER)
	    		|| (CLICK.equals(eventType))) {
			
			// При нажатии на ячейку инициализируем справочник, если он ещё не инициализирован
			if (!refBookPikerAlredyInit) {
				refBookPiker.setAcceptableValues(column.getRefBookAttributeId(), column.getFilter(), columnContext.getStartDate(),
						columnContext.getEndDate());
				refBookPikerAlredyInit = true;
			}
			// Устанавливаем старое значение
			refBookPiker.setValue(nvalue);
			
			// Регистрируем событие изменения значени 
			this.changeHandlerRegistration = refBookPiker.addValueChangeHandler(new ValueChangeHandler<Long>() {
				@Override
				public void onValueChange(ValueChangeEvent<Long> event) {
					// Update the cell and value updater.
					Long value = event.getValue();

					@SuppressWarnings("unchecked")
					DataRow<Cell> dataRow = (DataRow<Cell>) context.getKey();
					Cell cell = dataRow.getCell(RefBookCell.this.column.getAlias());
					cell.setRefBookDereference(refBookPiker
							.getDereferenceValue());

					setValue(context, parent, value);
					
					if (valueUpdater != null) {
						valueUpdater.update(value);
					}
					// Скрываем панель. При скрытии удаляется хендлер.
					panel.hide();
				}
			});
			
			// Устанавливаем позицию и отображаем справочник
			panel.setPopupPositionAndShow(new PositionCallback() {
				public void setPosition(int offsetWidth, int offsetHeight) {
					int windowHeight = Window.getClientHeight();
					int windowWidth = Window.getClientWidth();

					int exceedOffsetX = parent.getParentElement().getAbsoluteLeft();
					int exceedOffsetY = parent.getParentElement().getAbsoluteBottom();					

					// Сдвигаем попап, если он не помещается в окно
					if ((panel.getAbsoluteLeft() + panel.getOffsetWidth()) > windowWidth) {
						exceedOffsetX -= panel.getOffsetWidth();
					}

					if ((parent.getAbsoluteTop() + panel.getOffsetHeight()) > windowHeight) {
						exceedOffsetY -= panel.getOffsetHeight() + parent.getParentElement().getOffsetHeight();
					}
					panel.setPopupPosition(exceedOffsetX, exceedOffsetY);

				}
			});
			
		}
	}


	@Override
	public void render(Context context, Long value, SafeHtmlBuilder sb) {
		@SuppressWarnings("unchecked")
		DataRow<Cell> dataRow = (DataRow<Cell>) context.getKey();
		Cell cell = dataRow.getCell(column.getAlias());
		String rendValue = cell.getRefBookDereference();
		if (rendValue == null) {
			rendValue = "";
		}
		sb.append(renderer.render(rendValue));
		sb.append(template.referenceIcon());
	}
}
