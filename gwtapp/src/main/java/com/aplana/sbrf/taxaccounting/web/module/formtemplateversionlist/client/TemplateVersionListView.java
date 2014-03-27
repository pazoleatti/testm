package com.aplana.sbrf.taxaccounting.web.module.formtemplateversionlist.client;

import com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.AdminConstants;
import com.aplana.sbrf.taxaccounting.web.module.formtemplateversionlist.shared.FormTemplateVersion;
import com.aplana.sbrf.taxaccounting.web.widget.style.GenericCellTable;
import com.aplana.sbrf.taxaccounting.web.widget.style.LinkButton;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;
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
    private SingleSelectionModel<FormTemplateVersion> selectionModel;


    @UiField
    GenericCellTable<FormTemplateVersion> ftVersionCellTable;

    @UiField
    Anchor returnAnchor;

    @UiField
    Label versionLabel;

    @UiField
    Label versionKind;

    @UiField
    LinkButton deleteVersion;

    @Inject
    public TemplateVersionListView(Binder binder) {
        initWidget(binder.createAndBindUi(this));
        selectionModel = new SingleSelectionModel<FormTemplateVersion>();
        selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                updateDeleteVersionLinkButton();
            }
        });
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

        ftVersionCellTable.addResizableColumn(linkColumn, "Наименование");

        ftVersionCellTable.addResizableColumn(new TextColumn<FormTemplateVersion>() {
            @Override
            public String getValue(FormTemplateVersion object) {
                return String.valueOf(object.getVersionNumber());
            }
        }, "Версия");

        ftVersionCellTable.addResizableColumn(new TextColumn<FormTemplateVersion>() {
            @Override
            public String getValue(FormTemplateVersion object) {
                return object.getActualBeginVersionDate();
            }
        }, "Начало актуального периода");

        ftVersionCellTable.addResizableColumn(new TextColumn<FormTemplateVersion>() {
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

    @Override
    public void setKindLabel(String kindLabel) {
        versionKind.setText(kindLabel);
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

    public void updateDeleteVersionLinkButton() {
        deleteVersion.setEnabled(getSelectedElement() != null);
    }
}
