package com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.view;

import java.util.List;


import com.aplana.sbrf.taxaccounting.model.FormTemplate;
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
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewImpl;

/**
 * Представление для страницы администрирования.
 *
 * @author Vitalii Samolovskikh
 */
public class AdminView extends ViewImpl implements AdminPresenter.MyView {
	interface Binder extends UiBinder<Widget, AdminView> {
	}

	private final Widget widget;

	/**
	 * Список шаблонов форм. А больше здесь нифига нет.
	 */
	@UiField
	CellTable<FormTemplate> formTemplateTable;

	@Inject
	public AdminView(Binder binder) {
		widget = binder.createAndBindUi(this);

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
								+ AdminConstants.NameTokens.formTemplateInfoPage + ";"
								+ AdminConstants.NameTokens.formTemplateId + "="
								+ formTemplate.getId() + "\">"
								+ formTemplate.getType().getName() + "</a>");
					}
				}) {
			@Override
			public FormTemplate getValue(FormTemplate object) {
				return object;
			}
		};
		formTemplateTable.addColumn(linkColumn, "Наименование");

		formTemplateTable.addColumn(new Column<FormTemplate, Boolean>(
				new CheckboxCell()) {
			@Override
			public Boolean getValue(FormTemplate formTemplate) {
				return formTemplate.isActive();
			}
		}, "Активен");

		formTemplateTable.addColumn(new TextColumn<FormTemplate>() {
			@Override
			public String getValue(FormTemplate formTemplate) {
				return formTemplate.getVersion();
			}
		}, "Версия");
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void setFormTemplateTable(List<FormTemplate> formTemplates) {
		formTemplateTable.setRowData(formTemplates);
	}

}
