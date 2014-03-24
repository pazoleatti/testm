package com.aplana.sbrf.taxaccounting.web.module.declarationversionlist.client;

import com.aplana.sbrf.taxaccounting.web.module.declarationversionlist.shared.DeclarationTemplateVersion;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.client.DeclarationTemplateTokens;
import com.aplana.sbrf.taxaccounting.web.widget.style.GenericCellTable;
import com.aplana.sbrf.taxaccounting.web.widget.style.LinkButton;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

import java.util.List;

/**
 * User: avanteev
 */
public class DeclarationVersionListView extends ViewWithUiHandlers<DTVersionListUIHandlers> implements DeclarationVersionListPresenter.MyView{
    interface Binder extends UiBinder<Widget, DeclarationVersionListView> {
    }

    private SingleSelectionModel<DeclarationTemplateVersion> selectionModel;

    @UiField
    GenericCellTable<DeclarationTemplateVersion> dtVersionCellTable;

    @UiField
    Label versionLabel;

    @UiField
    LinkButton deleteVersion;

    @Inject
    public DeclarationVersionListView(Binder binder) {
        initWidget(binder.createAndBindUi(this));
        selectionModel = new SingleSelectionModel<DeclarationTemplateVersion>();
        selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                updateDeleteVersionLinkButton();
            }
        });
        dtVersionCellTable.setSelectionModel(selectionModel);

        // колонка Наименование декларации
        Column<DeclarationTemplateVersion, DeclarationTemplateVersion> linkColumn = new Column<DeclarationTemplateVersion, DeclarationTemplateVersion>(
                new AbstractCell<DeclarationTemplateVersion>() {
                    @Override
                    public void render(Context context,
                                       DeclarationTemplateVersion declarationTemplateVersion,
                                       SafeHtmlBuilder sb) {
                        if (declarationTemplateVersion == null) {
                            return;
                        }
                        sb.appendHtmlConstant("<a href=\"#"
                                + DeclarationTemplateTokens.declarationTemplate + ";"
                                + DeclarationTemplateTokens.declarationTemplateId + "="
                                + declarationTemplateVersion.getDtId() + "\">"
                                + declarationTemplateVersion.getTypeName() + "</a>");
                    }
                }) {
            @Override
            public DeclarationTemplateVersion getValue(DeclarationTemplateVersion object) {
                return object;
            }
        };

        dtVersionCellTable.addResizableColumn(linkColumn, "Наименование");

        dtVersionCellTable.addResizableColumn(new TextColumn<DeclarationTemplateVersion>() {
            @Override
            public String getValue(DeclarationTemplateVersion object) {
                return String.valueOf(object.getVersionNumber());
            }
        }, "Версия");

        dtVersionCellTable.addResizableColumn(new TextColumn<DeclarationTemplateVersion>() {
            @Override
            public String getValue(DeclarationTemplateVersion object) {
                return object.getActualBeginVersionDate();
            }
        }, "Начало актуального периода");

        dtVersionCellTable.addResizableColumn(new TextColumn<DeclarationTemplateVersion>() {
            @Override
            public String getValue(DeclarationTemplateVersion object) {
                return object.getActualEndVersionDate();
            }
        }, "Окончание актуальности периода");
    }

    @UiHandler("returnAnchor")
    void onReturnAnchor(ClickEvent event){
        if (getUiHandlers() != null){
            getUiHandlers().onReturnClicked();
            event.preventDefault();
            event.stopPropagation();
        }

    }

    @Override
    public void setDTVersionTable(List<DeclarationTemplateVersion> fullList) {
        dtVersionCellTable.setRowData(fullList);
    }

    @Override
    public DeclarationTemplateVersion getSelectedElement() {
        return selectionModel.getSelectedObject();
    }

    @Override
    public void setLabelName(String labelName) {
        versionLabel.setTitle(labelName);
        versionLabel.setText(labelName);
    }

    @Override
    public void resetSelectedLine() {
        selectionModel.clear();
    }

    @UiHandler("createVersion")
    void onCreateVersionClick(ClickEvent event){
        if (getUiHandlers() != null)
            getUiHandlers().onCreateVersion();
    }

    @UiHandler("deleteVersion")
    void onDeleteClick(ClickEvent event){
        if (getUiHandlers() != null)
            getUiHandlers().onDeleteVersion();
    }

    @UiHandler("historyVersion")
    void onHistoryClick(ClickEvent event){
        if (getUiHandlers() != null)
            getUiHandlers().onHistoryClick();
    }

    public void updateDeleteVersionLinkButton() {
        deleteVersion.setEnabled(getSelectedElement() != null);
    }
}
