package com.aplana.sbrf.taxaccounting.web.module.declarationdata.client.ndfl_references;

import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.gwt.client.dialog.DialogHandler;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.DeclarationDataReportType;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.client.DeclarationDataPresenter;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.client.ndfl_references.event.ChooseButtonEvent;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.client.ndfl_references.search.NdflReferencesSearchPresenter;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared.NdflReferenceDTO;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared.UpdateNdflReferenceResult;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared.UpdateNdflReferencesAction;
import com.aplana.sbrf.taxaccounting.web.widget.style.GenericDataGrid;
import com.aplana.sbrf.taxaccounting.web.widget.style.table.CheckBoxHeader;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.ListDataProvider;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;
import com.gwtplatform.mvp.client.PresenterWidget;

import java.math.BigDecimal;
import java.util.*;

public class NdflReferencesEditPresenter extends PresenterWidget<NdflReferencesEditPresenter.MyView> implements NdflReferencesEditUiHandlers {

    public interface MyView extends PopupView, HasUiHandlers<NdflReferencesEditUiHandlers> {
        void setTableData(List<DataRow<Cell>> tableData);

        void setReadOnlyMode(boolean readOnlyMode);

        void setNote(String note);

        GenericDataGrid<DataRow<Cell>> getTable();

        CheckBoxHeader getCheckBoxHeader();

        NumericColumn getNdflReferenceIdColumn();

        StringColumn getRefNumberColumn();

        StringColumn getLastNameColumn();

        StringColumn getFirstNameColumn();

        StringColumn getMiddleNameColumn();

        DateColumn getBirthDateColumn();

        StringColumn getTaxServiceTextColumn();

        void updateTable(int size);
    }

    private static final String ERROR_MSG = "Операция не выполнена";

    private final DispatchAsync dispatcher;
    private Long declarationDataId;
    private DeclarationDataPresenter declarationDataPresenter;
    private NdflReferencesSearchPresenter ndflReferencesSearchPresenter;
    private ListDataProvider<DataRow<Cell>> dataProvider = new ListDataProvider<DataRow<Cell>>();
    private boolean dataProviderInitialized = false;

    @Inject
    public NdflReferencesEditPresenter(final EventBus eventBus, final NdflReferencesEditPresenter.MyView view, final DispatchAsync dispatcher,
                                       NdflReferencesSearchPresenter ndflReferencesSearchPresenter) {
        super(eventBus, view);
        eventBus.addHandler(ChooseButtonEvent.getType(), new ChooseButtonEvent.ChooseHandler() {
            @Override
            public void onChoice(ChooseButtonEvent event) {
                if (!dataProviderInitialized) {
                    dataProvider.addDataDisplay(getView().getTable());
                    dataProviderInitialized = true;
                }
                dataProvider.setList(event.getNdflReferences());
                getView().updateTable(event.getNdflReferences().size());
            }
        });
        this.dispatcher = dispatcher;
        getView().setUiHandlers(this);
        this.ndflReferencesSearchPresenter = ndflReferencesSearchPresenter;
    }

    public void setDeclarationDataPresenter(DeclarationDataPresenter declarationDataPresenter) {
        this.declarationDataPresenter = declarationDataPresenter;
    }

    public void setDeclarationDataId(Long declarationDataId) {
        this.declarationDataId = declarationDataId;
    }

    @Override
    public void onAddClicked() {
        ndflReferencesSearchPresenter.setDeclarationDataId(declarationDataId);
        addToPopupSlot(ndflReferencesSearchPresenter);
    }

    @Override
    public void onRemoveClicked(Set<DataRow<Cell>> selectedItems) {
        dataProvider.getList().removeAll(selectedItems);
    }

    @Override
    public void onSaveClicked(final String note, final List<DataRow<Cell>> rows, boolean exit) {
        getView().getTable().flush();
        UpdateNdflReferencesAction action = new UpdateNdflReferencesAction();
        action.setNdflReferences(toNdflReference(rows));
        action.setNote(note);
        action.setDeclarationDataId(declarationDataId);
        dispatcher.execute(action, CallbackUtils.defaultCallback(new AbstractCallback<UpdateNdflReferenceResult>() {
            @Override
            public void onSuccess(UpdateNdflReferenceResult result) {
                for (DataRow<Cell> row : rows) {
                    row.getCell(getView().getTaxServiceTextColumn().getAlias()).setStringValue(note);
                    getView().getTable().redraw();
                }
                declarationDataPresenter.viewReport(true, true, DeclarationDataReportType.PDF_DEC);
            }
        }, this));
    }

    @Override
    public void onCancelClicked() {
        Dialog.confirmMessageYesNo("Закрытие", "Закрыть окно?", new DialogHandler() {
            @Override
            public void yes() {
                super.yes();
                getView().hide();
            }
        });
    }

    @Override
    public void onHide() {
        super.onHide();
        /*getView().getTable().setRowData(new ArrayList<DataRow<Cell>>());
        getView().setNote(null);*/
    }

    public void clean() {
        getView().updateTable(0);
        getView().setNote("");
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

    private List<Long> toNdflReference(List<DataRow<Cell>> tableDataRowList) {
        List<Long> toReturn = new LinkedList<Long>();

        for (DataRow<Cell> dataRow : tableDataRowList) {
            toReturn.add(dataRow.getCell(getView().getNdflReferenceIdColumn().getAlias()).getNumericValue().longValue());
        }

        return toReturn;
    }

    private class TableDataProvider extends ListDataProvider<DataRow<Cell>> {
        @Override
        protected void onRangeChanged(HasData<DataRow<Cell>> display) {
            super.onRangeChanged(display);
        }
    }
}
