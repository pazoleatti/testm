package com.aplana.sbrf.taxaccounting.web.module.refbookdata.client;

import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.web.main.api.client.RevealContentTypeHolder;
import com.aplana.sbrf.taxaccounting.web.main.api.client.TaPlaceManager;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogAddEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogCleanEvent;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.editform.EditFormPresenter;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.editform.event.RollbackTableRowSelection;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.editform.event.SetFormMode;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.editform.event.UpdateForm;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.sendquerydialog.DialogPresenter;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.versionform.RefBookVersionPresenter;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.*;
import com.aplana.sbrf.taxaccounting.web.module.refbooklist.client.RefBookListTokens;
import com.google.gwt.view.client.AbstractDataProvider;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.Place;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class RefBookDataPresenter extends Presenter<RefBookDataPresenter.MyView,
		RefBookDataPresenter.MyProxy> implements RefBookDataUiHandlers,
		UpdateForm.UpdateFormHandler, SetFormMode.SetFormModeHandler, RollbackTableRowSelection.RollbackTableRowSelectionHandler {

    @ProxyCodeSplit
	@NameToken(RefBookDataTokens.refBookData)
	public interface MyProxy extends ProxyPlace<RefBookDataPresenter>, Place {
	}

	static final Object TYPE_editFormPresenter = new Object();

	private Long refBookDataId;

    private Long recordId;
    private FormMode mode;

    private Integer selectedRowIndex;

	EditFormPresenter editFormPresenter;
    RefBookVersionPresenter versionPresenter;
    DialogPresenter dialogPresenter;

	private final DispatchAsync dispatcher;
	private final TaPlaceManager placeManager;

	private final TableDataProvider dataProvider = new TableDataProvider();

	public interface MyView extends View, HasUiHandlers<RefBookDataUiHandlers> {
		void setTableColumns(final List<RefBookColumn> columns);
		void setTableData(int start, int totalCount, List<RefBookDataRow> dataRows);
		void setSelected(Long recordId);
		void assignDataProvider(int pageSize, AbstractDataProvider<RefBookDataRow> data);
        int getPageSize();
		void setRange(Range range);
		void updateTable();
		void setRefBookNameDesc(String desc);
        void resetRefBookElements();
		RefBookDataRow getSelectedRow();
		Date getRelevanceDate();
        int getPage();
        void setPage(int page);
        /** Метод для получения строки с поля фильтрации*/
        String getSearchPattern();
        /** Сброс значения поля поиска */
        void resetSearchInputBox();
        /** Обновление вьюшки для определенного состояния */
        void updateMode(FormMode mode);
        /** доступность  кнопки-ссылка "Создать запрос на изменение..." для справочника "Организации-участники контролируемых сделок" */
        void updateSendQuery(boolean isAvailable);
        //Показывает/скрывает поля, которые необходимы только для версионирования
        void setVersionedFields(boolean isVisible);
        // Номер столбца, по которому осуществляется сортировка
        int getSortColumnIndex();
        // Признак сортировки по-возрастанию
        boolean isAscSorting();
        void setDeleteButtonVisible(boolean isVisible);
        // позиция выделенной строки в таблице
        Integer getSelectedRowIndex();
    }

	@Inject
	public RefBookDataPresenter(final EventBus eventBus, final MyView view, EditFormPresenter editFormPresenter,
                                RefBookVersionPresenter versionPresenter, DialogPresenter dialogPresenter,
                                PlaceManager placeManager, final MyProxy proxy, DispatchAsync dispatcher) {
		super(eventBus, view, proxy, RevealContentTypeHolder.getMainContent());
		this.dispatcher = dispatcher;
		this.placeManager = (TaPlaceManager)placeManager;
		this.editFormPresenter = editFormPresenter;
        this.versionPresenter = versionPresenter;
        this.dialogPresenter = dialogPresenter;
		getView().setUiHandlers(this);
		getView().assignDataProvider(getView().getPageSize(), dataProvider);
	}

	@Override
	protected void onHide() {
		super.onHide();
		clearSlot(TYPE_editFormPresenter);
	}

	@Override
	protected void onReveal() {
		super.onReveal();
        LogCleanEvent.fire(this);
		setInSlot(TYPE_editFormPresenter, editFormPresenter);
	}

	@Override
	public void onUpdateForm(UpdateForm event) {
        if (event.isSuccess() && this.isVisible()) {
            getView().resetSearchInputBox();
            recordId = event.getRecordChanges().getId();
            getView().updateTable();
        }
	}

	@Override
	public void onRollbackTableRowSelection(RollbackTableRowSelection event) {
		getView().setSelected(event.getRecordId());
	}

	@Override
	public void onAddRowClicked() {
        //getView().updateMode(FormMode.CREATE);
        editFormPresenter.setMode(FormMode.CREATE);
		editFormPresenter.show(null);
	}

	@Override
	public void onDeleteRowClicked() {
		DeleteRefBookRowAction action = new DeleteRefBookRowAction();
		action.setRefBookId(refBookDataId);
		List<Long> rowsId = new ArrayList<Long>();
		rowsId.add(getView().getSelectedRow().getRefBookRowId());
		action.setRecordsId(rowsId);
        action.setDeleteVersion(false);
        LogCleanEvent.fire(RefBookDataPresenter.this);
		dispatcher.execute(action,
				CallbackUtils.defaultCallback(
						new AbstractCallback<DeleteRefBookRowResult>() {
							@Override
							public void onSuccess(DeleteRefBookRowResult result) {
                                if (!result.isCheckRegion()) {
                                    String title = "Удаление элемента справочника";
                                    String msg = "Отсутствуют права доступа на удаление записи для указанного региона!";
                                    Dialog.errorMessage(title, msg);
                                    return;
                                }
                                LogAddEvent.fire(RefBookDataPresenter.this, result.getUuid());
                                if (result.isException()) {
                                    Dialog.errorMessage("Удаление всех версий элемента справочника", "Обнаружены фатальные ошибки!");
                                }
                                editFormPresenter.setMode(mode);
								editFormPresenter.show(null);
                                selectedRowIndex = getView().getSelectedRowIndex();
								getView().updateTable();
							}
						}, this));
	}

	@Override
	public void onSelectionChanged() {
		if (getView().getSelectedRow() != null) {
            Long recordId = getView().getSelectedRow().getRefBookRowId();
            editFormPresenter.setRecordId(recordId);
            editFormPresenter.show(recordId);
        } else {
            editFormPresenter.setRecordId(null);
        }
	}

	@Override
	public void onRelevanceDateChanged() {
		getView().updateTable();
        editFormPresenter.setMode(mode);
		editFormPresenter.show(null);
	}

    @Override
    public void onBackClicked() {
        refBookDataId = null;
        recordId = null;
        placeManager.revealPlace(new PlaceRequest.Builder().nameToken(RefBookListTokens.REFBOOK_LIST).build());
    }

	@Override
	public void prepareFromRequest(final PlaceRequest request) {
		super.prepareFromRequest(request);

        selectedRowIndex = null;
        refBookDataId = Long.parseLong(request.getParameter(RefBookDataTokens.REFBOOK_DATA_ID, null));
        CheckRefBookAction checkAction = new CheckRefBookAction();
        checkAction.setRefBookId(refBookDataId);

        dispatcher.execute(checkAction,
                CallbackUtils.defaultCallback(
                        new AbstractCallback<CheckRefBookResult>() {
                            @Override
                            public void onSuccess(CheckRefBookResult result) {
                                if (result.isAvailable()) {
                                    getView().resetSearchInputBox();
                                    editFormPresenter.setVersionMode(false);
                                    editFormPresenter.setCurrentUniqueRecordId(null);
                                    editFormPresenter.setRecordId(null);
                                    GetRefBookAttributesAction action = new GetRefBookAttributesAction();

                                    action.setRefBookId(refBookDataId);
                                    dispatcher.execute(action,
                                            CallbackUtils.defaultCallback(
                                                    new AbstractCallback<GetRefBookAttributesResult>() {
                                                        @Override
                                                        public void onSuccess(GetRefBookAttributesResult result) {
                                                            getView().resetRefBookElements();
                                                            getView().setTableColumns(result.getColumns());
                                                            getView().updateSendQuery(result.isSendQuery());
                                                            editFormPresenter.init(refBookDataId, result.getColumns());
                                                            if (result.isReadOnly()){
                                                                mode = FormMode.READ;
                                                            }
                                                            if (request.getParameterNames().contains(RefBookDataTokens.REFBOOK_RECORD_ID)) {
                                                                recordId = Long.parseLong(request.getParameter(RefBookDataTokens.REFBOOK_RECORD_ID, null));
                                                                if (mode == null) {
                                                                    mode = FormMode.VIEW;
                                                                }
                                                                setMode(mode);
                                                            } else {
                                                                recordId = null;
                                                                getView().resetSearchInputBox();
                                                                if (!result.isReadOnly()) {
                                                                    mode = FormMode.VIEW;
                                                                }
                                                                setMode(mode);
                                                            }
                                                            getView().setRange(new Range(0, getView().getPageSize()));
                                                            getProxy().manualReveal(RefBookDataPresenter.this);
                                                        }
                                                    }, RefBookDataPresenter.this));

                                    GetNameAction nameAction = new GetNameAction();
                                    nameAction.setRefBookId(refBookDataId);
                                    dispatcher.execute(nameAction,
                                            CallbackUtils.defaultCallback(
                                                    new AbstractCallback<GetNameResult>() {
                                                        @Override
                                                        public void onSuccess(GetNameResult result) {
                                                            getView().setRefBookNameDesc(result.getName());
                                                        }
                                                    }, RefBookDataPresenter.this));
                                    getView().setVersionedFields(!Arrays.asList(RefBookDataModule.NOT_VERSIONED_REF_BOOK_IDS).contains(refBookDataId));
                                    editFormPresenter.setCanVersion(!Arrays.asList(RefBookDataModule.NOT_VERSIONED_REF_BOOK_IDS).contains(refBookDataId));
                                    versionPresenter.setHierarchy(false);
                                } else {
                                    getProxy().manualReveal(RefBookDataPresenter.this);
                                    Dialog.errorMessage("Доступ к справочнику запрещен!");
                                }
                            }
                        }, this));
	}

	@Override
	public void onBind(){
        super.onBind();
		addRegisteredHandler(UpdateForm.getType(), this);
		addRegisteredHandler(RollbackTableRowSelection.getType(), this);
        addRegisteredHandler(SetFormMode.getType(), this);
    }

	private class TableDataProvider extends AsyncDataProvider<RefBookDataRow> {

		@Override
		protected void onRangeChanged(HasData<RefBookDataRow> display) {
			if (refBookDataId == null) return;
			final Range range = display.getVisibleRange();
			GetRefBookTableDataAction action = new GetRefBookTableDataAction();
            action.setRecordId(recordId);
			action.setRefBookId(refBookDataId);
			action.setPagingParams(new PagingParams(range.getStart() + 1, range.getLength()));
			action.setRelevanceDate(getView().getRelevanceDate());
            action.setSearchPattern(getView().getSearchPattern());
            action.setSortColumnIndex(getView().getSortColumnIndex());
            action.setAscSorting(getView().isAscSorting());
			dispatcher.execute(action,
					CallbackUtils.defaultCallback(
							new AbstractCallback<GetRefBookTableDataResult>() {
								@Override
								public void onSuccess(GetRefBookTableDataResult result) {
                                    if (result.getRowNum() != null) {
                                        int page = (int)((result.getRowNum() - 1)/range.getLength());
                                        if (page != getView().getPage()) {
                                            getView().setPage(page);
                                            return ;
                                        }
                                    }
									getView().setTableData(range.getStart(),
                                            result.getTotalCount(), result.getDataRows());
                                    // http://jira.aplana.com/browse/SBRFACCTAX-5684 автофокус на первую строку
                                    if (recordId == null && !result.getDataRows().isEmpty()) {
                                        getView().setSelected(result.getDataRows().get(0).getRefBookRowId());
                                    } else if(result.getDataRows().isEmpty()){
                                        editFormPresenter.cleanFields();
                                        editFormPresenter.clearRecordId();
                                        if (mode == FormMode.EDIT)
                                            getView().setDeleteButtonVisible(false);
                                    }
                                    // http://jira.aplana.com/browse/SBRFACCTAX-5759
                                    if (recordId != null) {
                                        getView().setSelected(recordId);
                                    }
                                    recordId = null;
                                    if (selectedRowIndex != null && result.getDataRows().size() > selectedRowIndex) {
                                        //сохраняем позицию после удаления записи
                                        getView().setSelected(result.getDataRows().get(selectedRowIndex.intValue()).getRefBookRowId());
                                    }
                                    selectedRowIndex = null;
                                    if (result.getDataRows().size() == 0) {
                                        editFormPresenter.setAllVersionVisible(false);
                                    }
                                }
							}, RefBookDataPresenter.this));
		}
	}

    @Override
    public boolean useManualReveal() {
        return true;
    }

    @Override
    public void setMode(FormMode mode) {
        this.mode = mode;
        editFormPresenter.setMode(mode);
        versionPresenter.setMode(mode);
        getView().updateMode(mode);
    }

    @Override
    public void saveChanges() {
        editFormPresenter.onSaveClicked(true);
    }

    @Override
    public void cancelChanges() {
        editFormPresenter.setIsFormModified(false);
        editFormPresenter.onCancelClicked();
    }

    @Override
    public boolean isFormModified() {
        return editFormPresenter.isFormModified();
    }

    @Override
    public void sendQuery() {
        addToPopupSlot(dialogPresenter);
    }

    @Override
    public void onReset(){
        this.dialogPresenter.getView().hide();
    }

    @Override
    public void onSetFormMode(SetFormMode event) {
        setMode(event.getFormMode());
    }
}
