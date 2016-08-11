package com.aplana.sbrf.taxaccounting.web.widget.cell;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.formdata.AbstractCell;
import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.client.RefBookPickerWidget;
import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.shared.model.PickerContext;
import com.google.gwt.cell.client.AbstractEditableCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.text.shared.SafeHtmlRenderer;
import com.google.gwt.text.shared.SimpleSafeHtmlRenderer;

import java.util.Arrays;
import java.util.List;

import static com.google.gwt.dom.client.BrowserEvents.CLICK;
import static com.google.gwt.dom.client.BrowserEvents.KEYDOWN;

/**
 * Ячейка для редактирования значений из справочника. 
 * 
 * @author sgoryachkin
 *
 */
public class RefBookCell extends AbstractEditableCell<Long, String> {

	interface Template extends SafeHtmlTemplates {
		@Template("<img style=\"margin: 3px 2px;\" align=\"right\" src=\"resources/img/circle.png\"/>")
		SafeHtml referenceIcon();
	}
	
	protected static final SafeHtmlRenderer<String> renderer = SimpleSafeHtmlRenderer.getInstance();

	private RefBookPickerWidget refBookPiker;
	
	private HandlerRegistration changeHandlerRegistration;

	private boolean refBookPikerAlredyInit;
    private long attrId;

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
        refBookPiker = new RefBookPickerWidget(column.isHierarchical(), null);
        PickerContext context = new PickerContext();
        context.setRegionFilter(PickerContext.RegionFilter.FORM_FILTER);
        context.setFormDataId(columnContext.getFormDataId());
        refBookPiker.setPickerContext(context);
		// Create popup panel
        refBookPiker.setTitle(this.column.getName());
        attrId = column.isHierarchical() ? column.getNameAttributeId() : column.getRefBookAttributeId();

        refBookPiker.setVisible(false);
        refBookPiker.setVersionEnabled(column.isVersioned());
		refBookPiker.setDepartmentPanelEnabled(Department.REF_BOOK_ID.equals(column.getRefBookId()));
		refBookPiker.setManualUpdate(true);
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
            if (this.changeHandlerRegistration != null) {
                this.changeHandlerRegistration.removeHandler();
            }

            // Регистрируем событие изменения значени
			this.changeHandlerRegistration = refBookPiker.addValueChangeHandler(new ValueChangeHandler<List<Long>>() {
				@Override
				public void onValueChange(ValueChangeEvent<List<Long>> event) {
					// Update the cell and value updater.
                    List<Long> values = event.getValue();
                    Long value = (values != null && !values.isEmpty() ? values.get(0) : null);

					@SuppressWarnings("unchecked")
					DataRow<Cell> dataRow = (DataRow<Cell>) context.getKey();
					Cell cell = dataRow.getCell(RefBookCell.this.column.getAlias());

                    if (refBookPiker.isHierarchical()) {
                        cell.setRefBookDereference(refBookPiker.getOtherDereferenceValue(column.getRefBookAttributeId()));
                    } else {
                        Long attrId2 = column.getRefBookAttributeId2();
                        if (attrId2 != null && attrId2 != 0) {
                            cell.setRefBookDereference(refBookPiker.getOtherDereferenceValue(attrId, attrId2));
                        } else {
                            cell.setRefBookDereference(refBookPiker.getDereferenceValue());
                        }
                    }

					setValue(context, parent, value);

                    // Разыменование для зависимых ячеек
                    List<Cell> linkedCells = dataRow.getLinkedCells(column.getId());
                    if (linkedCells != null) {
                        for (Cell refCell : linkedCells) {
                            ReferenceColumn referenceColumn = (ReferenceColumn)refCell.getColumn();
                            Long attrId = referenceColumn.getRefBookAttributeId();
                            Long attrId2 = referenceColumn.getRefBookAttributeId2();
                            refCell.setRefBookDereference( attrId2 != null && attrId2 != 0 ?
                                    refBookPiker.getOtherDereferenceValue(attrId, attrId2):
                                    refBookPiker.getOtherDereferenceValue(attrId));
                        }
                    }

                    if (valueUpdater != null) {
						valueUpdater.update(value);
					}
				}
			});

            // При нажатии на ячейку инициализируем справочник, если он ещё не инициализирован
            if (!refBookPikerAlredyInit) {
                refBookPikerAlredyInit = true;
                refBookPiker.setAttributeId(attrId);
                refBookPiker.setFilter(column.getFilter());
                refBookPiker.setSearchEnabled(column.isSearchEnabled());
                refBookPiker.setPeriodDates(columnContext.getStartDate(), columnContext.getEndDate());
            }

            // Устанавливаем старое значение
            if (nvalue != null) {
                if(refBookPiker.getValue()!= null && !refBookPiker.getValue().isEmpty()){
                    //if(!nvalue.equals(refBookPiker.getValue().get(0))){
                        refBookPiker.setValue(Arrays.asList(nvalue), false);
                    //}
                } else {
                    refBookPiker.setSingleValue(nvalue, false);
                }
            } else {
                refBookPiker.setSingleValue(null, false);
            }

			// Устанавливаем позицию и отображаем справочник
			refBookPiker.open();
			
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
		} else {
			rendValue = columnContext.getColumn().getFormatter().format(rendValue);
		}

		sb.append(renderer.render(rendValue));
        if ((DataRowEditableCellUtils.editMode(columnContext, cell))&&cell.isEditable()) {
            sb.append(template.referenceIcon());
        }
    }
}

