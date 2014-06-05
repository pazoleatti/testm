package com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.client;

import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.shared.DeclarationTypeTemplate;
import com.aplana.sbrf.taxaccounting.web.widget.style.GenericCellTable;
import com.aplana.sbrf.taxaccounting.web.widget.style.table.ComparatorWithNull;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

import java.util.List;

/**
 * Представление для страницы администрирования деклараций.
 *
 */
public class DeclarationTemplateListView extends ViewWithUiHandlers<DeclarationTemplateListUiHandlers>
		implements DeclarationTemplateListPresenter.MyView {
	interface Binder extends UiBinder<Widget, DeclarationTemplateListView> {
	}
	
	/**
	 * Список шаблонов деклараций
	 */
	@UiField
    GenericCellTable<DeclarationTypeTemplate> declarationTemplateTable;
    @UiField
    Panel filterContentPanel;

    private SingleSelectionModel<DeclarationTypeTemplate> selectionModel;
    private ListDataProvider<DeclarationTypeTemplate> dataProvider = new ListDataProvider<DeclarationTypeTemplate>();
    private ColumnSortEvent.ListHandler<DeclarationTypeTemplate> dataSortHandler;

    private DeclarationTypeTemplate selectedItem;

    @Inject
	public DeclarationTemplateListView(Binder binder) {
		initWidget(binder.createAndBindUi(this));

        selectionModel = new SingleSelectionModel<DeclarationTypeTemplate>();

        selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                selectedItem = getSelectedElement();
            }
        });

		// колонка Наименование декларации
		Column<DeclarationTypeTemplate, DeclarationTypeTemplate> linkColumn = new Column<DeclarationTypeTemplate, DeclarationTypeTemplate>(
				new AbstractCell<DeclarationTypeTemplate>() {
					@Override
					public void render(Context context,
                                       DeclarationTypeTemplate declarationTypeTemplate,
									   SafeHtmlBuilder sb) {
						if (declarationTypeTemplate == null) {
							return;
						}
						sb.appendHtmlConstant("<a href=\"#"
								+ DeclarationTemplateTokens.declarationVersionList + ";"
								+ DeclarationTemplateTokens.declarationType + "="
								+ declarationTypeTemplate.getTypeId() + "\">"
								+ declarationTypeTemplate.getTypeName() + "</a>");
					}
				}) {
			@Override
			public DeclarationTypeTemplate getValue(DeclarationTypeTemplate object) {
				return object;
			}
		};

        TextColumn<DeclarationTypeTemplate> versionColumn = new TextColumn<DeclarationTypeTemplate>() {
            @Override
            public String getValue(DeclarationTypeTemplate declarationTypeTemplate) {
                return String.valueOf(declarationTypeTemplate.getVersionCount());
            }
        };
        linkColumn.setSortable(true);

        versionColumn.setSortable(true);

		declarationTemplateTable.addResizableColumn(linkColumn, "Наименование");
		declarationTemplateTable.addResizableColumn(versionColumn, "Количество версий");

        declarationTemplateTable.setSelectionModel(selectionModel);
        dataProvider.addDataDisplay(declarationTemplateTable);

        declarationTemplateTable.setColumnWidth(linkColumn, 70, Style.Unit.PCT);
        // Настройке сортировки для таблицы
        declarationTemplateTable.getColumnSortList().push(linkColumn);
        dataSortHandler = new ColumnSortEvent.ListHandler<DeclarationTypeTemplate>(dataProvider.getList());
        dataSortHandler.setComparator(linkColumn, new ComparatorWithNull<DeclarationTypeTemplate, String>() {
            @Override
            public int compare(DeclarationTypeTemplate o1, DeclarationTypeTemplate o2) {
                return compareWithNull(o1.getTypeName(), o2.getTypeName());
            }
        });
        dataSortHandler.setComparator(versionColumn, new ComparatorWithNull<DeclarationTypeTemplate, Integer>() {
            @Override
            public int compare(DeclarationTypeTemplate o1, DeclarationTypeTemplate o2) {
                return compareWithNull(o1.getVersionCount(), o2.getVersionCount());
            }
        });
        declarationTemplateTable.addColumnSortHandler(dataSortHandler);
	}

	@Override
	public void setDeclarationTypeTemplateRows(List<DeclarationTypeTemplate> result) {
        dataProvider.setList(result);
        declarationTemplateTable.setVisibleRange(0, result.size());
        dataSortHandler.setList(dataProvider.getList());
        /* При единственном значении не перерисовывается таблица http://jira.aplana.com/browse/SBRFACCTAX-7612 */
        declarationTemplateTable.flush();
        selectionModel.clear();
        if (selectedItem != null) {
            for(DeclarationTypeTemplate item: result) {
                if (item.getTypeId() == selectedItem.getTypeId()) {
                    selectionModel.setSelected(item, true);
                    break;
                }
            }
        }
	}

    @Override
    public DeclarationTypeTemplate getSelectedElement() {
        return selectionModel.getSelectedObject();
    }

    @Override
    public void setInSlot(Object slot, IsWidget content) {
        if (slot == DeclarationTemplateListPresenter.OBJECT) {
            filterContentPanel.clear();
            if (content != null) {
                filterContentPanel.add(content);
            }
        } else {
            super.setInSlot(slot, content);
        }
    }

    @UiHandler("delete")
    void onDeleteTemplate(ClickEvent event){
        if (getUiHandlers() != null)
            getUiHandlers().onDeleteClicked();
    }

    @UiHandler("create")
    void onCreateButtonClicked(ClickEvent event) {
        if (getUiHandlers() != null) {
            getUiHandlers().onCreateClicked();
        }
    }
}
