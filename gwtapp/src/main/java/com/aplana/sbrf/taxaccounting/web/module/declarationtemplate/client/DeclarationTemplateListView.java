package com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.client;

import com.aplana.sbrf.taxaccounting.model.DeclarationTemplate;
import com.google.gwt.cell.client.ActionCell;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Widget;
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

	private final Widget widget;

	/**
	 * Список шаблонов деклараций
	 */
	@UiField
	CellTable<DeclarationTemplate> declarationTemplateTable;

	@Inject
	public DeclarationTemplateListView(Binder binder) {
		widget = binder.createAndBindUi(this);

		// колонка с название декларации
		declarationTemplateTable.addColumn(new TextColumn<DeclarationTemplate>() {
			@Override
			public String getValue(DeclarationTemplate declarationTemplate) {
				return declarationTemplate.getId().toString();
			}
		});

		// колонка с кнопкой "Изменить"
		declarationTemplateTable.addColumn(new Column<DeclarationTemplate, Integer>(
				new ActionCell<Integer>("Изменить", new ActionCell.Delegate<Integer>() {
					@Override
					public void execute(Integer id) {
						getUiHandlers().selectDeclaration(id);
					}
				})
		) {
			@Override
			public Integer getValue(DeclarationTemplate declarationTemplate) {
				return declarationTemplate.getId();
			}
		});
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void setDeclarationTemplateRows(List<DeclarationTemplate> templates) {
		declarationTemplateTable.setRowData(templates);
	}

}
