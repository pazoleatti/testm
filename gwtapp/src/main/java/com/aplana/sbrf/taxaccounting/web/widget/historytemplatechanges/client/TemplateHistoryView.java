package com.aplana.sbrf.taxaccounting.web.widget.historytemplatechanges.client;

import com.aplana.sbrf.taxaccounting.model.VersionHistorySearchOrdering;
import com.aplana.sbrf.taxaccounting.web.main.api.client.AplanaUiHandlers;
import com.aplana.sbrf.taxaccounting.web.main.api.client.sortable.AsyncDataProviderWithSortableTable;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.client.DeclarationTemplateTokens;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.AdminConstants;
import com.aplana.sbrf.taxaccounting.web.widget.historytemplatechanges.shared.TemplateChangesExt;
import com.aplana.sbrf.taxaccounting.web.widget.style.GenericDataGrid;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.PopupViewWithUiHandlers;

import java.util.List;

/**
 * @author Fail Mukhametdinov
 */
public class TemplateHistoryView extends PopupViewWithUiHandlers<AplanaUiHandlers>
        implements AbstractTemplateHistoryPresenter.MyView {

    @UiTemplate("HistoryView.ui.xml")
    interface Binder extends UiBinder<PopupPanel, TemplateHistoryView> {
    }

    protected static final DateTimeFormat format = DateTimeFormat.getFormat("dd.MM.yyyy HH:mm");
    protected AsyncDataProviderWithSortableTable dataProvider;
    protected VersionHistorySearchOrdering sortByColumn;
    @UiField
    GenericDataGrid<TemplateChangesExt> versionHistoryCellTable;

    protected TemplateHistoryView(EventBus eventBus) {
        super(eventBus);
    }

    @Inject
    public TemplateHistoryView(EventBus eventBus, Binder binder) {
        super(eventBus);
        initWidget(binder.createAndBindUi(this));
        setTableColumns();

        dataProvider = new AsyncDataProviderWithSortableTable(versionHistoryCellTable, this) {
            @Override
            public AplanaUiHandlers getUiHandlersX() {
                return getUiHandlers();
            }
        };
    }

    @Override
    public void fillTemplate(List<TemplateChangesExt> templateChangesExts) {
        versionHistoryCellTable.setRowData(templateChangesExts);
    }

    @Override
    public VersionHistorySearchOrdering getSearchOrdering() {
        if (sortByColumn == null) {
            sortByColumn = VersionHistorySearchOrdering.DATE;
        }
        return sortByColumn;
    }

    @Override
    public boolean isAscSorting() {
        return dataProvider.isAscSorting();
    }

    @Override
    public void setSortByColumn(String sortByColumn) {
        this.sortByColumn = VersionHistorySearchOrdering.valueOf(sortByColumn);
    }

    @UiHandler("hideButton")
    public void onHideButton(ClickEvent event) {
        hide();
    }

    private void setTableColumns() {
        // колонка Наименование декларации
        Column<TemplateChangesExt, TemplateChangesExt> linkColumn = new Column<TemplateChangesExt, TemplateChangesExt>(
                new AbstractCell<TemplateChangesExt>() {
                    @Override
                    public void render(Context context,
                                       TemplateChangesExt templateChanges,
                                       SafeHtmlBuilder sb) {
                        if (templateChanges == null) {
                            return;
                        }
                        String url = templateChanges.getTemplateChanges().getFormTemplateId() != null ?
                                AdminConstants.NameTokens.formTemplateInfoPage + ";" + AdminConstants.NameTokens.formTemplateId + "="
                                        + templateChanges.getTemplateChanges().getFormTemplateId() + "\">" + templateChanges.getTemplateChanges().getFormTemplateId() + "</a>" :
                                DeclarationTemplateTokens.declarationTemplate + ";" + DeclarationTemplateTokens.declarationTemplateId + "="
                                        + templateChanges.getTemplateChanges().getDeclarationTemplateId() + "\">" + templateChanges.getTemplateChanges().getDeclarationTemplateId() + "</a>";
                        sb.appendHtmlConstant("<a href=\"#" + url + "</a>");
                    }
                }) {
            @Override
            public TemplateChangesExt getValue(TemplateChangesExt object) {
                return object;
            }
        };

        TextColumn<TemplateChangesExt> eventColumn = new TextColumn<TemplateChangesExt>() {
            @Override
            public String getValue(TemplateChangesExt object) {
                return String.valueOf(object.getTemplateChanges().getEvent().getTitle());
            }
        };

        TextColumn<TemplateChangesExt> dateColumn = new TextColumn<TemplateChangesExt>() {
            @Override
            public String getValue(TemplateChangesExt object) {
                return format.format(object.getTemplateChanges().getEventDate());
            }
        };

        TextColumn<TemplateChangesExt> userColumn = new TextColumn<TemplateChangesExt>() {
            @Override
            public String getValue(TemplateChangesExt object) {
                return object.getTemplateChanges().getAuthor().getName();
            }
        };


        versionHistoryCellTable.addResizableColumn(linkColumn, "Версия");
        versionHistoryCellTable.addResizableColumn(eventColumn, "Событие");
        versionHistoryCellTable.addResizableColumn(dateColumn, "Дата и время события");
        versionHistoryCellTable.addResizableColumn(userColumn, "Инициатор");

        linkColumn.setDataStoreName(VersionHistorySearchOrdering.VERSION.name());
        eventColumn.setDataStoreName(VersionHistorySearchOrdering.EVENT.name());
        dateColumn.setDataStoreName(VersionHistorySearchOrdering.DATE.name());
        userColumn.setDataStoreName(VersionHistorySearchOrdering.USER.name());
    }
}
