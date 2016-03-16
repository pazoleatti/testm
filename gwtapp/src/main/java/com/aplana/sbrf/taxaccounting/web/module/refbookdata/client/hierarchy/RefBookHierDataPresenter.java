package com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.hierarchy;

import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.gwt.client.dialog.DialogHandler;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogAddEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogCleanEvent;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.editform.event.RollbackTableRowSelection;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.editform.event.UpdateForm;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.event.AddItemEvent;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.event.DeleteItemEvent;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.event.SearchButtonEvent;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.event.ShowItemEvent;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.*;
import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.shared.model.PickerState;
import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.shared.model.RefBookTreeItem;
import com.aplana.sbrf.taxaccounting.web.widget.utils.WidgetUtils;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.ProxyEvent;

import java.util.Arrays;
import java.util.Date;

/**
 * Презентор-виджет формы иерархического справочника
 *
 * @author aivanov
 */
public class RefBookHierDataPresenter extends PresenterWidget<RefBookHierDataPresenter.MyView> implements RefBookHierDataUiHandlers,
        RollbackTableRowSelection.RollbackTableRowSelectionHandler, IHierRefBookData,
        AddItemEvent.AddItemHandler, DeleteItemEvent.DeleteItemHandler, SearchButtonEvent.SearchHandler, UpdateForm.UpdateFormHandler{

    private Long recordId;
    boolean canVersion = true;
    private Long refBookId;
    private RecordChanges recordChanges;
    private Date relevanceDate;

    private final DispatchAsync dispatcher;

    @ProxyEvent
    @Override
    public void onAddItem(AddItemEvent event) {
        onAddRowClicked();
    }

    @ProxyEvent
    @Override
    public void onDeleteItem(DeleteItemEvent event) {
        onDeleteRowClicked();
    }

    @ProxyEvent
    @Override
    public void onSearch(SearchButtonEvent event) {
        getView().setPickerState(event.getRelevanceDate(), event.getSearchPattern());
        searchButtonClicked(event.getRelevanceDate());
    }

    @ProxyEvent
    @Override
    public void onUpdateForm(UpdateForm event) {
        if (event.isSuccess()) {
            getView().clearFilterInputBox();
                    /*refBookHierDataPresenter.clearFilterInputBox();*/
            RecordChanges rc = event.getRecordChanges();
            setRecordItem(rc);
            if (canVersion && !WidgetUtils.isInLimitPeriod(rc.getStart(), rc.getEnd(), relevanceDate)) {
                        /*refBookHierDataPresenter.setRecordItem(rc);
                        refBookHierDataPresenter.deleteItem();*/
                getView().deleteItem(rc.getId());
            } else {
                getView().clearSelected();
                updateTree();
            }
            /*getView().setSelected(rc.getId());*/
        }
    }

    public interface MyView extends View, HasUiHandlers<RefBookHierDataUiHandlers> {

        void setSelected(Long recordId);

        void setSelection(RefBookTreeItem parentRefBookItem);

        void clearSelected();

        void load();

        void loadAndSelect();

        void reload();

        Long getSelectedId();

        RefBookTreeItem getSelectedItem();

        RefBookTreeItem getItemById(Long recordId);

        void deleteItem(Long id);

        void updateItem(Long id, Long newParentId, String newName);

        void setAttributeId(Long attrId);

        void setPickerState(Date relevanceDate, String searchPattern);

        PickerState getPickerState();

        /** Обновление вьюшки для определенного состояния */
        void updateMode(FormMode mode);
        /** Очистить */
        void clearFilterInputBox();

        void searchButtonClicked(Date relevanceDate);
    }

    @Inject
    public RefBookHierDataPresenter(final EventBus eventBus, final MyView view,
                                    DispatchAsync dispatcher) {
        super(eventBus, view);
        this.dispatcher = dispatcher;
        getView().setUiHandlers(this);
    }

    @Override
    public void onBind() {
        addVisibleHandler(UpdateForm.getType(), this);
        addVisibleHandler(AddItemEvent.getType(), this);
        addVisibleHandler(DeleteItemEvent.getType(), this);
        addVisibleHandler(SearchButtonEvent.getType(), this);
        addVisibleHandler(RollbackTableRowSelection.getType(), this);
    }

    @Override
    protected void onUnbind() {
        super.onUnbind();
    }

    @Override
    protected void onReveal() {
        super.onReveal();
        CheckRefBookAction checkAction = new CheckRefBookAction();
        checkAction.setRefBookId(refBookId);
        dispatcher.execute(checkAction, CallbackUtils.defaultCallback(
                new AbstractCallback<CheckRefBookResult>() {
                    @Override
                    public void onSuccess(CheckRefBookResult result) {
                        if (!result.isAvailable()) {
                            Dialog.errorMessage("Доступ к справочнику запрещен!");
                        }
                    }
                }, this));
    }

    @Override
    protected void onHide() {
        super.onHide();
        /*clearSlot(TYPE_editFormPresenter);*/
    }

    @Override
    public void onRollbackTableRowSelection(RollbackTableRowSelection event) {
        RefBookTreeItem parentRefBookItem = getView().getItemById(event.getRecordId());
        getView().setSelection(parentRefBookItem);
    }

    private void onAddRowClicked() {
        RefBookTreeItem item = getView().getSelectedItem();
        ShowItemEvent.fire(this, item.getDereferenceValue(), item.getId());
        //editPresenter.show(item.getDereferenceValue(), item.getId());
        //getView().clearSelected();
    }

    private void onDeleteRowClicked() {
        if(getView().getSelectedItem()==null){
            return;
        }
        LogCleanEvent.fire(RefBookHierDataPresenter.this);
        //TODO: Подумать, может убрать совсем либо DeleteRefBookRowAction, либо DeleteNonVersionRefBookRowAction
        if (refBookId != 30){
            DeleteRefBookRowAction action = new DeleteRefBookRowAction();
            action.setRefBookId(refBookId);
            final Long selected = getView().getSelectedId();
            action.setRecordsId(Arrays.asList(selected));
            action.setDeleteVersion(false);
            final RefBookTreeItem parentRefBookItem = getView().getSelectedItem().getParent();
            dispatcher.execute(action, CallbackUtils.defaultCallback(
                    new AbstractCallback<DeleteRefBookRowResult>() {
                        @Override
                        public void onSuccess(DeleteRefBookRowResult result) {
                            if (!result.isCheckRegion()) {
                                String title = "Удаление элемента справочника";
                                String msg = "Отсутствуют права доступа на удаление записи для указанного региона!";
                                Dialog.errorMessage(title, msg);
                                return;
                            }
                            LogAddEvent.fire(RefBookHierDataPresenter.this, result.getUuid());
                            if (result.isException()) {
                                Dialog.errorMessage("Удаление записи справочника",
                                        "Обнаружены фатальные ошибки!");
                            } else {
                                getView().deleteItem(selected);
                                getView().setSelection(parentRefBookItem);
                                if (parentRefBookItem == null)
                                    ShowItemEvent.fire(RefBookHierDataPresenter.this, null, null);
                                //editPresenter.show(null);
                                /*editPresenter.clean();
                                editPresenter.setNeedToReload();*/
                            }
                        }
                    }, this));
        } else {
            DeleteNonVersionRefBookRowAction action = new DeleteNonVersionRefBookRowAction();
            final Long selected = getView().getSelectedId();
            action.setRecordsId(Arrays.asList(selected));
            action.setOkDelete(false);
            action.setRefBookId(refBookId);
            final RefBookTreeItem parentRefBookItem = getView().getSelectedItem().getParent();
            dispatcher.execute(action, CallbackUtils.defaultCallback(new AbstractCallback<DeleteNonVersionRefBookRowResult>() {
                @Override
                public void onSuccess(DeleteNonVersionRefBookRowResult result) {
                    if (result.isWarning()){
                        LogAddEvent.fire(RefBookHierDataPresenter.this, result.getUuid());
                        Dialog.confirmMessage("Удаление подразделения","Удалить все связанные записи?", new DialogHandler() {
                            @Override
                            public void yes() {
                                DeleteNonVersionRefBookRowAction action = new DeleteNonVersionRefBookRowAction();
                                final Long selected = getView().getSelectedId();
                                action.setRecordsId(Arrays.asList(selected));
                                action.setOkDelete(true);
                                action.setRefBookId(refBookId);
                                dispatcher.execute(action, CallbackUtils.defaultCallback(new AbstractCallback<DeleteNonVersionRefBookRowResult>() {
                                    @Override
                                    public void onSuccess(DeleteNonVersionRefBookRowResult result) {
                                        LogAddEvent.fire(RefBookHierDataPresenter.this, result.getUuid());
                                        //editPresenter.show(null);
                                        ShowItemEvent.fire(RefBookHierDataPresenter.this, null, null);
                                        /*editPresenter.clean();
                                        editPresenter.setNeedToReload();*/
                                        getView().deleteItem(selected);
                                        getView().setSelection(parentRefBookItem);
                                    }
                                }, RefBookHierDataPresenter.this));
                            }

                            @Override
                            public void no() {
                                Dialog.hideMessage();
                            }
                        });
                    } else {
                        LogAddEvent.fire(RefBookHierDataPresenter.this, result.getUuid());
                        //editPresenter.show(null);
                        ShowItemEvent.fire(RefBookHierDataPresenter.this, null, null);
                        /*editPresenter.clean();
                        editPresenter.setNeedToReload();*/
                        getView().deleteItem(selected);
                        getView().setSelection(parentRefBookItem);
                    }
                }
            }, RefBookHierDataPresenter.this));
        }

    }

    @Override
    public void onSelectionChanged() {
        if (getView().getSelectedId() != null) {
            recordId = getView().getSelectedId();
            ShowItemEvent.fire(RefBookHierDataPresenter.this, null, recordId);
            /*editPresenter.show(recordId);
            editPresenter.setRecordId(recordId);*/
        }
    }

    public Long getSelectedId(){
        return getView().getSelectedId();
    }

    @Override
    public void onRelevanceDateChanged() {
        ShowItemEvent.fire(RefBookHierDataPresenter.this, null, null);
        /*editPresenter.clean();
        editPresenter.setNeedToReload();*/
        getView().load();
    }

    @Override
    public void updateTree() {
        RefBookTreeItem selectedItem = getView().getSelectedItem();
        if (selectedItem != null) {
            String sName = selectedItem.getDereferenceValue();
            Long sParentId = selectedItem.getParent() != null ? selectedItem.getParent().getId() : null;

            if (WidgetUtils.isWasChange(sName, recordChanges.getName()) ||
                    WidgetUtils.isWasChange(sParentId, recordChanges.getParentId())) {
                // обновляем если только есть изменения
                getView().updateItem(recordChanges.getId(), recordChanges.getParentId(), recordChanges.getName());
            }
        } else {
            // добавление записи rc.getId() ==null
            getView().updateItem(recordChanges.getId(), recordChanges.getParentId(), recordChanges.getName());
        }
    }

    @Override
    public void setMode(FormMode mode){
        getView().updateMode(mode);
    }

    private void setRecordItem(RecordChanges recordChanges) {
        this.recordChanges = recordChanges;
    }

    @Override
    public void setAttributeId(Long attrId) {
        getView().setAttributeId(attrId);
    }

    public void initPickerState(Date relevanceDate, String searchPattern) {
        this.relevanceDate = relevanceDate;
        getView().setPickerState(relevanceDate, searchPattern);
    }

    @Override
    public void onCleanEditForm() {
        ShowItemEvent.fire(RefBookHierDataPresenter.this, null, null);
       /* editPresenter.cleanFields();
        editPresenter.setCurrentUniqueRecordId(null);*/
        //editPresenter.setAllVersionVisible(false);
    }

    @Override
    public void setRefBookId(Long refBookId){
        this.refBookId = refBookId;
    }

    public void clearAll() {
        getView().clearFilterInputBox();
        getView().clearSelected();
    }

    public void loadAndSelect() {
        getView().loadAndSelect();
    }

    private void searchButtonClicked(Date relevanceDate){
        getView().searchButtonClicked(relevanceDate);
    }

    /*private void checkRecord() {
        CheckRecordExistenceAction action = new CheckRecordExistenceAction();
        action.setRefBookId(refBookId);
        action.setRecordId(recordId);
        dispatcher.execute(action, CallbackUtils.defaultCallback(
                new AbstractCallback<CheckRecordExistenceResult>() {
                    @Override
                    public void onSuccess(CheckRecordExistenceResult result) {
                        if (result.isRecordExistence()) {
                            recordId = null;
                            getView().reload();
                            getView().loadAndSelect();
                        } else {
                            getView().reload();
                            getView().setSelected(recordId);
                            ShowItemEvent.fire(RefBookHierDataPresenter.this, null, recordId);
                            //editPresenter.show(recordId);
                        }
                    }
                }, this));
    }*/


    public Date getRelevanceDate() {
        return getView().getPickerState().getVersionDate();
    }

    public String getSearchPattern() {
        return getView().getPickerState().getSearchPattern();
    }
}
