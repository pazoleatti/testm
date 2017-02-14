package com.aplana.sbrf.taxaccounting.web.module.formdata.client.sources;

import com.aplana.gwt.client.ModalWindow;
import com.aplana.sbrf.taxaccounting.model.Months;
import com.aplana.sbrf.taxaccounting.model.Relation;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.client.DeclarationDataTokens;
import com.aplana.sbrf.taxaccounting.web.module.formdata.client.FormDataPresenter;
import com.aplana.sbrf.taxaccounting.web.widget.style.LinkButton;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.ClickableTextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.Range;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.PopupViewWithUiHandlers;

import java.util.LinkedList;
import java.util.List;

/**
 * Представление попапа окна с инфомацией об источниках приемниках,
 * данное окно вызывается с формы нф
 *
 * @author auldanov
 */
public class SourcesView extends PopupViewWithUiHandlers<SourcesUiHandlers> implements SourcesPresenter.MyView{

    public interface Binder extends UiBinder<PopupPanel, SourcesView> {
    }

    private List<Relation> tableData = null;

    private final PopupPanel widget;

    private static final DateTimeFormat DATE_TIME_FORMAT = DateTimeFormat.getFormat("dd.MM.yyyy");
    public static final String TITLE_FORM = "Формы источники/приемники";
    public static final String TITLE_DEC = "Налоговые формы приемники";
    public static final String TITLE_DEC_DEAL = "Уведомления приемники";

    interface UrlTemplates extends SafeHtmlTemplates {

        @Template("{0}{1}")
        SafeHtml getColValue(String main, String optional);
    }
    private static final UrlTemplates urlTemplates = GWT.create(UrlTemplates.class);

    @UiField
    Button close;
    @UiField
    ModalWindow modalWindow;
    @UiField
    DataGrid<Relation> table;
    @UiField
    LinkButton formDecAnchor;
    @UiField
    Label formDecLabel;
    @UiField
    CheckBox source;
    @UiField
    CheckBox destination;
    @UiField
    CheckBox uncreated;
    @UiField
    HTML verSep;

    private boolean isForm;
    private ListDataProvider<Relation> dataProvider = new ListDataProvider<Relation>();

    @Inject
    public SourcesView(Binder uiBinder, EventBus eventBus) {
        super(eventBus);
        widget = uiBinder.createAndBindUi(this);
        widget.setAnimationEnabled(true);
        isForm = true;
        if (getUiHandlers() != null) {
            initColumns();
        }
        table.setRowCount(0);
        dataProvider.addDataDisplay(table);
        initCheckboxes();
    }

    @Override
    public Widget asWidget() {
        return widget;
    }

    @UiHandler("close")
    public void onCloseClicked(ClickEvent event){
        modalWindow.hide();
    }

    @UiHandler("formDecAnchor")
    public void changeView(ClickEvent event){
        isForm = !isForm;
        if (getUiHandlers() != null) {
            initColumns();
            updateSwitchMode();
            updateTableData();
        }
    }

    @UiHandler("source")
    public void onSourceClicked(ClickEvent event){
        updateTableData();
    }

    @UiHandler("destination")
    public void onDestinationClicked(ClickEvent event){
        updateTableData();
    }

    @UiHandler("uncreated")
    public void onUncreatedClicked(ClickEvent event){
        updateTableData();
    }

    private void initColumns(){
        while (table.getColumnCount() > 0) {
            table.removeColumn(0);
        }

        Column<Relation, String> counterColumn = new Column<Relation, String>(new ClickableTextCell()){

            @Override
            public void render(Cell.Context context, Relation object, SafeHtmlBuilder sb) {
                sb.appendHtmlConstant(String.valueOf(context.getIndex() + 1));
            }

            @Override
            public String getValue(Relation relation) {
                return null;
            }
        };

        TextColumn<Relation> taxTypeColumn = new TextColumn<Relation>() {
            @Override
            public String getValue(Relation object) {
                return object.getTaxType().getName();
            }
        };

        TextColumn<Relation> sourceColumn = new TextColumn<Relation>() {
            @Override
            public String getValue(Relation object) {
                return object.isSource() ? "Источник" : "Приемник";
            }
        };

        TextColumn<Relation> departmentColumn = new TextColumn<Relation>() {
            @Override
            public String getValue(Relation object) {
                return object.getFullDepartmentName();
            }
        };

        TextColumn<Relation> correctionDateColumn = new TextColumn<Relation>() {
            @Override
            public String getValue(Relation object) {
                if (object.getCorrectionDate() == null) {
                    return null;
                }
                return DATE_TIME_FORMAT.format(object.getCorrectionDate());
            }
        };
        correctionDateColumn.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);

        TextColumn<Relation> performerColumn = new TextColumn<Relation>() {
            @Override
            public String getValue(Relation object) {
                if (object.getPerformerNames() != null && !object.getPerformerNames().isEmpty()) {
                    StringBuilder performers = new StringBuilder();
                    for (String performer : object.getPerformerNames()) {
						if (performers.length() > 0) {
							performers.append("; ");
						}
                        performers.append(performer);
                    }
                    return performers.toString();
                } else {
                    return "";
                }
            }
        };

        TextColumn<Relation> stateColumn = new TextColumn<Relation>() {
            @Override
            public String getValue(Relation object) {
                //return object.isCreated() ? object.getState().getTitle(): "Не создана";
                return urlTemplates.getColValue(
                        object.isCreated() ? object.getState().getTitle() : "Не создана",
                        !object.isStatus() ? " (версия макета выведена из действия)" : "").
                        asString();
            }
        };

        TextColumn<Relation> formKindColumn = new TextColumn<Relation>() {
            @Override
            public String getValue(Relation object) {
                if (object.getFormTypeName()!= null)
                    return object.getFormDataKind().getTitle();
                else
                    return "";
            }
        };

        TextColumn<Relation> yearColumn = new TextColumn<Relation>() {
            @Override
            public String getValue(Relation object) {
                return String.valueOf(object.getYear());
            }
        };

        TextColumn<Relation> periodColumn = new TextColumn<Relation>() {
            @Override
            public String getValue(Relation object) {
                return object.getPeriodName();
            }
        };

        TextColumn<Relation> comparativePeriodColumn = new TextColumn<Relation>() {
            @Override
            public String getValue(Relation object) {
                return object.getComparativePeriodName() != null ?
                        object.getComparativePeriodYear() + ", " + object.getComparativePeriodName() : "";
            }
        };

        Column<Relation, String> formTypeColumn = new Column<Relation, String>(new ClickableTextCell()){

            @Override
            public void render(Cell.Context context, Relation object, SafeHtmlBuilder sb) {
                String link;
                if (object.getFormTypeName() != null) {
                    if (object.isCreated()) {
                        link = "<a href=\"#"
                                + FormDataPresenter.NAME_TOKEN + ";"
                                + FormDataPresenter.FORM_DATA_ID + "="
                                + object.getFormDataId() + "\">"
                                + object.getFormTypeName() + "</a>";
                    } else {
                        link = object.getFormTypeName();
                    }
                } else {
                    if (object.isCreated()) {
                        link = "<a href=\"#"
                                + DeclarationDataTokens.declarationData + ";"
                                + DeclarationDataTokens.declarationId + "="
                                + object.getDeclarationDataId() + "\">"
                                + object.getDeclarationTypeName() + "</a>";
                    } else {
                        link = object.getDeclarationTypeName();
                    }
                }
                sb.appendHtmlConstant(link);
            }

            @Override
            public String getValue(Relation relation) {
                if (relation.getFormTypeName() != null)
                    return relation.getFormTypeName();
                else
                    return relation.getDeclarationTypeName();
            }
        };

        formKindColumn.setCellStyleNames("linkCell");

        TextColumn<Relation> monthColumn = new TextColumn<Relation>() {
            @Override
            public String getValue(Relation object) {
                return object.getMonth() != null ? Months.fromId(object.getMonth()).getTitle() : "";
            }
        };

        TextColumn<Relation> declarationTaxOrganColumn = new TextColumn<Relation>() {
            @Override
            public String getValue(Relation object) {
                return object.getTaxOrganCode() != null ? object.getTaxOrganCode() : "";
            }
        };

        TextColumn<Relation> declarationTaxOrganKppColumn = new TextColumn<Relation>() {
            @Override
            public String getValue(Relation object) {
                return object.getKpp() != null ? object.getKpp() : "";
            }
        };

        table.addColumn(counterColumn, "№");
        table.setColumnWidth(counterColumn, 20, Style.Unit.PX);
        table.addColumn(taxTypeColumn, "Налог");
        table.setColumnWidth(taxTypeColumn, 110, Style.Unit.PX);
        table.addColumn(sourceColumn, "Источник / Приёмник");
        table.setColumnWidth(sourceColumn, 85, Style.Unit.PX);
        table.addColumn(departmentColumn, "Подразделение");
        table.addColumn(correctionDateColumn, "Дата сдачи корректировки");
        table.setColumnWidth(correctionDateColumn, 85, Style.Unit.PX);
        if (isForm) {
            table.addColumn(formKindColumn, "Тип формы");
            table.setColumnWidth(formKindColumn, 110, Style.Unit.PX);
            table.addColumn(formTypeColumn, "Вид формы");
            table.setColumnWidth(formTypeColumn, 110, Style.Unit.PX);
        } else {
			table.addColumn(formTypeColumn, "Вид налоговой формы");
        }
        table.addColumn(yearColumn, "Год");
        table.setColumnWidth(yearColumn, 40, Style.Unit.PX);
        table.addColumn(periodColumn, "Период");
        table.setColumnWidth(periodColumn, 70, Style.Unit.PX);
        if (isForm) {
			table.addColumn(monthColumn, "Месяц");
			table.setColumnWidth(monthColumn, 70, Style.Unit.PX);
            table.addColumn(performerColumn, "Исполнитель");
        } else {
            switch (getUiHandlers().getTaxType()) {
                case PROPERTY:
                case TRANSPORT:
                    table.addColumn(declarationTaxOrganColumn, "Налоговый орган");
                case INCOME:
                    table.addColumn(declarationTaxOrganKppColumn, "КПП");
                    break;
            }
        }
        if (isForm) {
            table.addColumn(stateColumn, "Состояние формы");
        } else {
			table.addColumn(stateColumn, "Состояние налоговой формы");
        }
        table.setColumnWidth(stateColumn, 90, Style.Unit.PX);
    }

    private void initCheckboxes() {
        source.setVisible(true);
        source.setValue(true);
        destination.setVisible(true);
        destination.setValue(true);
        uncreated.setValue(false);
    }

    private void updateSwitchMode() {
		verSep.setVisible(true);
		formDecAnchor.setVisible(true);
		formDecAnchor.setText(isForm ? (TITLE_DEC) : TITLE_FORM);
        formDecLabel.setText(!isForm ? (TITLE_DEC) : TITLE_FORM);
        source.setVisible(isForm);
    }

    @Override
    public void setTableData(List<Relation> result) {
        tableData = result;
        isForm = true;
        if (getUiHandlers() != null) {
            if (result == null)
                initColumns();
            updateSwitchMode();
            initCheckboxes();
            updateTableData();
        }
    }

    private void updateTableData() {
        List<Relation> filteredData = new LinkedList<Relation>();
        if (tableData != null) {
            boolean src = source.getValue();
            boolean dst = destination.getValue();
            boolean uncr = uncreated.getValue();

            for (Relation relation : tableData) {
                boolean fSrc = relation.isSource();
                boolean fCr = relation.isCreated();
                boolean fForm = relation.getFormTypeName() != null;
                if ((src && fSrc || dst && !fSrc) && (uncr || fCr) && (fForm == isForm)) {
                    filteredData.add(relation);
                }
            }
            dataProvider.setList(filteredData);
            table.setVisibleRange(new Range(0, filteredData.size()));
            table.flush();
        }
    }
}
