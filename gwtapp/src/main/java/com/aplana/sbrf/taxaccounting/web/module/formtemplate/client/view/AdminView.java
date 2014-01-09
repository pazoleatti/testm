package com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.view;

import com.aplana.sbrf.taxaccounting.model.FormTemplate;
import com.aplana.sbrf.taxaccounting.model.VersionedObjectStatus;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.AdminConstants;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.presenter.AdminPresenter;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.NoSelectionModel;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewImpl;

import java.util.List;

/**
 * Представление для страницы администрирования.
 *
 * @author Vitalii Samolovskikh
 * @author Eugene Stetsenko
 */
public class AdminView extends ViewImpl implements AdminPresenter.MyView {
	interface Binder extends UiBinder<Widget, AdminView> {
	}

    @UiField
    Panel filterContentPanel;

	@UiField
	CellTable<FormTemplate> formTemplateTable;

    private NoSelectionModel<FormTemplate> selectionModel;

	@Inject
	public AdminView(Binder binder) {
		initWidget(binder.createAndBindUi(this));

        selectionModel = new NoSelectionModel<FormTemplate>();

		// колонка Наименование декларации
		Column<FormTemplate, FormTemplate> linkColumn = new Column<FormTemplate, FormTemplate>(
				new AbstractCell<FormTemplate>() {
					@Override
					public void render(Context context,
									   FormTemplate formTemplate,
									   SafeHtmlBuilder sb) {
						if (formTemplate == null) {
							return;
						}
						sb.appendHtmlConstant("<a href=\"#"
								+ AdminConstants.NameTokens.formTemplateVersionList + ";"
								+ AdminConstants.NameTokens.formTypeId + "="
								+ formTemplate.getId() + "\">"
								+ formTemplate.getType().getName() + "</a>");
					}
				}) {
			@Override
			public FormTemplate getValue(FormTemplate object) {
				return object;
			}
		};
        formTemplateTable.setSelectionModel(selectionModel);
		formTemplateTable.addColumn(linkColumn, "Наименование");

		formTemplateTable.addColumn(new Column<FormTemplate, Boolean>(
				new CheckboxCell()) {
			@Override
			public Boolean getValue(FormTemplate formTemplate) {
				return formTemplate.getStatus() == VersionedObjectStatus.NORMAL;
			}
		}, "Активен");

		formTemplateTable.addColumn(new TextColumn<FormTemplate>() {
			@Override
			public String getValue(FormTemplate formTemplate) {
				return String.valueOf(formTemplate.getEdition());
			}
		}, "Версия");
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
	public void setFormTemplateTable(List<FormTemplate> formTemplates) {
		formTemplateTable.setRowData(formTemplates);
	}

    @Override
    public FormTemplate getSelectedElement() {
        return selectionModel.getLastSelectedObject();
    }

}
