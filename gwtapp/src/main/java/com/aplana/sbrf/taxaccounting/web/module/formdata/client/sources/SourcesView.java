package com.aplana.sbrf.taxaccounting.web.module.formdata.client.sources;

import com.aplana.gwt.client.ModalWindow;
import com.aplana.sbrf.taxaccounting.model.FormToFormRelation;
import com.aplana.sbrf.taxaccounting.web.module.formdata.client.FormDataPresenter;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.ClickableTextCell;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;
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

    private List<FormToFormRelation> tableData = null;

    private final PopupPanel widget;

    @UiField
    Button close;
    @UiField
    ModalWindow modalWindow;
    @UiField
    DataGrid<FormToFormRelation> table;
    @UiField
    CheckBox source;
    @UiField
    CheckBox destination;
    @UiField
    CheckBox uncreated;

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

    @UiHandler("destination")
    public void onDestinationClicked(ClickEvent event){
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

        TextColumn<FormToFormRelation> performerColumn = new TextColumn<FormToFormRelation>() {
            @Override
            public String getValue(FormToFormRelation object) {
                return object.getPerformer().getName();
            }
        };

        TextColumn<FormToFormRelation> stateColumn = new TextColumn<FormToFormRelation>() {
            @Override
            public String getValue(FormToFormRelation object) {
                return object.isCreated() ? object.getState().getName(): "Не создана";
            }
        };

        TextColumn<FormToFormRelation> formKindColumn = new TextColumn<FormToFormRelation>() {
            @Override
            public String getValue(FormToFormRelation object) {
                return object.getFormDataKind().getName();
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

        table.addColumn(counterColumn, "№");
        table.setColumnWidth(counterColumn, 50, Style.Unit.PX);
        table.addColumn(sourceColumn, "Источник / Приёмник");
        table.setColumnWidth(sourceColumn, 100, Style.Unit.PX);
        table.addColumn(departmentColumn, "Подразделение");
        table.setColumnWidth(departmentColumn, 100, Style.Unit.PX);
        table.addColumn(formKindColumn, "Тип формы");
        table.setColumnWidth(formKindColumn, 200, Style.Unit.PX);
        table.addColumn(formTypeColumn, "Вид формы");
        table.setColumnWidth(formTypeColumn, 250, Style.Unit.PX);
        table.addColumn(performerColumn, "Исполнитель");
        table.setColumnWidth(performerColumn, 150, Style.Unit.PX);
        table.addColumn(stateColumn, "Состояние формы");
        table.setColumnWidth(stateColumn, 100, Style.Unit.PCT);
        table.setRowCount(0);
    }

    private void initCheckboxes() {
        source.setValue(true);
        destination.setValue(true);
        uncreated.setValue(false);
    }

    @Override
    public void setTableData(List<FormToFormRelation> result) {
        tableData = result;
        initCheckboxes();
        updateTableData();
    }

    private void updateTableData() {
        List<FormToFormRelation> filteredData = new LinkedList<FormToFormRelation>();
        if (tableData != null) {
            boolean src = source.getValue();
            boolean dst = destination.getValue();
            boolean uncr = uncreated.getValue();

            for (FormToFormRelation formToFormRelation : tableData) {
                boolean fSrc = formToFormRelation.isSource();
                boolean fCr = formToFormRelation.isCreated();
                if ((src && fSrc || dst && !fSrc) && (uncr || fCr)) {
                    filteredData.add(formToFormRelation);
                }
            }
        }
        table.setRowCount(filteredData.size());
        table.setRowData(0, filteredData);
    }
}
