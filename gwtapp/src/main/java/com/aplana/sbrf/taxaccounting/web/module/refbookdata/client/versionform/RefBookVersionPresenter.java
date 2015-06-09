package com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.versionform;

import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookType;
import com.aplana.sbrf.taxaccounting.web.main.api.client.RevealContentTypeHolder;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogAddEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogCleanEvent;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.FormMode;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.RefBookDataTokens;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.editform.EditFormPresenter;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.editform.event.RollbackTableRowSelection;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.editform.event.SetFormMode;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.editform.event.UpdateForm;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.*;
import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.shared.model.RefBookTreeItem;
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
import java.util.List;

public class RefBookVersionPresenter extends Presenter<RefBookVersionPresenter.MyView,
		RefBookVersionPresenter.MyProxy> implements RefBookVersionUiHandlers,
		UpdateForm.UpdateFormHandler,  RollbackTableRowSelection.RollbackTableRowSelectionHandler, SetFormMode.SetFormModeHandler{

    @Override
    public void onSetFormMode(SetFormMode event) {
        setMode(event.getFormMode());
    }

    @ProxyCodeSplit
	@NameToken(RefBookDataTokens.refBookVersion)
	public interface MyProxy extends ProxyPlace<RefBookVersionPresenter>, Place {
	}

	static final Object TYPE_editFormPresenter = new Object();

	private Long refBookId;
    //Идентификатор записи
    private Long uniqueRecordId;
    //Идентификатор версии
    private Long recordId;
    private FormMode mode;
    private boolean isHierarchy = false;
    private Integer selectedRowIndex;

    public void setHierarchy(boolean hierarchy) {
        isHierarchy = hierarchy;
    }

    EditFormPresenter editFormPresenter;

	private final DispatchAsync dispatcher;
	private final PlaceManager placeManager;

    private RefBookType refBookType;

    public interface MyView extends View, HasUiHandlers<RefBookVersionUiHandlers> {
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
        void setTitleDetails(String uniqueAttrValues);

        /**
         * Ссылка для возвращения на форму справочников
         * @param refBookType тип !refbookhier или !refbook
         * @param record RefBookDataTokens.REFBOOK_RECORD_ID, но вдруг поменяется
         */
        void setBackAction(String refBookType, long refBookId, String record, long recordId);
        void updateMode(FormMode mode);
        // позиция выделенной строки в таблице
        Integer getSelectedRowIndex();
    }

	@Inject
	public RefBookVersionPresenter(final EventBus eventBus, final MyView view, EditFormPresenter editFormPresenter, PlaceManager placeManager, final MyProxy proxy,
                                   DispatchAsync dispatcher) {
		super(eventBus, view, proxy, RevealContentTypeHolder.getMainContent());
		this.dispatcher = dispatcher;
		this.placeManager = placeManager;
		this.editFormPresenter = editFormPresenter;
		getView().setUiHandlers(this);
        TableDataProvider dataProvider = new TableDataProvider();
        getView().assignDataProvider(getView().getPageSize(), dataProvider);
        setMode(FormMode.READ);
	}

	@Override
	protected void onHide() {
		super.onHide();
		clearSlot(TYPE_editFormPresenter);
	}

	@Override
	protected void onReveal() {
		super.onReveal();
		setInSlot(TYPE_editFormPresenter, editFormPresenter);
	}

	@Override
	public void onUpdateForm(UpdateForm event) {
        if (this.isVisible()) {
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
        setMode(FormMode.CREATE);
        editFormPresenter.setMode(FormMode.CREATE);
        if (isHierarchy){
            //http://jira.aplana.com/browse/SBRFACCTAX-10062
            GetLastVersionHierarchyAction action
                    = new GetLastVersionHierarchyAction();
            action.setRefBookId(refBookId);
            action.setRefBookRecordId(uniqueRecordId);
            dispatcher.execute(action, CallbackUtils.defaultCallback(new AbstractCallback<GetLastVersionHierarchyResult>() {
                @Override
                public void onSuccess(GetLastVersionHierarchyResult result) {
                    editFormPresenter.show(null, new RefBookTreeItem(
                            result.getDataRow().getRefBookRowId(),
                            result.getDataRow().getValues().get("PARENT_ID"))
                    );
                }
            }, this));
        }
        else{
            editFormPresenter.show(null);
        }
	}

	@Override
	public void onDeleteRowClicked() {
		DeleteRefBookRowAction action = new DeleteRefBookRowAction();
		action.setRefBookId(refBookId);
		List<Long> rowsId = new ArrayList<Long>();
        Long deletedVersion = getView().getSelectedRow().getRefBookRowId();
		rowsId.add(deletedVersion);
		action.setRecordsId(rowsId);
        action.setDeleteVersion(true);
        selectedRowIndex = getView().getSelectedRowIndex();
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
                                LogCleanEvent.fire(RefBookVersionPresenter.this);
                                LogAddEvent.fire(RefBookVersionPresenter.this, result.getUuid());
                                editFormPresenter.show(null);
                                if (result.getNextVersion() != null) {
                                    getView().updateTable();

                                    getView().setBackAction(
                                            refBookType.equals(RefBookType.LINEAR) ?
                                                    RefBookDataTokens.refBookData :
                                                    RefBookDataTokens.refBookHierData, refBookId, RefBookDataTokens.REFBOOK_RECORD_ID, result.getNextVersion());
                                } else {
                                    placeManager
                                            .revealPlace(new PlaceRequest.Builder().nameToken(isHierarchy ? RefBookDataTokens.refBookHierData : RefBookDataTokens.refBookData)
                                                    .with(RefBookDataTokens.REFBOOK_DATA_ID, String.valueOf(refBookId))
                                                    .build());
                                }
							}
						}, this));
	}

	@Override
	public void onSelectionChanged() {
		if (getView().getSelectedRow() != null) {
            editFormPresenter.show(getView().getSelectedRow().getRefBookRowId());
        }
	}

	@Override
	public void prepareFromRequest(final PlaceRequest request) {
		super.prepareFromRequest(request);
        selectedRowIndex = null;
        refBookId = Long.parseLong(request.getParameter(RefBookDataTokens.REFBOOK_DATA_ID, null));
        CheckRefBookAction checkAction = new CheckRefBookAction();
        checkAction.setRefBookId(refBookId);
        dispatcher.execute(checkAction, CallbackUtils.defaultCallback(
                new AbstractCallback<CheckRefBookResult>() {
                    @Override
                    public void onSuccess(CheckRefBookResult result) {
                        if (result.isAvailable()) {
                            uniqueRecordId = Long.parseLong(request.getParameter(RefBookDataTokens.REFBOOK_RECORD_ID, null));

                            editFormPresenter.setVersionMode(true);
                            editFormPresenter.setCurrentUniqueRecordId(null);
                            editFormPresenter.setRecordId(null);

                            GetRefBookAttributesAction action = new GetRefBookAttributesAction();
                            action.setRefBookId(refBookId);
                            dispatcher.execute(action,
                                    CallbackUtils.defaultCallback(
                                            new AbstractCallback<GetRefBookAttributesResult>() {
                                                @Override
                                                public void onSuccess(GetRefBookAttributesResult result) {
                                                    getView().resetRefBookElements();
                                                    getView().setTableColumns(result.getColumns());
                                                    editFormPresenter.init(refBookId, result.getColumns());
                                                    editFormPresenter.setMode(mode);
                                                    editFormPresenter.show(uniqueRecordId);
                                                    getView().setRange(new Range(0, getView().getPageSize()));
                                                    if (result.isReadOnly()){
                                                        setMode(FormMode.READ);
                                                    }
                                                    //editFormPresenter.init(refBookId);
                                                    getProxy().manualReveal(RefBookVersionPresenter.this);
                                                }
                                            }, RefBookVersionPresenter.this));

                            GetNameAction nameAction = new GetNameAction();
                            nameAction.setRefBookId(refBookId);
                            nameAction.setUniqueRecordId(uniqueRecordId);
                            dispatcher.execute(nameAction,
                                    CallbackUtils.defaultCallback(
                                            new AbstractCallback<GetNameResult>() {
                                                @Override
                                                public void onSuccess(GetNameResult result) {
                                                    getView().setRefBookNameDesc(result.getName());
                                                    getView().setTitleDetails(result.getUniqueAttributeValues());
                                                    refBookType = RefBookType.get(result.getRefBookType());
                                                    getView().setBackAction(
                                                            refBookType.equals(RefBookType.LINEAR) ?
                                                                    RefBookDataTokens.refBookData :
                                                                    RefBookDataTokens.refBookHierData, refBookId, RefBookDataTokens.REFBOOK_RECORD_ID, uniqueRecordId);
                                                    editFormPresenter.setRecordId(result.getRecordId());
                                                }
                                            }, RefBookVersionPresenter.this));
                        } else {
                            getProxy().manualReveal(RefBookVersionPresenter.this);
                            Dialog.errorMessage("Доступ к справочнику запрещен!");
                        }
                    }
                }, this));
	}

	@Override
	public void onBind(){
		addRegisteredHandler(UpdateForm.getType(), this);
		addRegisteredHandler(RollbackTableRowSelection.getType(), this);
        addRegisteredHandler(SetFormMode.getType(), this);
	}

	private class TableDataProvider extends AsyncDataProvider<RefBookDataRow> {

		@Override
		protected void onRangeChanged(HasData<RefBookDataRow> display) {
			if (refBookId == null) return;
			final Range range = display.getVisibleRange();
            GetRefBookRecordVersionAction action = new GetRefBookRecordVersionAction();
            action.setRefBookId(refBookId);
            action.setRefBookRecordId(uniqueRecordId);
			action.setPagingParams(new PagingParams(range.getStart() + 1, range.getLength()));
			dispatcher.execute(action,
					CallbackUtils.defaultCallback(
							new AbstractCallback<GetRefBookRecordVersionResult>() {
								@Override
								public void onSuccess(GetRefBookRecordVersionResult result) {
									getView().setTableData(range.getStart(),
											result.getTotalCount(), result.getDataRows());
                                    if (recordId == null && !result.getDataRows().isEmpty()) {
                                        getView().setSelected(result.getDataRows().get(0).getRefBookRowId());
                                        // recordCommonId = result.getRefBookRecordCommonId();
                                    } else if (recordId != null){
                                        getView().setSelected(recordId);
                                    }
                                    recordId = null;
                                    if (selectedRowIndex != null && result.getDataRows().size() > selectedRowIndex) {
                                        //сохраняем позицию после удаления записи
                                        getView().setSelected(result.getDataRows().get(selectedRowIndex).getRefBookRowId());
                                    }
                                    selectedRowIndex = null;
								}
							}, RefBookVersionPresenter.this));
		}
	}

    @Override
    public boolean useManualReveal() {
        return true;
    }

    @Override
    public void setMode(FormMode mode) {
        this.mode = mode;
        getView().updateMode(mode);
    }
}
