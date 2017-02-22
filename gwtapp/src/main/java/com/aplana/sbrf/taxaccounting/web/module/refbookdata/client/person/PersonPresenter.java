package com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.person;

import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogAddEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogCleanEvent;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.*;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;
import com.gwtplatform.mvp.client.PresenterWidget;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Презентер попап модального окна "Файлы и комментарии",
 * данное окно вызывается с формы нф
 *
 * @author Lhaziev
 */
public class PersonPresenter extends PresenterWidget<PersonPresenter.MyView> implements PersonUiHandlers {
    public interface MyView extends PopupView, HasUiHandlers<PersonUiHandlers> {
        void init(RefBookDataRow row, List<RefBookAttribute> attributes);
        void setTableColumns(final List<RefBookColumn> columns);
        void setDuplicateTableColumns(final List<RefBookColumn> columns);
        void setTableRows(final List<RefBookDataRow> rows);
        void setDuplicateTableRows(final List<RefBookDataRow> rows);
    }

    private final DispatchAsync dispatcher;

    private RefBookDataRow row;
    private RefBookDataRow originalRow;
    private List<RefBookDataRow> duplicateRows = new ArrayList<RefBookDataRow>();
    private List<RefBookDataRow> deleteDuplicateRows = new ArrayList<RefBookDataRow>();
    private List<RefBookColumn> columns;

    @Inject
    public PersonPresenter(final EventBus eventBus, final MyView view, DispatchAsync dispatcher) {
        super(eventBus, view);
        this.dispatcher = dispatcher;
        getView().setUiHandlers(this);
    }

    @Override
    protected void onReveal() {
        super.onReveal();
        setOriginalRow(null);
        deleteDuplicateRows.clear();
        GetDuplicatePersonAction action = new GetDuplicatePersonAction();
        action.setRelevanceDate(new Date());
        action.setRecord(row);
        dispatcher.execute(action,
                CallbackUtils.defaultCallback(
                        new AbstractCallback<GetDuplicatePersonResult>() {
                            @Override
                            public void onSuccess(GetDuplicatePersonResult result) {
                                row = result.getDataRow();
                                getView().init(row, result.getTableHeaders());
                                setOriginalRow(result.getOriginalRow());
                                duplicateRows = result.getDuplicateRows();
                                getView().setDuplicateTableRows(duplicateRows);
                            }
                        }, PersonPresenter.this));
    }

    public void setOriginalRow(RefBookDataRow originalRow) {
        this.originalRow = originalRow;
        if (originalRow != null) {
            getView().setTableRows(Arrays.asList(originalRow));
        } else {
            getView().setTableRows(null);
        }
    }

    @Override
    public void onHide() {
        super.onHide();

    }

    @Override
    protected void onBind() {
        super.onBind();
    }

    @Override
    public void onSave() {
        LogCleanEvent.fire(this);
        SaveDuplicatePersonAction action = new SaveDuplicatePersonAction();
        action.setRecord(row);
        action.setOriginalRecord(originalRow);
        action.setDuplicateRecords(duplicateRows);
        action.setDeleteDuplicateRecords(deleteDuplicateRows);
        dispatcher.execute(action,
                CallbackUtils.defaultCallback(
                        new AbstractCallback<SaveDuplicatePersonResult>() {
                            @Override
                            public void onSuccess(SaveDuplicatePersonResult result) {
                                LogAddEvent.fire(PersonPresenter.this, result.getUuid());
                                onReveal();
                            }
                        }, PersonPresenter.this));

    }

    public void init(RefBookDataRow row, List<RefBookColumn> columns) {
        getView().setTableColumns(columns);
        getView().setDuplicateTableColumns(columns);
        this.row = row;
        this.columns = columns;
        //getView().init(row, columns);
    }

    @Override
    public void addOriginalPerson(Long recordId) {
        GetRefBookTableDataAction action = new GetRefBookTableDataAction();
        action.setFilter(RefBook.RECORD_ID_ALIAS + " = " + recordId);
        action.setRefBookId(RefBook.Id.PERSON.getId());
        action.setPagingParams(new PagingParams(1, 1));
        action.setRelevanceDate(new Date());
        action.setSortColumnIndex(0);
        action.setAscSorting(false);
        dispatcher.execute(action,
                CallbackUtils.defaultCallback(
                        new AbstractCallback<GetRefBookTableDataResult>() {
                            @Override
                            public void onSuccess(GetRefBookTableDataResult result) {
                                setOriginalRow(result.getDataRows().get(0));
                            }
                        }, PersonPresenter.this));
    }

    @Override
    public void removeOriginalPerson() {
        setOriginalRow(null);
    }

    @Override
    public void addDuplicatePerson(Long recordId) {
        GetRefBookTableDataAction action = new GetRefBookTableDataAction();
        action.setFilter(RefBook.RECORD_ID_ALIAS + " = " + recordId);
        action.setRecordId(recordId);
        action.setRefBookId(RefBook.Id.PERSON.getId());
        action.setPagingParams(new PagingParams(1, 1));
        action.setRelevanceDate(new Date());
        action.setSortColumnIndex(0);
        action.setAscSorting(false);
        dispatcher.execute(action,
                CallbackUtils.defaultCallback(
                        new AbstractCallback<GetRefBookTableDataResult>() {
                            @Override
                            public void onSuccess(GetRefBookTableDataResult result) {
                                deleteDuplicateRows.removeAll(result.getDataRows());
                                if (!duplicateRows.containsAll(result.getDataRows())) {
                                    duplicateRows.addAll(result.getDataRows());
                                }
                                getView().setDuplicateTableRows(duplicateRows);
                            }
                        }, PersonPresenter.this));
    }

    @Override
    public void removeDuplicatePerson(List<RefBookDataRow> refBookDataRowList) {
        deleteDuplicateRows.addAll(refBookDataRowList);
        for(RefBookDataRow refBookDataRow: refBookDataRowList) {
            duplicateRows.remove(refBookDataRow);
        }
        getView().setDuplicateTableRows(duplicateRows);
    }

    @Override
    public RefBookDataRow getRow() {
        return row;
    }

    @Override
    public List<RefBookDataRow> getDuplicateRows() {
        return duplicateRows;
    }

    @Override
    public RefBookDataRow getOriginalRow() {
        return originalRow;
    }
}

