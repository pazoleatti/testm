package com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.view;

import com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.AdminConstants;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.presenter.AdminPresenter;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.presenter.AdminUIHandlers;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.shared.FormTypeTemplate;
import com.aplana.sbrf.taxaccounting.web.widget.style.GenericCellTable;
import com.aplana.sbrf.taxaccounting.web.widget.style.table.ComparatorWithNull;
import com.google.gwt.cell.client.AbstractCell;
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
 * Представление для страницы администрирования.
 *
 * @author Vitalii Samolovskikh
 * @author Eugene Stetsenko
 */
public class AdminView extends ViewWithUiHandlers<AdminUIHandlers> implements AdminPresenter.MyView {
	interface Binder extends UiBinder<Widget, AdminView> {
	}

    @UiField
    Panel filterContentPanel;
    @UiField
    Panel editFormContentPanel;

	@UiField
    GenericCellTable<FormTypeTemplate> formTemplateTable;

    private SingleSelectionModel<FormTypeTemplate> selectionModel;
    private ListDataProvider<FormTypeTemplate> dataProvider;
    private ColumnSortEvent.ListHandler<FormTypeTemplate> sortHandler;

    private FormTypeTemplate selectedItem;

	@Inject
	public AdminView(Binder binder) {
		initWidget(binder.createAndBindUi(this));

        selectionModel = new SingleSelectionModel<FormTypeTemplate>();
        dataProvider = new ListDataProvider<FormTypeTemplate>();
        sortHandler = new ColumnSortEvent.ListHandler<FormTypeTemplate>(dataProvider.getList());

        selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                selectedItem = getSelectedElement();
                getUiHandlers().onSelectionChanged(selectedItem);
            }
        });

        formTemplateTable.setSelectionModel(selectionModel);

		// колонка Наименование декларации
		Column<FormTypeTemplate, FormTypeTemplate> linkColumn = new Column<FormTypeTemplate, FormTypeTemplate>(
				new AbstractCell<FormTypeTemplate>() {
					@Override
					public void render(Context context,
                                       FormTypeTemplate formTypeTemplate,
									   SafeHtmlBuilder sb) {
						if (formTypeTemplate == null) {
							return;
						}
						sb.appendHtmlConstant("<a href=\"#"
								+ AdminConstants.NameTokens.formTemplateVersionList + ";"
								+ AdminConstants.NameTokens.formTypeId + "="
								+ formTypeTemplate.getFormTypeId() + "\">"
								+ formTypeTemplate.getFormTypeName() + "</a>");
					}
				}) {
			@Override
			public FormTypeTemplate getValue(FormTypeTemplate object) {
				return object;
			}
		};

        TextColumn<FormTypeTemplate> typeTaxColumn = new TextColumn<FormTypeTemplate>() {
            @Override
            public String getValue(FormTypeTemplate formTypeTemplate) {
                return formTypeTemplate.getTaxType().getName();
            }
        };

        TextColumn<FormTypeTemplate> versionColumn = new TextColumn<FormTypeTemplate>() {
            @Override
            public String getValue(FormTypeTemplate formTypeTemplate) {
                return String.valueOf(formTypeTemplate.getVersionCount());
            }
        };

        linkColumn.setSortable(true);
        typeTaxColumn.setSortable(true);
        versionColumn.setSortable(true);

        sortHandler.setComparator(linkColumn, new ComparatorWithNull<FormTypeTemplate, String>() {
            @Override
            public int compare(FormTypeTemplate o1, FormTypeTemplate o2) {
                return compareWithNull(o1.getFormTypeName().replaceFirst("\\(", ""), o2.getFormTypeName().replaceFirst("\\(", ""));
            }
        });

        sortHandler.setComparator(typeTaxColumn, new ComparatorWithNull<FormTypeTemplate, String>() {
            @Override
            public int compare(FormTypeTemplate o1, FormTypeTemplate o2) {
                return compareWithNull(o1.getTaxType().getName(), o2.getTaxType().getName());
            }
        });

        sortHandler.setComparator(versionColumn, new ComparatorWithNull<FormTypeTemplate, Integer>() {
            @Override
            public int compare(FormTypeTemplate o1, FormTypeTemplate o2) {
                return compareWithNull(o1.getVersionCount(), o2.getVersionCount());
            }
        });

        formTemplateTable.addResizableColumn(linkColumn, "Наименование");
        formTemplateTable.addResizableColumn(typeTaxColumn, "Вид налога");
		formTemplateTable.addResizableColumn(versionColumn, "Количество версий");

        dataProvider.addDataDisplay(formTemplateTable);
        formTemplateTable.addColumnSortHandler(sortHandler);
        formTemplateTable.setRowCount(200, false);
	}

    @Override
    public void setInSlot(Object slot, IsWidget content) {
        if (slot == AdminPresenter.TYPE_filterPresenter) {
            filterContentPanel.clear();
            if (content != null) {
                filterContentPanel.add(content);
            }
        } else if (slot == AdminPresenter.TYPE_editPresenter) {
            editFormContentPanel.clear();
            if (content != null) {
                editFormContentPanel.add(content);
            }
        } else {
            super.setInSlot(slot, content);
        }
    }

	@Override
	public void setFormTemplateTable(List<FormTypeTemplate> formTypeTemplates) {
        dataProvider.setList(formTypeTemplates);
        formTemplateTable.setVisibleRange(0, formTypeTemplates.size());
        sortHandler.setList(dataProvider.getList());
        /* При единственном значении не перерисовывается таблица http://jira.aplana.com/browse/SBRFACCTAX-7612 */
        formTemplateTable.flush();
        selectionModel.clear();
        if (selectedItem != null) {
            for(FormTypeTemplate item: formTypeTemplates) {
                if (item.getFormTypeId() == selectedItem.getFormTypeId()) {
                    selectionModel.setSelected(item, true);
                    break;
                }
            }
        }
        if (!formTypeTemplates.isEmpty() && (selectedItem == null)) {
            selectionModel.setSelected(formTypeTemplates.get(0), true);
        }
	}

    @Override
    public FormTypeTemplate getSelectedElement() {
        return selectionModel.getSelectedObject();
    }

    @UiHandler("delete")
    void onDeleteTemplate(ClickEvent event){
        if (getUiHandlers() != null)
            getUiHandlers().onDeleteClick();
    }

    @UiHandler("create")
    void onCreateButtonClicked(ClickEvent event) {
        if (getUiHandlers() != null) {
            getUiHandlers().onCreateClicked();
        }
    }
}
