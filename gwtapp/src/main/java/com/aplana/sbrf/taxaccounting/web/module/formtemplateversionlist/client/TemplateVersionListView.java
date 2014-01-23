package com.aplana.sbrf.taxaccounting.web.module.formtemplateversionlist.client;

import com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.AdminConstants;
import com.aplana.sbrf.taxaccounting.web.module.formtemplateversionlist.shared.FormTemplateVersion;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.NoSelectionModel;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

import java.util.List;

/**
 * User: avanteev
 * Date: 2013
 */
public class TemplateVersionListView extends ViewWithUiHandlers<FTVersionListUiHandlers> implements TemplateVersionListPresenter.MyView {

    interface Binder extends UiBinder<Widget, TemplateVersionListView> {
    }
    private NoSelectionModel<FormTemplateVersion> selectionModel;


    @UiField
    CellTable<FormTemplateVersion> ftVersionCellTable;

    @UiField
    Anchor returnAnchor;

    @UiField
    Label versionLabel;

    @Inject
    public TemplateVersionListView(Binder binder) {
        initWidget(binder.createAndBindUi(this));
        selectionModel = new NoSelectionModel<FormTemplateVersion>();
        ftVersionCellTable.setSelectionModel(selectionModel);

        // колонка Наименование декларации
        Column<FormTemplateVersion, FormTemplateVersion> linkColumn = new Column<FormTemplateVersion, FormTemplateVersion>(
                new AbstractCell<FormTemplateVersion>() {
                    @Override
                    public void render(Context context,
                                       FormTemplateVersion formTemplateVersion,
                                       SafeHtmlBuilder sb) {
                        if (formTemplateVersion == null) {
                            return;
                        }
                        sb.appendHtmlConstant("<a href=\"#"
                                + AdminConstants.NameTokens.formTemplateInfoPage + ";"
                                + AdminConstants.NameTokens.formTemplateId + "="
                                + formTemplateVersion.getFormTemplateId() + "\">"
                                + formTemplateVersion.getTypeName() + "</a>");
                    }
                }) {
            @Override
            public FormTemplateVersion getValue(FormTemplateVersion object) {
                return object;
            }
        };

        ftVersionCellTable.addColumn(linkColumn, "Наименование");

        ftVersionCellTable.addColumn(new TextColumn<FormTemplateVersion>() {
            @Override
            public String getValue(FormTemplateVersion object) {
                return String.valueOf(object.getVersionNumber());
            }
        }, "Версия");

        ftVersionCellTable.addColumn(new TextColumn<FormTemplateVersion>() {
            @Override
            public String getValue(FormTemplateVersion object) {
                return object.getActualBeginVersionDate();
            }
        }, "Начало актуального периода");

        ftVersionCellTable.addColumn(new TextColumn<FormTemplateVersion>() {
            @Override
            public String getValue(FormTemplateVersion object) {
                return object.getActualEndVersionDate();
            }
        }, "Окончание актуальности периода");
    }

    @Override
    public void setFTVersionTable(List<FormTemplateVersion> userFullList) {
        ftVersionCellTable.setRowData(userFullList);
    }

    @Override
    public FormTemplateVersion getSelectedElement() {
        return selectionModel.getLastSelectedObject();
    }

    @Override
    public void setLabelName(String labelName) {
        versionLabel.setTitle(labelName);
        versionLabel.setText(labelName);
    }

    @UiHandler("createVersion")
    void onCreateVersionClick(ClickEvent event){
        if (getUiHandlers() != null)
            getUiHandlers().onCreateVersion();
    }

    @UiHandler("deleteVersion")
    void onDeleteVersion(ClickEvent event){
        if (getUiHandlers() != null)
            getUiHandlers().onDeleteVersion();
    }

    @UiHandler("historyVersion")
    void onHistoryVersionClick(ClickEvent event){
        if (getUiHandlers() != null)
            getUiHandlers().onHistoryClick();
    }

    @UiHandler("returnAnchor")
    void onReturnAnchor(ClickEvent event){
        if (getUiHandlers() != null){
            getUiHandlers().onReturnClicked();
            event.preventDefault();
            event.stopPropagation();
        }

    }
}
