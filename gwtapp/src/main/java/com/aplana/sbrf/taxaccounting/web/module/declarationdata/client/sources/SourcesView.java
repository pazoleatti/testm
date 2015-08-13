package com.aplana.sbrf.taxaccounting.web.module.declarationdata.client.sources;

import com.aplana.gwt.client.ModalWindow;
import com.aplana.sbrf.taxaccounting.model.FormToFormRelation;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.web.module.formdata.client.FormDataPresenter;
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
import com.google.gwt.user.client.ui.*;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.Range;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.PopupViewWithUiHandlers;

import java.util.LinkedList;
import java.util.List;

/**
 * Представление попапа окна с инфомацией об источниках текущей декларации
 *
 * @author auldanov
 */
public class SourcesView extends PopupViewWithUiHandlers<SourcesUiHandlers> implements SourcesPresenter.MyView{

    public interface Binder extends UiBinder<PopupPanel, SourcesView> {
    }

    private List<FormToFormRelation> tableData = null;

    private final PopupPanel widget;

    private static final DateTimeFormat DATE_TIME_FORMAT = DateTimeFormat.getFormat("dd.MM.yyyy");
    private TaxType taxType;

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
    DataGrid<FormToFormRelation> table;
    @UiField
    CheckBox source;
    @UiField
    CheckBox uncreated;
    private ListDataProvider<FormToFormRelation> dataProvider = new ListDataProvider<FormToFormRelation>();

    @Inject
    public SourcesView(Binder uiBinder, EventBus eventBus) {
        super(eventBus);
        widget = uiBinder.createAndBindUi(this);
        widget.setAnimationEnabled(true);
        init();
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

    @UiHandler("source")
    public void onSourceClicked(ClickEvent event){
        updateTableData();
    }

    @UiHandler("uncreated")
    public void onUncreatedClicked(ClickEvent event){
        updateTableData();
    }

    private void init(){
        Column<FormToFormRelation, String> counterColumn = new Column<FormToFormRelation, String>(new ClickableTextCell()){

            @Override
            public void render(Cell.Context context, FormToFormRelation object, SafeHtmlBuilder sb) {
                sb.appendHtmlConstant(String.valueOf(context.getIndex() + 1));
            }

            @Override
            public String getValue(FormToFormRelation formToFormRelation) {
                return null;
            }
        };

        TextColumn<FormToFormRelation> sourceColumn = new TextColumn<FormToFormRelation>() {
            @Override
            public String getValue(FormToFormRelation object) {
                return object.isSource() ? "Источник" : "Приемник";
            }
        };

        TextColumn<FormToFormRelation> departmentColumn = new TextColumn<FormToFormRelation>() {
            @Override
            public String getValue(FormToFormRelation object) {
                return object.getFullDepartmentName();
            }
        };

        TextColumn<FormToFormRelation> correctionDateColumn = new TextColumn<FormToFormRelation>() {
            @Override
            public String getValue(FormToFormRelation object) {
                if (object.getCorrectionDate() == null) {
                    return null;
                }
                return DATE_TIME_FORMAT.format(object.getCorrectionDate());
            }
        };
        correctionDateColumn.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);

        TextColumn<FormToFormRelation> performerColumn = new TextColumn<FormToFormRelation>() {
            @Override
            public String getValue(FormToFormRelation object) {
                return object.getPerformer() != null ? object.getPerformer().getName() : "";
            }
        };

        TextColumn<FormToFormRelation> stateColumn = new TextColumn<FormToFormRelation>() {
            @Override
            public String getValue(FormToFormRelation object) {
                return urlTemplates.getColValue(
                        object.isCreated() ? object.getState().getName() : "Не создана",
                        !object.isStatus() ? " (версия макета выведена из действия)" : "").
                        asString();
            }
        };

        TextColumn<FormToFormRelation> formKindColumn = new TextColumn<FormToFormRelation>() {
            @Override
            public String getValue(FormToFormRelation object) {
                return object.getFormDataKind().getName();
            }
        };

        TextColumn<FormToFormRelation> yearColumn = new TextColumn<FormToFormRelation>() {
            @Override
            public String getValue(FormToFormRelation object) {
                return String.valueOf(object.getYear());
            }
        };

        TextColumn<FormToFormRelation> periodColumn = new TextColumn<FormToFormRelation>() {
            @Override
            public String getValue(FormToFormRelation object) {
                return object.getPeriodName();
            }
        };

        Column<FormToFormRelation, String> formTypeColumn = new Column<FormToFormRelation, String>(new ClickableTextCell()){

            @Override
            public void render(Cell.Context context, FormToFormRelation object, SafeHtmlBuilder sb) {
                String link;
                if (object.isCreated()) {
                    link = "<a href=\"#"
                            + FormDataPresenter.NAME_TOKEN + ";"
                            + FormDataPresenter.FORM_DATA_ID + "="
                            + object.getFormDataId() + "\">"
                            + object.getFormType().getName() + "</a>";
                } else {
                    link = object.getFormType().getName();
                }
                sb.appendHtmlConstant(link);
            }

            @Override
            public String getValue(FormToFormRelation formToFormRelation) {
                return formToFormRelation.getFormType().getName();
            }
        };

        formKindColumn.setCellStyleNames("linkCell");

        TextColumn<FormToFormRelation> monthColumn = new TextColumn<FormToFormRelation>() {
            @Override
            public String getValue(FormToFormRelation object) {
                return object.getMonth() != null ? object.getMonth() : "";
            }
        };

        table.addColumn(counterColumn, "№");
        table.setColumnWidth(counterColumn, 20, Style.Unit.PX);
        table.addColumn(sourceColumn, "Источник / Приёмник");
        table.setColumnWidth(sourceColumn, 80, Style.Unit.PX);
        table.addColumn(departmentColumn, "Подразделение");
        table.addColumn(correctionDateColumn, "Дата сдачи корректировки");
        table.setColumnWidth(correctionDateColumn, 85, Style.Unit.PX);
        table.addColumn(formKindColumn, "Тип формы");
        table.setColumnWidth(formKindColumn, 120, Style.Unit.PX);
        table.addColumn(formTypeColumn, "Вид формы");
        table.setColumnWidth(formTypeColumn, 120, Style.Unit.PX);
        table.addColumn(yearColumn, "Год");
        table.setColumnWidth(yearColumn, 40, Style.Unit.PX);
        table.addColumn(periodColumn, "Период");
        table.setColumnWidth(periodColumn, 60, Style.Unit.PX);
        table.addColumn(monthColumn, "Месяц");
        table.setColumnWidth(monthColumn, 80, Style.Unit.PX);
        table.addColumn(performerColumn, "Исполнитель");
        table.addColumn(stateColumn, "Состояние формы");
        table.setColumnWidth(stateColumn, 130, Style.Unit.PX);
        table.setRowCount(0);
        dataProvider.addDataDisplay(table);
    }

    private void initCheckboxes() {
        source.setValue(true);
        uncreated.setValue(false);
    }

    @Override
    public void setTableData(List<FormToFormRelation> result) {
        tableData = result;
        initCheckboxes();
        updateTableData();
    }

    @Override
    public void setTaxType(TaxType taxType) {
        this.taxType = taxType;
        if (!taxType.equals(TaxType.DEAL)) {
            modalWindow.setTitle("Источники декларации");
        } else {
            modalWindow.setTitle("Источники уведомления");
        }
    }

    private void updateTableData() {
        List<FormToFormRelation> filteredData = new LinkedList<FormToFormRelation>();
        if (tableData != null) {
            boolean src = source.getValue();
            boolean uncr = uncreated.getValue();

            for (FormToFormRelation formToFormRelation : tableData) {
                boolean fSrc = formToFormRelation.isSource();
                boolean fCr = formToFormRelation.isCreated();
                if (src && fSrc && (uncr || fCr)) {
                    filteredData.add(formToFormRelation);
                }
            }
            dataProvider.setList(filteredData);
            table.setVisibleRange(new Range(0, filteredData.size()));
            table.flush();
        }
    }
}
