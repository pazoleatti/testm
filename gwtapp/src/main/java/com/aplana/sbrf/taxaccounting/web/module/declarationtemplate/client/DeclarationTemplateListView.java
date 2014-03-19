package com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.client;

import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.shared.DeclarationTypeTemplate;
import com.aplana.sbrf.taxaccounting.web.widget.style.GenericCellTable;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
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

    private NoSelectionModel<DeclarationTypeTemplate> selectionModel;

    @Inject
	public DeclarationTemplateListView(Binder binder) {
		initWidget(binder.createAndBindUi(this));

        selectionModel = new NoSelectionModel<DeclarationTypeTemplate>();

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
		declarationTemplateTable.addResizableColumn(linkColumn, "Наименование");

		declarationTemplateTable.addResizableColumn(new TextColumn<DeclarationTypeTemplate>() {
			@Override
			public String getValue(DeclarationTypeTemplate declarationTypeTemplate) {
				return String.valueOf(declarationTypeTemplate.getVersionCount());
			}
		}, "Версий");
        declarationTemplateTable.setSelectionModel(selectionModel);
	}

	@Override
	public void setDeclarationTypeTemplateRows(List<DeclarationTypeTemplate> result) {
		declarationTemplateTable.setRowData(result);
	}

    @Override
    public DeclarationTypeTemplate getSelectedElement() {
        return selectionModel.getLastSelectedObject();
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
