package com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.view;

import com.aplana.sbrf.taxaccounting.model.FormTemplate;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.presenter.AdminPresenter;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.shared.FormListResult;
import com.google.gwt.cell.client.ActionCell;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

/**
 * Представление для страницы администрирования.
 *
 * @author Vitalii Samolovskikh
 */
public class AdminView extends ViewWithUiHandlers<AdminUiHandlers> implements AdminPresenter.MyView {
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

		// колонка с название типа формы
		formTemplateTable.addColumn(new TextColumn<FormTemplate>() {
			@Override
			public String getValue(FormTemplate formTemplate) {
				return formTemplate.getType().getName();
			}
		});

		// колонка с кнопкой "Изменить"
		formTemplateTable.addColumn(new Column<FormTemplate, Integer>(
			new ActionCell<Integer>("Изменить", new ActionCell.Delegate<Integer>() {
				@Override
				public void execute(Integer id) {
					getUiHandlers().selectForm(id);
				}
			})
		) {
			@Override
			public Integer getValue(FormTemplate formTemplate) {
				return formTemplate.getId();
			}
		});
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void setFormTemplateTable(FormListResult result) {
		formTemplateTable.setRowData(result.getForms());
	}

}
