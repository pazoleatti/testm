package com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.view;

import com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.AdminConstants;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.presenter.AdminPresenter;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.presenter.AdminUIHandlers;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.shared.FormTypeTemplate;
import com.aplana.sbrf.taxaccounting.web.widget.style.GenericCellTable;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.NoSelectionModel;
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
    GenericCellTable<FormTypeTemplate> formTemplateTable;

    private NoSelectionModel<FormTypeTemplate> selectionModel;

	@Inject
	public AdminView(Binder binder) {
		initWidget(binder.createAndBindUi(this));

        selectionModel = new NoSelectionModel<FormTypeTemplate>();

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
        formTemplateTable.setSelectionModel(selectionModel);
		formTemplateTable.addResizableColumn(linkColumn, "Наименование");

        formTemplateTable.addResizableColumn(new TextColumn<FormTypeTemplate>() {
            @Override
            public String getValue(FormTypeTemplate formTypeTemplate) {
                return formTypeTemplate.getTaxType().getName();
            }
        }, "Вид налога");

		formTemplateTable.addResizableColumn(new TextColumn<FormTypeTemplate>() {
			@Override
			public String getValue(FormTypeTemplate formTypeTemplate) {
				return String.valueOf(formTypeTemplate.getVersionCount());
			}
		}, "Версий");
	}

    @Override
    public void setInSlot(Object slot, IsWidget content) {
        if (slot == AdminPresenter.TYPE_filterPresenter) {
            filterContentPanel.clear();
            if (content != null) {
                filterContentPanel.add(content);
            }
        } else {
            super.setInSlot(slot, content);
        }
    }

	@Override
	public void setFormTemplateTable(List<FormTypeTemplate> formTypeTemplates) {
		formTemplateTable.setRowData(formTypeTemplates);
	}

    @Override
    public FormTypeTemplate getSelectedElement() {
        return selectionModel.getLastSelectedObject();
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
