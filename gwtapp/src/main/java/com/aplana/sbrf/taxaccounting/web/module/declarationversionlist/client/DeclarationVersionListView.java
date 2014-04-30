package com.aplana.sbrf.taxaccounting.web.module.declarationversionlist.client;

import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.gwt.client.dialog.DialogHandler;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.client.DeclarationTemplateTokens;
import com.aplana.sbrf.taxaccounting.web.module.declarationversionlist.shared.DeclarationTemplateVersion;
import com.aplana.sbrf.taxaccounting.web.widget.style.GenericCellTable;
import com.aplana.sbrf.taxaccounting.web.widget.style.LinkButton;
import com.aplana.sbrf.taxaccounting.web.widget.style.table.ComparatorWithNull;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.cellview.client.TextColumn;
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
 */
public class DeclarationVersionListView extends ViewWithUiHandlers<DTVersionListUIHandlers> implements DeclarationVersionListPresenter.MyView{

    private static final DateTimeFormat SDF = DateTimeFormat.getFormat("dd.MM.yyyy");

    interface Binder extends UiBinder<Widget, DeclarationVersionListView> {
    }

    private SingleSelectionModel<DeclarationTemplateVersion> selectionModel;

    @UiField
    GenericCellTable<DeclarationTemplateVersion> dtVersionCellTable;

    @UiField
    Label versionLabel;

    @UiField
    LinkButton deleteVersion;

    private ListDataProvider<DeclarationTemplateVersion> dataProvider = new ListDataProvider<DeclarationTemplateVersion>();
    private ColumnSortEvent.ListHandler<DeclarationTemplateVersion> dataSortHandler;

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
        dataProvider.addDataDisplay(dtVersionCellTable);

        // колонка Наименование декларации
        Column<DeclarationTemplateVersion, DeclarationTemplateVersion> linkColumn = new Column<DeclarationTemplateVersion, DeclarationTemplateVersion>(
                new AbstractCell<DeclarationTemplateVersion>() {
                    @Override
                    public void render(Context context,DeclarationTemplateVersion declarationTemplateVersion,
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
        TextColumn<DeclarationTemplateVersion> versionColumn = new TextColumn<DeclarationTemplateVersion>() {
            @Override
            public String getValue(DeclarationTemplateVersion object) {
                return String.valueOf(object.getVersionNumber());
            }
        };
        TextColumn<DeclarationTemplateVersion> startColumn = new TextColumn<DeclarationTemplateVersion>() {
            @Override
            public String getValue(DeclarationTemplateVersion object) {
                return object.getActualBeginVersionDate();
            }
        };
        TextColumn<DeclarationTemplateVersion> endColumn = new TextColumn<DeclarationTemplateVersion>() {
            @Override
            public String getValue(DeclarationTemplateVersion object) {
                return object.getActualEndVersionDate();
            }
        };
        linkColumn.setSortable(true);
        versionColumn.setSortable(true);
        startColumn.setSortable(true);
        endColumn.setSortable(true);

        dtVersionCellTable.addResizableColumn(linkColumn, "Наименование");
        dtVersionCellTable.addResizableColumn(versionColumn, "Версия");
        dtVersionCellTable.addResizableColumn(startColumn, "Начало актуального периода");
        dtVersionCellTable.addResizableColumn(endColumn, "Окончание актуальности периода");

        dtVersionCellTable.setColumnWidth(linkColumn, 50, Style.Unit.PCT);
        dtVersionCellTable.setColumnWidth(versionColumn, 10, Style.Unit.PCT);
        dtVersionCellTable.setColumnWidth(startColumn, 20, Style.Unit.PCT);
        dtVersionCellTable.setColumnWidth(endColumn, 20, Style.Unit.PCT);

        dataSortHandler = new ColumnSortEvent.ListHandler<DeclarationTemplateVersion>(dataProvider.getList());
        dataSortHandler.setComparator(linkColumn, new ComparatorWithNull<DeclarationTemplateVersion, String>() {
            @Override
            public int compare(DeclarationTemplateVersion o1, DeclarationTemplateVersion o2) {
                return compareWithNull(o1.getTypeName(), o2.getTypeName());
            }
        });
        dataSortHandler.setComparator(versionColumn, new ComparatorWithNull<DeclarationTemplateVersion, String>() {
            @Override
            public int compare(DeclarationTemplateVersion o1, DeclarationTemplateVersion o2) {
                return compareWithNull(o1.getVersionNumber(), o2.getVersionNumber());
            }
        });
        dataSortHandler.setComparator(startColumn, new ComparatorWithNull<DeclarationTemplateVersion, String>() {
            @Override
            public int compare(DeclarationTemplateVersion o1, DeclarationTemplateVersion o2) {
                return compareWithNull(o1.getActualBeginVersionDate(), o2.getActualBeginVersionDate());
            }
        });
        dataSortHandler.setComparator(endColumn, new ComparatorWithNull<DeclarationTemplateVersion, String>() {
            @Override
            public int compare(DeclarationTemplateVersion o1, DeclarationTemplateVersion o2) {
                return compareWithNull(o1.getActualEndVersionDate(), o2.getActualEndVersionDate());
            }
        });

        dtVersionCellTable.addColumnSortHandler(dataSortHandler);
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
        dataProvider.setList(fullList);
        dtVersionCellTable.setVisibleRange(new Range(0, fullList.size()));
        dataSortHandler.setList(dataProvider.getList());
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
        Dialog.confirmMessage("Удаление версии макета", "Вы подтверждаете удаление версии макета?", new DialogHandler() {
            @Override
            public void yes() {
                if (getUiHandlers() != null)
                    getUiHandlers().onDeleteVersion();
            }
        });
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
