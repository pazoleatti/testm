package com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.hierarchy;

import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.gwt.client.dialog.DialogHandler;
import com.aplana.sbrf.taxaccounting.web.main.api.client.RevealContentTypeHolder;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogAddEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogCleanEvent;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.EditForm.EditFormPresenter;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.EditForm.event.RollbackTableRowSelection;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.EditForm.event.UpdateForm;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.FormMode;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.RefBookDataModule;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.RefBookDataTokens;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.VersionForm.RefBookVersionPresenter;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.*;
import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.shared.model.RefBookTreeItem;
import com.aplana.sbrf.taxaccounting.web.widget.utils.WidgetUtils;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.Place;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;

import java.util.Arrays;
import java.util.Date;

/**
 * Презентор формы редактирования иерархического справочника
 *
 * @author aivanov
 */
public class RefBookHierDataPresenter extends Presenter<RefBookHierDataPresenter.MyView,
        RefBookHierDataPresenter.MyProxy> implements RefBookHierDataUiHandlers,
        UpdateForm.UpdateFormHandler, RollbackTableRowSelection.RollbackTableRowSelectionHandler {

    @ProxyCodeSplit
    @NameToken(RefBookDataTokens.refBookHierData)
    public interface MyProxy extends ProxyPlace<RefBookHierDataPresenter>, Place {
    }

    static final Object TYPE_editFormPresenter = new Object();

    Long refBookDataId;

    private Long recordId;

    private FormMode mode;

    boolean canVersion = true;

    EditFormPresenter editFormPresenter;
    RefBookVersionPresenter versionPresenter;

    private final DispatchAsync dispatcher;

    public interface MyView extends View, HasUiHandlers<RefBookHierDataUiHandlers> {

        void setSelected(Long recordId);

        void load();

        void loadAndSelect();

        void reload();

        void setRefBookNameDesc(String desc);

        Long getSelectedId();

        RefBookTreeItem getSelectedItem();

        void deleteItem(Long id);

        void updateItem(Long id, Long newParentId, String newName);

        Date getRelevanceDate();

        void setAttributeId(Long attrId);

        /** Обновление вьюшки для определенного состояния */
        void updateMode(FormMode mode);
        /** Очистить */
        void clearFilterInputBox();

        //Показывает/скрывает поля, которые необходимы только для версионирования
        void setVersionedFields(boolean isVisible);
    }

    @Inject
    public RefBookHierDataPresenter(final EventBus eventBus, final MyView view, EditFormPresenter editFormPresenter,
                                    RefBookVersionPresenter versionPresenter, final MyProxy proxy, DispatchAsync dispatcher) {
        super(eventBus, view, proxy, RevealContentTypeHolder.getMainContent());
        this.dispatcher = dispatcher;
        this.editFormPresenter = editFormPresenter;
        this.versionPresenter = versionPresenter;
        this.mode = FormMode.VIEW;
        getView().setUiHandlers(this);
    }

    @Override
    public void onBind() {
        addRegisteredHandler(UpdateForm.getType(), this);
        addRegisteredHandler(RollbackTableRowSelection.getType(), this);
    }

    @Override
    protected void onReveal() {
        super.onReveal();
        setInSlot(TYPE_editFormPresenter, editFormPresenter);
    }

    @Override
    protected void onHide() {
        super.onHide();
        clearSlot(TYPE_editFormPresenter);
    }

    @Override
    public void onUpdateForm(UpdateForm event) {
        if (event.isSuccess() && this.isVisible()) {
            getView().clearFilterInputBox();
            RecordChanges rc = event.getRecordChanges();
            RefBookTreeItem selectedItem = getView().getSelectedItem();
            if (canVersion && !WidgetUtils.isInLimitPeriod(rc.getStart(), rc.getEnd(), getView().getRelevanceDate())) {
                getView().deleteItem(rc.getId());
            } else {
                if (selectedItem != null) {
                    String sName = selectedItem.getDereferenceValue();
                    Long sParentId = selectedItem.getParent() != null ? selectedItem.getParent().getId() : null;

                    if (WidgetUtils.isWasChange(sName, rc.getName()) ||
                            WidgetUtils.isWasChange(sParentId, rc.getParentId())) {
                        // обновляем если только есть изменения
                        getView().updateItem(rc.getId(), rc.getParentId(), rc.getName());
                    }
                } else {
                    // добавление записи rc.getId() ==null
                    getView().updateItem(rc.getId(), rc.getParentId(), rc.getName());
                }
            }
            getView().setSelected(rc.getId());
        }
    }

    @Override
    public void onRollbackTableRowSelection(RollbackTableRowSelection event) {
        getView().setSelected(event.getRecordId());
    }

    @Override
    public void onAddRowClicked() {
        editFormPresenter.show(null);
    }

    @Override
    public void onDeleteRowClicked() {
        if (canVersion){
            DeleteRefBookRowAction action = new DeleteRefBookRowAction();
            action.setRefBookId(refBookDataId);
            final Long selected = getView().getSelectedId();
            action.setRecordsId(Arrays.asList(selected));
            action.setDeleteVersion(false);
            dispatcher.execute(action, CallbackUtils.defaultCallback(
                    new AbstractCallback<DeleteRefBookRowResult>() {
                        @Override
                        public void onSuccess(DeleteRefBookRowResult result) {
                            LogCleanEvent.fire(RefBookHierDataPresenter.this);
                            LogAddEvent.fire(RefBookHierDataPresenter.this, result.getUuid());
                            if (result.isException()) {
                                Dialog.errorMessage("Удаление всех версий элемента справочника",
                                        "Обнаружены фатальные ошибки!");
                            } else {
                                editFormPresenter.show(null);
                                editFormPresenter.setNeedToReload();
                                getView().deleteItem(selected);
                            }
                        }
                    }, this));
        } else {
            DeleteNonVersionRefBookRowAction action = new DeleteNonVersionRefBookRowAction();
            final Long selected = getView().getSelectedId();
            action.setRecordsId(Arrays.asList(selected));
            action.setOkDelete(false);
            action.setRefBookId(refBookDataId);
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
                                action.setRefBookId(refBookDataId);
                                dispatcher.execute(action, CallbackUtils.defaultCallback(new AbstractCallback<DeleteNonVersionRefBookRowResult>() {
                                    @Override
                                    public void onSuccess(DeleteNonVersionRefBookRowResult result) {
                                        LogAddEvent.fire(RefBookHierDataPresenter.this, result.getUuid());
                                        editFormPresenter.show(null);
                                        editFormPresenter.setNeedToReload();
                                        getView().deleteItem(selected);
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
                        editFormPresenter.show(null);
                        editFormPresenter.setNeedToReload();
                        getView().deleteItem(selected);
                    }
                }
            }, RefBookHierDataPresenter.this));
        }

    }

    @Override
    public void onSelectionChanged() {
        if (getView().getSelectedId() != null) {
            recordId = getView().getSelectedId();
            editFormPresenter.show(recordId);
        }
    }

    @Override
    public void onRelevanceDateChanged() {
        editFormPresenter.setRelevanceDate(getView().getRelevanceDate());
        editFormPresenter.show(null);
        editFormPresenter.setNeedToReload();
        getView().load();
    }

    @Override
    public void prepareFromRequest(final PlaceRequest request) {
        super.prepareFromRequest(request);

        editFormPresenter.setVersionMode(false);
        editFormPresenter.setCurrentUniqueRecordId(null);
        editFormPresenter.setRecordId(null);

        /** Очищаем поле поиска если перешли со страницы списка справочников */
        if (!request.getParameterNames().contains(RefBookDataTokens.REFBOOK_RECORD_ID)) {
            if (mode == FormMode.EDIT) mode = FormMode.VIEW;
            updateMode();
            getView().clearFilterInputBox();
            recordId = null;
        } else {
            updateMode();
            recordId = Long.parseLong(request.getParameter(RefBookDataTokens.REFBOOK_RECORD_ID, null));
        }

        refBookDataId = Long.parseLong(request.getParameter(RefBookDataTokens.REFBOOK_DATA_ID, null));

        GetRefBookAttributesAction action = new GetRefBookAttributesAction(refBookDataId);
        dispatcher.execute(action, CallbackUtils.defaultCallback(
                new AbstractCallback<GetRefBookAttributesResult>() {
                    @Override
                    public void onSuccess(GetRefBookAttributesResult result) {
                        Long attrId = null;
                        for (RefBookColumn refBookColumn : result.getColumns()) {
                            if(refBookColumn.getAlias().toLowerCase().equals("name")){
                                attrId = refBookColumn.getId();
                            }
                        }
                        getView().setAttributeId(attrId);
                        editFormPresenter.init(refBookDataId, result.isReadOnly());
                        if (recordId == null) {
                            getView().reload();
                            getView().loadAndSelect();
                        } else {
                            checkRecord();
                        }
                        getProxy().manualReveal(RefBookHierDataPresenter.this);
                        if (result.isReadOnly()){
                            mode = FormMode.READ;
                            updateMode();
                        }
                    }
                }, this));

        dispatcher.execute(new GetNameAction(refBookDataId), CallbackUtils.defaultCallback(
                new AbstractCallback<GetNameResult>() {
                    @Override
                    public void onSuccess(GetNameResult result) {
                        getView().setRefBookNameDesc(result.getName());
                    }
                }, this));
        canVersion = !Arrays.asList(RefBookDataModule.NOT_VERSIONED_REF_BOOK_IDS).contains(refBookDataId);
        getView().setVersionedFields(canVersion);
        editFormPresenter.setCanVersion(canVersion);
    }

    private void checkRecord() {
        CheckRecordExistenceAction action = new CheckRecordExistenceAction();
        action.setRefBookId(refBookDataId);
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
                            editFormPresenter.show(recordId);
                        }
                    }
                }, this));
    }

    @Override
    public boolean useManualReveal() {
        return true;
    }


    private void updateMode(){
        getView().updateMode(mode);
        editFormPresenter.setMode(mode);
        versionPresenter.setMode(mode);
    }

    @Override
    public void setMode(FormMode mode){
        this.mode = mode;
        updateMode();
    }
}
