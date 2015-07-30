package com.aplana.sbrf.taxaccounting.web.module.formdata.client.sources;

import com.aplana.gwt.client.ModalWindow;
import com.aplana.sbrf.taxaccounting.model.FormToFormRelation;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.client.DeclarationDataTokens;
import com.aplana.sbrf.taxaccounting.web.module.formdata.client.FormDataPresenter;
import com.aplana.sbrf.taxaccounting.web.widget.style.LinkButton;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.ClickableTextCell;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.i18n.client.DateTimeFormat;
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

    private static final DateTimeFormat DATE_TIME_FORMAT = DateTimeFormat.getFormat("dd.MM.yyyy");
    public static final String TITLE_FORM = "Формы источники/приемники";
    public static final String TITLE_DEC = "Декларации приемники";
    public static final String TITLE_DEC_DEAL = "Уведомления приемники";

    @UiField
    Button close;
    @UiField
    ModalWindow modalWindow;
    @UiField
    DataGrid<FormToFormRelation> table;
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
    private ListDataProvider<FormToFormRelation> dataProvider = new ListDataProvider<FormToFormRelation>();

    @Inject
    public SourcesView(Binder uiBinder, EventBus eventBus) {
        super(eventBus);
        widget = uiBinder.createAndBindUi(this);
        widget.setAnimationEnabled(true);
        isForm = true;
        initColumns();
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
        initColumns();
        updateSwitchMode();
        updateTableData();
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
                return object.isCreated() ? object.getState().getName(): "Не создана";
            }
        };

        TextColumn<FormToFormRelation> formKindColumn = new TextColumn<FormToFormRelation>() {
            @Override
            public String getValue(FormToFormRelation object) {
                if (object.getFormType()!= null)
                    return object.getFormDataKind().getName();
                else
                    return "";
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
                if (object.getFormType() != null) {
                    if (object.isCreated()) {
                        link = "<a href=\"#"
                                + FormDataPresenter.NAME_TOKEN + ";"
                                + FormDataPresenter.FORM_DATA_ID + "="
                                + object.getFormDataId() + "\">"
                                + object.getFormType().getName() + "</a>";
                    } else {
                        link = object.getFormType().getName();
                    }
                } else {
                    if (object.isCreated()) {
                        link = "<a href=\"#"
                                + DeclarationDataTokens.declarationData + ";"
                                + DeclarationDataTokens.declarationId + "="
                                + object.getDeclarationDataId() + "\">"
                                + object.getDeclarationType().getName() + "</a>";
                    } else {
                        link = object.getDeclarationType().getName();
                    }
                }
                sb.appendHtmlConstant(link);
            }

            @Override
            public String getValue(FormToFormRelation formToFormRelation) {
                if (formToFormRelation.getFormType()!= null)
                    return formToFormRelation.getFormType().getName();
                else
                    return formToFormRelation.getDeclarationType().getName();
            }
        };

        formKindColumn.setCellStyleNames("linkCell");

        TextColumn<FormToFormRelation> monthColumn = new TextColumn<FormToFormRelation>() {
            @Override
            public String getValue(FormToFormRelation object) {
                return object.getMonth() != null ? object.getMonth() : "";
            }
        };

        TextColumn<FormToFormRelation> declarationTaxOrganColumn = new TextColumn<FormToFormRelation>() {
            @Override
            public String getValue(FormToFormRelation object) {
                return object.getTaxOrganCode() != null ? object.getTaxOrganCode() : "";
            }
        };

        TextColumn<FormToFormRelation> declarationTaxOrganKppColumn = new TextColumn<FormToFormRelation>() {
            @Override
            public String getValue(FormToFormRelation object) {
                return object.getKpp() != null ? object.getKpp() : "";
            }
        };

        table.addColumn(counterColumn, "№");
        table.setColumnWidth(counterColumn, 20, Style.Unit.PX);
        table.addColumn(sourceColumn, "Источник / Приёмник");
        table.setColumnWidth(sourceColumn, 80, Style.Unit.PX);
        table.addColumn(departmentColumn, "Подразделение");
        table.addColumn(correctionDateColumn, "Дата сдачи корректировки");
        table.setColumnWidth(correctionDateColumn, 85, Style.Unit.PX);
        if (isForm) {
            table.addColumn(formKindColumn, "Тип формы");
            table.setColumnWidth(formKindColumn, 100, Style.Unit.PX);
            table.addColumn(formTypeColumn, "Вид формы");
            table.setColumnWidth(formTypeColumn, 100, Style.Unit.PX);
        } else {
            if (!TaxType.DEAL.equals(getUiHandlers().getTaxType())) {
                table.addColumn(formTypeColumn, "Вид декларации");
            } else {
                table.addColumn(formTypeColumn, "Вид уведомления");
            }
            table.setColumnWidth(formTypeColumn, 110, Style.Unit.PX);
        }
        table.addColumn(yearColumn, "Год");
        table.addColumn(periodColumn, "Период");
        if (isForm) {
            table.addColumn(monthColumn, "Месяц");
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
            if (!TaxType.DEAL.equals(getUiHandlers().getTaxType())) {
                table.addColumn(stateColumn, "Состояние декларации");
            } else {
                table.addColumn(stateColumn, "Состояние уведомления");
            }
        }
        table.setColumnWidth(stateColumn, 120, Style.Unit.PX);
    }

    private void initCheckboxes() {
        source.setVisible(true);
        source.setValue(true);
        destination.setVisible(true);
        destination.setValue(true);
        uncreated.setValue(false);
    }

    private void updateSwitchMode() {
        boolean isTaxTypeDeal = TaxType.DEAL.equals(getUiHandlers().getTaxType());
        boolean isTaxTypeETR = TaxType.ETR.equals(getUiHandlers().getTaxType());

        if (isTaxTypeETR) {
            verSep.setVisible(false);
            formDecAnchor.setVisible(false);
        } else {
            verSep.setVisible(true);
            formDecAnchor.setVisible(true);
            formDecAnchor.setText(isForm ?
                    (isTaxTypeDeal ? TITLE_DEC_DEAL : TITLE_DEC) :
                    (isTaxTypeDeal ? TITLE_FORM : TITLE_FORM));
        }
        formDecLabel.setText(!isForm ?
                (isTaxTypeDeal ? TITLE_DEC_DEAL : TITLE_DEC) :
                (isTaxTypeDeal || isTaxTypeETR ? TITLE_FORM : TITLE_FORM));

        source.setVisible(isForm);
    }

    @Override
    public void setTableData(List<FormToFormRelation> result) {
        tableData = result;
        isForm = true;
        if (result == null)
            initColumns();
        updateSwitchMode();
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
                boolean fForm = formToFormRelation.getFormType() != null;
                if ((src && fSrc || dst && !fSrc) && (uncr || fCr) && (fForm == isForm)) {
                    filteredData.add(formToFormRelation);
                }
            }
            dataProvider.setList(filteredData);
            table.setVisibleRange(new Range(0, filteredData.size()));
            table.flush();
        }
    }
}
