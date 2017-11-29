package com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.client;

import com.aplana.sbrf.taxaccounting.model.DeclarationTemplateCheck;
import com.aplana.sbrf.taxaccounting.web.widget.style.GenericDataGrid;
import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.dom.client.Style;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.DefaultSelectionEventManager;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.ProvidesKey;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

import java.util.*;

public class DeclarationTemplateChecksView extends ViewWithUiHandlers<DeclarationTemplateChecksUiHandlers>
		implements DeclarationTemplateChecksPresenter.MyView {

    public interface Binder extends UiBinder<Widget, DeclarationTemplateChecksView> { }

    public static final String CODE_TITLE = "Код";
    public static final String TYPE_TITLE = "Тип";
    public static final String DESCRIPTION_TITLE = "Описание";

    @UiField
    GenericDataGrid<DeclarationTemplateCheck> checksGrid;

    @Inject
	@UiConstructor
	public DeclarationTemplateChecksView(Binder binder) {
        initWidget(binder.createAndBindUi(this));
        initGrid();
	}

    final MultiSelectionModel<DeclarationTemplateCheck> selectionModel = new MultiSelectionModel<DeclarationTemplateCheck>(
            new ProvidesKey<DeclarationTemplateCheck>() {
                @Override
                public Object getKey(DeclarationTemplateCheck item) {
                    return item.getId();
                }
            }
    );

    /**
     * Подготовка таблицы
     */
    private void initGrid() {
        Column<DeclarationTemplateCheck, Boolean> fatalityColumn = new Column<DeclarationTemplateCheck, Boolean>(
                new CheckboxCell(true, false)) {
            @Override
            public Boolean getValue(DeclarationTemplateCheck item) {
                return selectionModel.isSelected(item);
            }
        };

        TextColumn<DeclarationTemplateCheck> codeColumn = new TextColumn<DeclarationTemplateCheck>() {
            @Override
            public String getValue(DeclarationTemplateCheck item) {
                return item.getCode().getCode();
            }
        };

        TextColumn<DeclarationTemplateCheck> typeColumn = new TextColumn<DeclarationTemplateCheck>() {
            @Override
            public String getValue(DeclarationTemplateCheck item) {
                return item.getCheckType();
            }
        };

        TextColumn<DeclarationTemplateCheck> descriptionColumn = new TextColumn<DeclarationTemplateCheck>() {
            @Override
            public String getValue(DeclarationTemplateCheck item) {
                return item.getDescription();
            }
        };

        checksGrid.addColumn(fatalityColumn);
        checksGrid.setColumnWidth(fatalityColumn, 10, Style.Unit.PX);

        checksGrid.addResizableColumn(codeColumn, CODE_TITLE);
        checksGrid.setColumnWidth(codeColumn, 30, Style.Unit.PX);

        checksGrid.addResizableColumn(typeColumn, TYPE_TITLE);
        checksGrid.setColumnWidth(typeColumn, 80, Style.Unit.PX);

        checksGrid.addResizableColumn(descriptionColumn, DESCRIPTION_TITLE);
        checksGrid.setColumnWidth(descriptionColumn, 80, Style.Unit.PX);

        checksGrid.setSelectionModel(selectionModel, DefaultSelectionEventManager
                .<DeclarationTemplateCheck>createCheckboxManager());
    }

    @Override
    public void setTableData(List<DeclarationTemplateCheck> checks) {
        selectionModel.clear();
        checksGrid.setRowData(checks);
        for(DeclarationTemplateCheck check: checks) {
            if (check.isFatal()) {
                selectionModel.setSelected(check, true);
            }
        }
    }

    @Override
    public Set<DeclarationTemplateCheck> getFatalChecks() {
        return selectionModel.getSelectedSet();
    }
}
