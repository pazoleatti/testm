package com.aplana.sbrf.taxaccounting.web.module.formtemplateversionlist.client.history;

import com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.AdminConstants;
import com.aplana.sbrf.taxaccounting.web.module.formtemplateversionlist.shared.TemplateChangesExt;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.PopupViewImpl;

import java.util.List;

/**
 * User: avanteev
 */
public class VersionHistoryView extends PopupViewImpl implements VersionHistoryPresenter.MyView {

    @UiField
    CellTable<TemplateChangesExt> versionHistoryCellTable;

    @Override
    public void fillTemplate(List<TemplateChangesExt> templateChangeses) {
        versionHistoryCellTable.setRowData(templateChangeses);
    }

    interface Binder extends UiBinder<PopupPanel, VersionHistoryView> {
    }

    @Inject
    public VersionHistoryView(EventBus eventBus, Binder binder) {
        super(eventBus);
        initWidget(binder.createAndBindUi(this));

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
                        sb.appendHtmlConstant("<a href=\"#"
                                + AdminConstants.NameTokens.formTemplateInfoPage + ";"
                                + AdminConstants.NameTokens.formTemplateId + "="
                                + templateChanges.getTemplateChanges().getFormTemplateId() + "\">"
                                + templateChanges.getEdition() + "</a>");
                    }
                }) {
            @Override
            public TemplateChangesExt getValue(TemplateChangesExt object) {
                return object;
            }
        };

        versionHistoryCellTable.addColumn(linkColumn, "Версия");

        versionHistoryCellTable.addColumn(new TextColumn<TemplateChangesExt>() {
            @Override
            public String getValue(TemplateChangesExt object) {
                return String.valueOf(object.getTemplateChanges().getEvent());
            }
        }, "Событие");

        versionHistoryCellTable.addColumn(new TextColumn<TemplateChangesExt>() {
            @Override
            public String getValue(TemplateChangesExt object) {
                return object.getTemplateChanges().getEventDate().toString();
            }
        }, "Дата и время события");

        versionHistoryCellTable.addColumn(new TextColumn<TemplateChangesExt>() {
            @Override
            public String getValue(TemplateChangesExt object) {
                return object.getTemplateChanges().getAuthor().getName();
            }
        }, "Инициатор");
    }

    @UiHandler("hideButton")
    void onHideButton(ClickEvent event){
        hide();
    }
}
