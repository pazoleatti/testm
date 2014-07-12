package com.aplana.sbrf.taxaccounting.web.widget.historytemplatechanges.client;

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
    GenericDataGrid<TemplateChangesExt> versionHistoryCellTable;

    @Override
    public void fillTemplate(List<TemplateChangesExt> templateChangeses) {
        versionHistoryCellTable.setRowData(templateChangeses);
    }

    interface Binder extends UiBinder<PopupPanel, VersionHistoryView> {
    }

    private static final DateTimeFormat format = DateTimeFormat.getFormat("dd.MM.yyyy HH:mm");

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
                        String url = templateChanges.getTemplateChanges().getFormTemplateId() != null ?
                                AdminConstants.NameTokens.formTemplateInfoPage + ";" + AdminConstants.NameTokens.formTemplateId + "="
                                + templateChanges.getTemplateChanges().getFormTemplateId() + "\">"  + templateChanges.getTemplateChanges().getFormTemplateId() + "</a>":
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

        versionHistoryCellTable.addResizableColumn(linkColumn, "Версия");

        versionHistoryCellTable.addResizableColumn(new TextColumn<TemplateChangesExt>() {
            @Override
            public String getValue(TemplateChangesExt object) {
                return String.valueOf(object.getTemplateChanges().getEvent().getName());
            }
        }, "Событие");

        versionHistoryCellTable.addResizableColumn(new TextColumn<TemplateChangesExt>() {
            @Override
            public String getValue(TemplateChangesExt object) {
                return format.format(object.getTemplateChanges().getEventDate());
            }
        }, "Дата и время события");

        versionHistoryCellTable.addResizableColumn(new TextColumn<TemplateChangesExt>() {
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
