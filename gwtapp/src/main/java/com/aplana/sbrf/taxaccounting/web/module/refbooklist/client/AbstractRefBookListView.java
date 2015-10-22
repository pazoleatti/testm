package com.aplana.sbrf.taxaccounting.web.module.refbooklist.client;

import com.aplana.sbrf.taxaccounting.model.refbook.RefBookType;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.RefBookDataTokens;
import com.aplana.sbrf.taxaccounting.web.module.refbooklist.shared.TableModel;
import com.aplana.sbrf.taxaccounting.web.widget.style.GenericDataGrid;
import com.aplana.sbrf.taxaccounting.web.widget.style.LinkAnchor;
import com.aplana.sbrf.taxaccounting.web.widget.style.table.ComparatorWithNull;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.ProvidesKey;
import com.google.gwt.view.client.SingleSelectionModel;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

import java.util.List;

/**
 * Абстрактный класс представления списка справочников
 *
 * @author Stanislav Yasinskiy
 * @author Fail Mukhametdinov
 */
public abstract class AbstractRefBookListView extends ViewWithUiHandlers<RefBookListUiHandlers>
        implements AbstractRefBookListPresenter.MyView {

    public static final String[] COLUMN_NAMES = {
            "Наименование справочника",
            "Видимость справочника",
            "Тип справочника",              // редак/нередак
            "Региональность справочника",
            "Вид справочника"};             // лин/иерарх

    @UiField
    GenericDataGrid<TableModel> formDataTable;
    @UiField
    TextBox filterText;
    @UiField
    LinkAnchor loadButton;

    Column<TableModel, TableModel> nameColumn;
    TextColumn<TableModel> visibleColumn;
    TextColumn<TableModel> editableColumn;
    TextColumn<TableModel> regionColumn;
    TextColumn<TableModel> typeColumn;

    protected SingleSelectionModel<TableModel> selectionModel;
    protected ListDataProvider<TableModel> dataProvider;
    protected ColumnSortEvent.ListHandler<TableModel> sortHandler;
    protected ProvidesKey<TableModel> providesKey = new ProvidesKey<TableModel>() {
        @Override
        public Long getKey(TableModel item) {
            return item.getId();
        }
    };

    @Override
    public void setTableData(List<TableModel> tableData, Long selectedItemId) {
        dataProvider.setList(tableData);
        sortHandler.setList(dataProvider.getList());
        formDataTable.setVisibleRange(0, dataProvider.getList().size());
        formDataTable.flush();
        selectionModel.clear();
        if (selectedItemId != null) {
            for(TableModel item: tableData) {
                if (item.getId().equals(selectedItemId)) {
                    selectionModel.setSelected(item, true);
                    break;
                }
            }
        }
    }

    protected void setSelectionModel() {
        selectionModel = new SingleSelectionModel<TableModel>(providesKey);
        dataProvider = new ListDataProvider<TableModel>(providesKey);
        sortHandler = new ColumnSortEvent.ListHandler<TableModel>(dataProvider.getList());

        formDataTable.setSelectionModel(selectionModel);
        dataProvider.addDataDisplay(formDataTable);
        formDataTable.addColumnSortHandler(sortHandler);
    }

    protected void initColumns(final boolean adminView){
        nameColumn = new Column<TableModel, TableModel>(
                new AbstractCell<TableModel>() {
                    @Override
                    public void render(Cell.Context context, TableModel model, SafeHtmlBuilder sb) {
                        if (model == null) {
                            return;
                        }
                        sb.appendHtmlConstant("<a href=\"#" +
                                (adminView ?
                                    RefBookDataTokens.REFBOOK_SCRIPT :
                                    (RefBookType.LINEAR == model.getType() ? RefBookDataTokens.REFBOOK_DATA : RefBookDataTokens.REFBOOK_HIER_DATA)
                                ) +
                                ";" + RefBookDataTokens.REFBOOK_DATA_ID + "=" + model.getId() + "\">" + model.getName() + "</a>");
                    }
                }) {
            @Override
            public TableModel getValue(TableModel object) {
                return object;
            }
        };

        visibleColumn = new TextColumn<TableModel>() {
            @Override
            public String getValue(TableModel tableModel) {
                return tableModel.isVisible() ? "Видимый" : "Скрытый";
            }
        };

        editableColumn = new TextColumn<TableModel>() {
            @Override
            public String getValue(TableModel tableModel) {
                return tableModel.isReadOnly() ? "Только для чтения" : "Редактируемый";
            }
        };

        regionColumn = new TextColumn<TableModel>() {
            @Override
            public String getValue(TableModel tableModel) {
                return tableModel.getRegionAttribute() == null ? "Общий" : "Региональный";
            }
        };

        typeColumn = new TextColumn<TableModel>() {
            @Override
            public String getValue(TableModel tableModel) {
                return tableModel.getRefBookType().getId() == 0 ? "Линейный" : "Иерархический";
            }
        };

        nameColumn.setSortable(true);
        visibleColumn.setSortable(true);
        editableColumn.setSortable(true);
        regionColumn.setSortable(true);
        typeColumn.setSortable(true);

        sortHandler.setComparator(nameColumn, new ComparatorWithNull<TableModel, Integer>() {
            @Override
            public int compare(TableModel o1, TableModel o2) {
                return compareWithNull(o1.getRowNumber(), o2.getRowNumber());
            }
        });

        sortHandler.setComparator(visibleColumn, new ComparatorWithNull<TableModel, Boolean>() {
            @Override
            public int compare(TableModel o1, TableModel o2) {
                return compareWithNull(o2.isVisible(), o1.isVisible());
            }
        });

        sortHandler.setComparator(editableColumn, new ComparatorWithNull<TableModel, Boolean>() {
            @Override
            public int compare(TableModel o1, TableModel o2) {
                return compareWithNull(o1.isReadOnly(), o2.isReadOnly());
            }
        });

        sortHandler.setComparator(regionColumn, new ComparatorWithNull<TableModel, String>() {
            @Override
            public int compare(TableModel o1, TableModel o2) {
                return compareWithNull(o1.getRegionAttribute() == null ? "Общий" : "Региональный",
                        o2.getRegionAttribute() == null ? "Общий" : "Региональный");
            }
        });

        sortHandler.setComparator(typeColumn, new ComparatorWithNull<TableModel, Integer>() {
            @Override
            public int compare(TableModel o1, TableModel o2) {
                return compareWithNull(o2.getRefBookType().getId(), o1.getRefBookType().getId());
            }
        });
    }



    @Override
    public Long getSelectedId() {
        return (Long) selectionModel.getKey(selectionModel.getSelectedObject());
    }

    @Override
    public String getFilter() {
        return filterText.getText();
    }

    @Override
    public void setFilter(String filterText) {
        this.filterText.setText(filterText);
    }

    @SuppressWarnings("GwtUiHandlerErrors")
    @UiHandler("findButton")
    void onFindClicked(ClickEvent event) {
        if (getUiHandlers() != null) {
            getUiHandlers().onFindClicked();
        }
    }

    @SuppressWarnings("GwtUiHandlerErrors")
    @UiHandler("filterText")
    void onFilterPressClicked(KeyPressEvent event) {
        if (KeyCodes.KEY_ENTER == event.getNativeEvent().getKeyCode()) {
            if (getUiHandlers() != null) {
                getUiHandlers().onFindClicked();
            }
        }
    }
}
