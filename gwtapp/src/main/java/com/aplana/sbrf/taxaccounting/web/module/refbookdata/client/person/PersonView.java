package com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.person;

import com.aplana.gwt.client.ModalWindow;
import com.aplana.gwt.client.modal.CanHide;
import com.aplana.gwt.client.modal.OnHideHandler;
import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.DeclarationDataSearchResultItem;
import com.aplana.sbrf.taxaccounting.model.Formats;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.HorizontalAlignment;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.RefBookColumn;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.RefBookDataRow;
import com.aplana.sbrf.taxaccounting.web.module.sources.shared.model.CurrentAssign;
import com.aplana.sbrf.taxaccounting.web.widget.pager.FlexiblePager;
import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.client.RefBookPickerWidget;
import com.aplana.sbrf.taxaccounting.web.widget.style.GenericDataGrid;
import com.aplana.sbrf.taxaccounting.web.widget.style.LinkButton;
import com.aplana.sbrf.taxaccounting.web.widget.style.table.CheckBoxHeader;
import com.aplana.sbrf.taxaccounting.web.widget.utils.WidgetUtils;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.view.client.DefaultSelectionEventManager;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.PopupViewWithUiHandlers;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import static com.google.gwt.view.client.DefaultSelectionEventManager.createCustomManager;

/**
 * Представление попапа модального окна "Файлы и комментарии",
 * данное окно вызывается с формы нф
 *
 * @author Lhaziev
 */
public class PersonView extends PopupViewWithUiHandlers<PersonUiHandlers> implements PersonPresenter.MyView{

    public interface Binder extends UiBinder<PopupPanel, PersonView> {
    }

    @UiField
    ModalWindow modalWindow;

    @UiField
    Label note;

    @UiField
    LinkButton addOriginalPerson, removeOriginalPerson;

    @UiField
    LinkButton addDuplicatePerson, removeDuplicatePerson;

    @UiField
    GenericDataGrid<RefBookDataRow> tableOriginal, tableDuplicate;

    @UiField
    FlexiblePager pager;

    @UiField
    Button saveButton, cancelButton;

    private RefBookPickerWidget personPicker, duplicatePersonPicker;

    private MultiSelectionModel<RefBookDataRow> selectionModel;
    private Column<RefBookDataRow, Boolean> checkColumn;
    private final CheckBoxHeader checkBoxHeader = new CheckBoxHeader();
    private DefaultSelectionEventManager<RefBookDataRow> multiSelectManager = createCustomManager(
            new DefaultSelectionEventManager.CheckboxEventTranslator<RefBookDataRow>()
    );

    private ListDataProvider<RefBookDataRow> model;

    @Inject
    public PersonView(Binder uiBinder, EventBus eventBus) {
        super(eventBus);
        PopupPanel widget = uiBinder.createAndBindUi(this);
        widget.setAnimationEnabled(true);
        initWidget(widget);

        personPicker = new RefBookPickerWidget(false, false);
        personPicker.setAttributeId(9059L);
        personPicker.setWidth("100%");
        personPicker.setManualUpdate(true);
        personPicker.setPeriodDates(new Date(), new Date());

        personPicker.addValueChangeHandler(new ValueChangeHandler<List<Long>>() {
            @Override
            public void onValueChange(ValueChangeEvent<List<Long>> event) {
                if (event.getValue() != null && !event.getValue().isEmpty()) {
                    getUiHandlers().addOriginalPerson(personPicker.getSingleValue());
                }
            }
        });

        duplicatePersonPicker = new RefBookPickerWidget(false, false);
        duplicatePersonPicker.setAttributeId(9059L);
        duplicatePersonPicker.setWidth("100%");
        duplicatePersonPicker.setManualUpdate(true);
        duplicatePersonPicker.setPeriodDates(new Date(), new Date());

        duplicatePersonPicker.addValueChangeHandler(new ValueChangeHandler<List<Long>>() {
            @Override
            public void onValueChange(ValueChangeEvent<List<Long>> event) {
                if (event.getValue() != null && !event.getValue().isEmpty()) {
                    getUiHandlers().addDuplicatePerson(duplicatePersonPicker.getSingleValue());
                }
            }
        });

        selectionModel = new MultiSelectionModel<RefBookDataRow>();
        tableDuplicate.setSelectionModel(selectionModel, multiSelectManager);

        model = new ListDataProvider<RefBookDataRow>();
        model.addDataDisplay(tableDuplicate);

        checkColumn = new Column<RefBookDataRow, Boolean>(
                new CheckboxCell(true, false)) {
            @Override
            public Boolean getValue(RefBookDataRow object) {
                return selectionModel.isSelected(object);
            }
        };

        selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                onSelection();
            }
        });

        checkBoxHeader.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                for (RefBookDataRow row: tableDuplicate.getVisibleItems()) {
                    selectionModel.setSelected(row, event.getValue());
                }
            }
        });

        pager.setDisplay(tableDuplicate);
    }

    private void onSelection() {
        int selectedItemCount = selectionModel.getSelectedSet().size();
        int visibleItemCount = tableDuplicate.getVisibleItemCount();

        if (selectedItemCount < visibleItemCount || selectedItemCount == 0 || visibleItemCount == 0) {
            checkBoxHeader.setValue(false);
        } else if (selectedItemCount == visibleItemCount) {
            checkBoxHeader.setValue(true);
        }

        removeDuplicatePerson.setEnabled (selectedItemCount > 0);
    }

    private final List<String> noteAlias = new ArrayList<String>() {{
       add("RECORD_ID");
       add("LAST_NAME");
       add("FIRST_NAME");
       add("MIDDLE_NAME");
       add("INN");
       add("SNILS");
       add("OLD_ID");
    }};

    @Override
    public void init(RefBookDataRow row, List<RefBookAttribute> columns) {
        StringBuilder stringBuilder = new StringBuilder();
        for(RefBookAttribute refBookColumn: columns) {
            if (!noteAlias.contains(refBookColumn.getAlias())) {
                continue;
            }
            String value = row.getValues().get(refBookColumn.getAlias());
            if (refBookColumn.getAlias().equals("OLD_ID") && (value == null || value.isEmpty())) {
                continue;
            }
            if (stringBuilder.length() > 0) {
                stringBuilder.append(", ");
            }
            stringBuilder.append(refBookColumn.getName()).append(" = ").append(value);
        }
        note.setText(stringBuilder.toString());
        selectionModel.clear();
        removeDuplicatePerson.setEnabled(false);
    }

    @UiHandler("saveButton")
    void addSaveButtonClicked(ClickEvent event) {
        getUiHandlers().onSave();
    }

    @UiHandler("cancelButton")
    void addCancelButtonClicked(ClickEvent event) {
        hide();
    }

    @UiHandler("addOriginalPerson")
    void addOriginalPersonClicked(ClickEvent event) {
        String filter = "OLD_ID is null" ;
        if (getUiHandlers().getOriginalRow() != null) {
            filter += " AND ID != " + getUiHandlers().getOriginalRow().getRefBookRowId();
        }
        if (getUiHandlers().getRow() != null) {
            filter += " AND ID != " + getUiHandlers().getRow().getRefBookRowId();
        }
        for(RefBookDataRow refBookDataRow: getUiHandlers().getDuplicateRows()) {
            filter += " AND ID != " + refBookDataRow.getRefBookRowId();
        }
        personPicker.setFilter(filter);
        personPicker.open();
    }

    @UiHandler("removeOriginalPerson")
    void removeOriginalPersonClicked(ClickEvent event) {
        getUiHandlers().removeOriginalPerson();
    }

    @Override
    public void setTableRows(List<RefBookDataRow> rows) {
        if (rows != null) {
            addOriginalPerson.setEnabled(false);
            removeOriginalPerson.setEnabled(true);
            tableOriginal.setPageSize(1);
            tableOriginal.setRowCount(rows.size());
            tableOriginal.setRowData(0, rows);
        } else {
            addOriginalPerson.setEnabled(true);
            removeOriginalPerson.setEnabled(false);
            tableOriginal.setRowCount(0);
            tableOriginal.setRowData(new ArrayList<RefBookDataRow>());
        }
    }

    @UiHandler("addDuplicatePerson")
    void addDuplicatePersonClicked(ClickEvent event) {
        String filter = "OLD_ID is null" ;
        if (getUiHandlers().getOriginalRow() != null) {
            filter += " AND ID != " + getUiHandlers().getOriginalRow().getRefBookRowId();
        }
        if (getUiHandlers().getRow() != null) {
            filter += " AND ID != " + getUiHandlers().getRow().getRefBookRowId();
        }
        for(RefBookDataRow refBookDataRow: getUiHandlers().getDuplicateRows()) {
            filter += " AND ID != " + refBookDataRow.getRefBookRowId();
        }
        duplicatePersonPicker.setFilter(filter);
        duplicatePersonPicker.open();
    }

    @UiHandler("removeDuplicatePerson")
    void removeDuplicatePersonClicked(ClickEvent event) {
        getUiHandlers().removeDuplicatePerson(new ArrayList<RefBookDataRow>(selectionModel.getSelectedSet()));
    }

    @Override
    public void setDuplicateTableRows(List<RefBookDataRow> rows) {
        model.getList().clear();
        model.getList().addAll(rows);
        tableDuplicate.redraw();
        selectionModel.clear();
        checkBoxHeader.setValue(false);
    }

    @Override
    public void setTableColumns(final List<RefBookColumn> columns) {
        int i;
        while ((i = tableOriginal.getColumnCount()) != 0) {
            tableOriginal.removeColumn(i - 1);
        }

        for (final RefBookColumn header : columns) {
            Column column;
            if (Formats.BOOLEAN.equals(header.getFormat())) {
                column = new Column<RefBookDataRow, Boolean>(new AbstractCell<Boolean>() {
                    @Override
                    public void render(Context context, Boolean value, SafeHtmlBuilder sb) {
                        sb.append(value != null && value ? WidgetUtils.UNCHECKABLE_TRUE : WidgetUtils.UNCHECKABLE_FALSE);
                    }
                }) {
                    @Override
                    public Boolean getValue(RefBookDataRow object) {
                        String s = object.getValues().get(header.getAlias());
                        if (s != null && !s.trim().isEmpty()) {
                            try {
                                long l = Long.parseLong(s.trim());
                                return l > 0;
                            } catch (NumberFormatException e) {
                                return false;
                            }

                        } else {
                            return false;
                        }
                    }
                };
                column.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
            } else if(header.getAttributeType() == RefBookAttributeType.NUMBER){
                column = new TextColumn<RefBookDataRow>() {
                    private NumberFormat numberFormat;
                    {
                        StringBuilder mask = new StringBuilder("#,##0");
                        if (header.getPrecision() > 0) {
                            mask.append('.');
                            for(int i = 0; i < header.getPrecision(); ++i) {
                                mask.append('0');
                            }
                        }
                        numberFormat = NumberFormat.getFormat(mask.toString());
                    }

                    @Override
                    public String getValue(RefBookDataRow object) {
                        try{
                            String s = object.getValues().get(header.getAlias());
                            return numberFormat.format(new BigDecimal(s)).replace(',', ' ');
                        } catch (NumberFormatException e){
                            return object.getValues().get(header.getAlias());
                        }
                    }
                };
                column.setHorizontalAlignment(convertAlignment(header.getAlignment()));
            } else {
                column = new TextColumn<RefBookDataRow>() {
                    @Override
                    public String getValue(RefBookDataRow object) {
                        return object.getValues().get(header.getAlias());
                    }
                };

                column.setHorizontalAlignment(convertAlignment(header.getAlignment()));
            }
            column.setSortable(false);
            tableOriginal.addResizableColumn(column, header.getName());
            tableOriginal.setColumnWidth(column, header.getWidth(), Style.Unit.EM);
        }
    }

    @Override
    public void setDuplicateTableColumns(final List<RefBookColumn> columns) {
        int i;
        while ((i = tableDuplicate.getColumnCount()) != 0) {
            tableDuplicate.removeColumn(i - 1);
        }

        tableDuplicate.addColumn(checkColumn, checkBoxHeader, 40, Style.Unit.PX);
        tableDuplicate.setColumnWidth(checkColumn, 2, Style.Unit.EM);

        for (final RefBookColumn header : columns) {
            Column column;
            if (Formats.BOOLEAN.equals(header.getFormat())) {
                column = new Column<RefBookDataRow, Boolean>(new AbstractCell<Boolean>() {
                    @Override
                    public void render(Context context, Boolean value, SafeHtmlBuilder sb) {
                        sb.append(value != null && value ? WidgetUtils.UNCHECKABLE_TRUE : WidgetUtils.UNCHECKABLE_FALSE);
                    }
                }) {
                    @Override
                    public Boolean getValue(RefBookDataRow object) {
                        String s = object.getValues().get(header.getAlias());
                        if (s != null && !s.trim().isEmpty()) {
                            try {
                                long l = Long.parseLong(s.trim());
                                return l > 0;
                            } catch (NumberFormatException e) {
                                return false;
                            }

                        } else {
                            return false;
                        }
                    }
                };
                column.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
            } else if(header.getAttributeType() == RefBookAttributeType.NUMBER){
                column = new TextColumn<RefBookDataRow>() {
                    private NumberFormat numberFormat;
                    {
                        StringBuilder mask = new StringBuilder("#,##0");
                        if (header.getPrecision() > 0) {
                            mask.append('.');
                            for(int i = 0; i < header.getPrecision(); ++i) {
                                mask.append('0');
                            }
                        }
                        numberFormat = NumberFormat.getFormat(mask.toString());
                    }

                    @Override
                    public String getValue(RefBookDataRow object) {
                        try{
                            String s = object.getValues().get(header.getAlias());
                            return numberFormat.format(new BigDecimal(s)).replace(',', ' ');
                        } catch (NumberFormatException e){
                            return object.getValues().get(header.getAlias());
                        }
                    }
                };
                column.setHorizontalAlignment(convertAlignment(header.getAlignment()));
            } else {
                column = new TextColumn<RefBookDataRow>() {
                    @Override
                    public String getValue(RefBookDataRow object) {
                        return object.getValues().get(header.getAlias());
                    }
                };

                column.setHorizontalAlignment(convertAlignment(header.getAlignment()));
            }
            column.setSortable(false);
            tableDuplicate.addResizableColumn(column, header.getName());
            tableDuplicate.setColumnWidth(column, header.getWidth(), Style.Unit.EM);
        }
    }


    private HasHorizontalAlignment.HorizontalAlignmentConstant convertAlignment(HorizontalAlignment alignment) {
        switch (alignment) {
            case ALIGN_LEFT:
                return HasHorizontalAlignment.ALIGN_LEFT;
            case ALIGN_CENTER:
                return HasHorizontalAlignment.ALIGN_CENTER;
            case ALIGN_RIGHT:
                return HasHorizontalAlignment.ALIGN_RIGHT;
            default:
                return HasHorizontalAlignment.ALIGN_LEFT;
        }
    }
}
