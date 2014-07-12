package com.aplana.sbrf.taxaccounting.web.module.formtemplateversionlist.client;

import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.gwt.client.dialog.DialogHandler;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.AdminConstants;
import com.aplana.sbrf.taxaccounting.web.module.formtemplateversionlist.shared.FormTemplateVersion;
import com.aplana.sbrf.taxaccounting.web.widget.style.GenericCellTable;
import com.aplana.sbrf.taxaccounting.web.widget.style.LinkButton;
import com.aplana.sbrf.taxaccounting.web.widget.style.table.ComparatorWithNull;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.Range;
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

    @UiField
    Anchor returnAnchor;

    @UiField
    Label versionLabel;

    @UiField
    Label versionKind;

    @UiField
    LinkButton deleteVersion;

    @UiField
    GenericCellTable<FormTemplateVersion> ftVersionCellTable;

    private SingleSelectionModel<FormTemplateVersion> selectionModel;
    private ListDataProvider<FormTemplateVersion> dataProvider;
    private ColumnSortEvent.ListHandler<FormTemplateVersion> sortHandler;

    @Inject
    public TemplateVersionListView(Binder binder) {
        initWidget(binder.createAndBindUi(this));
        selectionModel = new SingleSelectionModel<FormTemplateVersion>();
        dataProvider = new ListDataProvider<FormTemplateVersion>();
        sortHandler = new ColumnSortEvent.ListHandler<FormTemplateVersion>(dataProvider.getList());

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
                                + AdminConstants.NameTokens.formTemplateMainPage + ";"
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

        TextColumn<FormTemplateVersion> startColumn = new TextColumn<FormTemplateVersion>() {
            @Override
            public String getValue(FormTemplateVersion object) {
                return object.getActualBeginVersionDate();
            }
        };

        TextColumn<FormTemplateVersion> endColumn = new TextColumn<FormTemplateVersion>() {
            @Override
            public String getValue(FormTemplateVersion object) {
                return object.getActualEndVersionDate();
            }
        };
        linkColumn.setSortable(true);
        startColumn.setSortable(true);
        endColumn.setSortable(true);

        sortHandler.setComparator(linkColumn, new ComparatorWithNull<FormTemplateVersion, String>() {
            @Override
            public int compare(FormTemplateVersion o1, FormTemplateVersion o2) {
                return compareWithNull(o1.getTypeName(), o2.getTypeName());
            }
        });
        sortHandler.setComparator(startColumn, new ComparatorWithNull<FormTemplateVersion, String>() {
            @Override
            public int compare(FormTemplateVersion o1, FormTemplateVersion o2) {
                return compareWithNull(o1.getActualBeginVersionDate(), o2.getActualBeginVersionDate());
            }
        });
        sortHandler.setComparator(endColumn, new ComparatorWithNull<FormTemplateVersion, String>() {
            @Override
            public int compare(FormTemplateVersion o1, FormTemplateVersion o2) {
                return compareWithNull(o1.getActualEndVersionDate(), o2.getActualEndVersionDate());
            }
        });

        ftVersionCellTable.addResizableColumn(linkColumn, "Наименование");
        ftVersionCellTable.addResizableColumn(startColumn, "Начало актуального периода");
        ftVersionCellTable.addResizableColumn(endColumn, "Окончание актуальности периода");

        dataProvider.addDataDisplay(ftVersionCellTable);

        ftVersionCellTable.addColumnSortHandler(sortHandler);
    }

    @Override
    public void setFTVersionTable(List<FormTemplateVersion> userFullList) {
        dataProvider.setList(userFullList);
        ftVersionCellTable.setVisibleRange(new Range(0, userFullList.size()));
        sortHandler.setList(dataProvider.getList());
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
        Dialog.confirmMessage("Удаление версии макета", "Вы подтверждаете удаление версии макета?", new DialogHandler() {
            @Override
            public void yes() {
                if (getUiHandlers() != null)
                    getUiHandlers().onDeleteVersion();
            }
        });
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
