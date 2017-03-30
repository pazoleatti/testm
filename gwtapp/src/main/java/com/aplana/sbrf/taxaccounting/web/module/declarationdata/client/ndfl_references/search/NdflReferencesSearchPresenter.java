package com.aplana.sbrf.taxaccounting.web.module.declarationdata.client.ndfl_references.search;

import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.gwt.client.dialog.DialogHandler;
import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.NumericColumn;
import com.aplana.sbrf.taxaccounting.model.StringColumn;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.client.ndfl_references.event.ChooseButtonEvent;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared.GetNdflReferencesResult;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared.GetNdflReferencesTableDataAction;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared.NdflReferenceDTO;
import com.aplana.sbrf.taxaccounting.web.widget.datepicker.DateMaskBoxPicker;
import com.aplana.sbrf.taxaccounting.web.widget.style.GenericDataGrid;
import com.aplana.sbrf.taxaccounting.web.widget.style.table.CheckBoxHeader;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.Range;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;
import com.gwtplatform.mvp.client.PresenterWidget;

import java.math.BigDecimal;
import java.util.*;

public class NdflReferencesSearchPresenter extends PresenterWidget<NdflReferencesSearchPresenter.SearchView> implements NdflReferencesSearchUIHandlers {


    public interface SearchView extends PopupView, HasUiHandlers<NdflReferencesSearchUIHandlers> {
        String getRefNumberValue();
        String getLastNameValue();
        String getFirstNameValue();
        String getMiddleNameValue();
        Date getBirthDateFromValue();
        Date getBirthDateBeforeValue();
        TextBox getRefNumberTextBox();
        TextBox getLastNameTextBox();
        TextBox getFirstNameTextBox();
        TextBox getMiddleNameTextBox();
        DateMaskBoxPicker birthDateFromPicker();
        DateMaskBoxPicker birthDateBeforePicker();
        void updateTable();
        GenericDataGrid<DataRow<Cell>> getSearchTable();
        void setTableData(List<DataRow<Cell>> tableData);
        CheckBoxHeader getCheckBoxHeader();
        NumericColumn getNdflReferenceIdColumn();
        StringColumn getRefNumberColumn();
        StringColumn getLastNameColumn();
        StringColumn getFirstNameColumn();
        StringColumn getMiddleNameColumn();
        StringColumn getBirthDateColumn();
        StringColumn getTaxServiceTextColumn();
        MultiSelectionModel<DataRow<Cell>> getSelectionModel();
    }

    private Long declarationDataId;
    private final DispatchAsync dispatcher;
    private final TableDataProvider dataProvider = new TableDataProvider();
    private boolean dataProviderInitialized = false;
    private EventBus eventBus;

    @Inject
    public NdflReferencesSearchPresenter(final EventBus eventBus, final SearchView view, DispatchAsync dispatcher) {
        super(eventBus, view);
        this.eventBus = eventBus;
        this.dispatcher = dispatcher;
        getView().setUiHandlers(this);
    }

    @Override
    public void onFindClicked() {
        dataProvider.refNumber = getView().getRefNumberValue();
        dataProvider.lastNamePattrern = getView().getLastNameValue();
        dataProvider.firstNamePattern = getView().getFirstNameValue();
        dataProvider.middleNamePattern = getView().getMiddleNameValue();
        dataProvider.birthDateFrom = getView().getBirthDateFromValue();
        dataProvider.birthDateBefore = getView().getBirthDateBeforeValue();
        if (!dataProviderInitialized) {
            dataProvider.addDataDisplay(getView().getSearchTable());
            dataProviderInitialized = true;
        } else {
            getView().updateTable();
        }
    }

    @Override
    public void onClearClicked() {
        getView().getRefNumberTextBox().setValue(null);
        getView().getLastNameTextBox().setValue(null);
        getView().getFirstNameTextBox().setValue(null);
        getView().getMiddleNameTextBox().setValue(null);
        getView().birthDateFromPicker().setValue(null, false);
        getView().birthDateBeforePicker().setValue(null);
    }

    @Override
    public void onChooseClicked(Set<DataRow<Cell>> rows) {
        eventBus.fireEvent(new ChooseButtonEvent(new ArrayList<DataRow<Cell>>(rows)));
        getView().getSelectionModel().clear();
    }

    @Override
    public void onCloseClicked() {
        Dialog.confirmMessageYesNo("Отмена", "Отменить изменения?", new DialogHandler() {
            @Override
            public void yes() {
                super.yes();
                getView().hide();
            }
        });
    }

    @Override
    protected void onReveal() {
        super.onReveal();
        dataProvider.addDataDisplay(getView().getSearchTable());
        dataProviderInitialized = true;
    }

    @Override
    protected void onHide() {
        super.onHide();
        getView().getSearchTable().setRowCount(0);
        getView().getSearchTable().setRowData(0, new ArrayList<DataRow<Cell>>());
        if (dataProviderInitialized) {
            dataProvider.removeDataDisplay(getView().getSearchTable());
        }
        dataProviderInitialized = false;
        onClearClicked();
    }

    public void setDeclarationDataId(long declarationDataId) {
        this.declarationDataId = declarationDataId;
    }

    private DataRow<Cell> createDataRow() {
        DataRow<Cell> dataRow = new DataRow<Cell>();

        Cell ndflReferenceIdCell = new Cell();
        ndflReferenceIdCell.setColumn(getView().getNdflReferenceIdColumn());

        Cell refNumberCell = new Cell();
        refNumberCell.setColumn(getView().getRefNumberColumn());

        Cell lastNameCell = new Cell();
        lastNameCell.setColumn(getView().getLastNameColumn());

        Cell firstNameCell = new Cell();
        firstNameCell.setColumn(getView().getFirstNameColumn());

        Cell middleNameCell = new Cell();
        middleNameCell.setColumn(getView().getMiddleNameColumn());

        Cell birthDateCell = new Cell();
        birthDateCell.setColumn(getView().getBirthDateColumn());

        Cell taxServiceTextColumn = new Cell();
        taxServiceTextColumn.setColumn(getView().getTaxServiceTextColumn());

        dataRow.setFormColumns(Arrays.asList(
                ndflReferenceIdCell,
                refNumberCell,
                lastNameCell,
                firstNameCell,
                middleNameCell,
                birthDateCell,
                taxServiceTextColumn
        ));
        return dataRow;
    }

    private List<DataRow<Cell>> toDataRow(List<NdflReferenceDTO> ndflReferences) {
        List<DataRow<Cell>> toReturn = new ArrayList<DataRow<Cell>>();

        for (NdflReferenceDTO ndflReference : ndflReferences) {
            toReturn.add(toDataRow(ndflReference));
        }
        return toReturn;
    }

    private DataRow<Cell> toDataRow(NdflReferenceDTO ndflReference) {
        DataRow<Cell> toReturn = createDataRow();
        toReturn.getCell(getView().getNdflReferenceIdColumn().getAlias()).setNumericValue(new BigDecimal(ndflReference.getId()));
        toReturn.getCell(getView().getRefNumberColumn().getAlias()).setStringValue(ndflReference.getRefNumber());
        toReturn.getCell(getView().getLastNameColumn().getAlias()).setStringValue(ndflReference.getLastName());
        toReturn.getCell(getView().getFirstNameColumn().getAlias()).setStringValue(ndflReference.getFirstName());
        toReturn.getCell(getView().getMiddleNameColumn().getAlias()).setStringValue(ndflReference.getMiddleName());
        toReturn.getCell(getView().getBirthDateColumn().getAlias()).setDateValue(ndflReference.getBirthDate());
        toReturn.getCell(getView().getTaxServiceTextColumn().getAlias()).setStringValue(ndflReference.getErrorText());

        return toReturn;
    }

    private class TableDataProvider extends AsyncDataProvider<DataRow<Cell>> {

        String refNumber;
        String lastNamePattrern;
        String firstNamePattern;
        String middleNamePattern;
        Date birthDateFrom;
        Date birthDateBefore;

        @Override
        protected void onRangeChanged(HasData<DataRow<Cell>> display) {
            GetNdflReferencesTableDataAction action = new GetNdflReferencesTableDataAction();
            action.setDeclarationDataId(declarationDataId);
            action.setRefNumber(refNumber);
            action.setFirstNamePattern(firstNamePattern);
            action.setLastNamePattrern(lastNamePattrern);
            action.setMiddleNamePattern(middleNamePattern);
            action.setBirthDateFrom(birthDateFrom);
            action.setBirthDateBefore(birthDateBefore);
            final Range range = getView().getSearchTable().getVisibleRange();
            dispatcher.execute(action,
                    CallbackUtils.defaultCallback (
                            new AbstractCallback<GetNdflReferencesResult>() {
                                @Override
                                public void onSuccess(GetNdflReferencesResult result) {
                                    if (!result.getNdflReferences().isEmpty()) {
                                        getView().getSearchTable().setRowCount(result.getNdflReferences().size());
                                        getView().getSearchTable().setRowData(toDataRow(result.getNdflReferences()));
                                    }


                                    //updateRowData(range.getStart(), toDataRow(result.getNdflReferences()));
                                }
                            }, NdflReferencesSearchPresenter.this));
        }
    }
}
