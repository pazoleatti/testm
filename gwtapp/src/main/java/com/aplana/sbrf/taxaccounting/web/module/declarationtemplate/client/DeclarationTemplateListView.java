package com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.client;

import com.aplana.sbrf.taxaccounting.model.DeclarationTemplate;
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
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewImpl;

import java.util.List;

/**
 * Представление для страницы администрирования деклараций.
 *
 */
public class DeclarationTemplateListView extends ViewImpl
		implements DeclarationTemplateListPresenter.MyView {
	interface Binder extends UiBinder<Widget, DeclarationTemplateListView> {
	}
	
	/**
	 * Список шаблонов деклараций
	 */
	@UiField
	CellTable<DeclarationTemplate> declarationTemplateTable;
    @UiField
    Panel filterContentPanel;

    @Inject
	public DeclarationTemplateListView(Binder binder) {
		initWidget(binder.createAndBindUi(this));

		// колонка Наименование декларации
		Column<DeclarationTemplate, DeclarationTemplate> linkColumn = new Column<DeclarationTemplate, DeclarationTemplate>(
				new AbstractCell<DeclarationTemplate>() {
					@Override
					public void render(Context context,
									   DeclarationTemplate declaration,
									   SafeHtmlBuilder sb) {
						if (declaration == null) {
							return;
						}
						sb.appendHtmlConstant("<a href=\"#"
								+ DeclarationTemplateTokens.declarationTemplate + ";"
								+ DeclarationTemplateTokens.declarationTemplateId + "="
								+ declaration.getId() + "\">"
								+ declaration.getDeclarationType().getName() + "</a>");
					}
				}) {
			@Override
			public DeclarationTemplate getValue(DeclarationTemplate object) {
				return object;
			}
		};
		declarationTemplateTable.addColumn(linkColumn, "Наименование");

		declarationTemplateTable.addColumn(new Column<DeclarationTemplate, Boolean>(
				new CheckboxCell()) {
			@Override
			public Boolean getValue(DeclarationTemplate declarationTemplate) {
				return declarationTemplate.isActive();
			}
		}, "Активен");

		declarationTemplateTable.addColumn(new TextColumn<DeclarationTemplate>() {
			@Override
			public String getValue(DeclarationTemplate declarationTemplate) {
				return declarationTemplate.getVersion();
			}
		}, "Версия");
	}

	@Override
	public void setDeclarationTemplateRows(List<DeclarationTemplate> templates) {
		declarationTemplateTable.setRowData(templates);
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

}
