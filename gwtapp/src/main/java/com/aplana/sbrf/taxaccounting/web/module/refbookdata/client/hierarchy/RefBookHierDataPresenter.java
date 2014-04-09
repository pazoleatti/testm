package com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.hierarchy;

import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.sbrf.taxaccounting.web.main.api.client.RevealContentTypeHolder;
import com.aplana.sbrf.taxaccounting.web.main.api.client.TaPlaceManager;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogAddEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogCleanEvent;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.EditForm.EditFormPresenter;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.EditForm.event.RollbackTableRowSelection;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.EditForm.event.UpdateForm;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.RefBookDataTokens;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.VersionForm.RefBookVersionPresenter;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.*;
import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.shared.RefBookTreeItem;
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
import com.gwtplatform.mvp.client.proxy.PlaceManager;
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

    private Long refBookDataId;

    private Long recordId;

    Boolean editMode;

    EditFormPresenter editFormPresenter;
    RefBookVersionPresenter versionPresenter;

    private final DispatchAsync dispatcher;
    private final TaPlaceManager placeManager;

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

        void setReadOnlyMode(boolean readOnly);

        void setAttributeId(Long attrId);
        /** Показ формы в режиме редактирования */
        public void setEditMode();
        /** Показ формы в режиме просмотра */
        public void setDefaultMode();
        /** установить видимость кнопки редактирования */
        public void setVisibleEditLink(boolean visible);
    }

    @Inject
    public RefBookHierDataPresenter(final EventBus eventBus, final MyView view, EditFormPresenter editFormPresenter,
                                    RefBookVersionPresenter versionPresenter, PlaceManager placeManager, final MyProxy proxy, DispatchAsync dispatcher) {
        super(eventBus, view, proxy, RevealContentTypeHolder.getMainContent());
        this.dispatcher = dispatcher;
        this.placeManager = (TaPlaceManager) placeManager;
        this.editFormPresenter = editFormPresenter;
        this.versionPresenter = versionPresenter;
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
        if (event.isSuccess()) {
            RecordChanges rc = event.getRecordChanges();
            RefBookTreeItem selectedItem = getView().getSelectedItem();
            if (!WidgetUtils.isInLimitPeriod(rc.getStart(), rc.getEnd(), getView().getRelevanceDate())) {
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
                            editFormPresenter.setEnabled(false);
                            editFormPresenter.setNeedToReload();
                            getView().deleteItem(selected);
                        }
                    }
                }, this));
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
        editFormPresenter.setEnabled(false);
        getView().load();
    }

    @Override
    public void prepareFromRequest(final PlaceRequest request) {
        super.prepareFromRequest(request);

        editFormPresenter.setVersionMode(false);
        editFormPresenter.setCurrentUniqueRecordId(null);
        editFormPresenter.setRecordId(null);

        refBookDataId = Long.parseLong(request.getParameter(RefBookDataTokens.REFBOOK_DATA_ID, null));
        onSetDefaultMode();

        dispatcher.execute(new GetRefBookAttributesAction(refBookDataId), CallbackUtils.defaultCallback(
                new AbstractCallback<GetRefBookAttributesResult>() {
                    @Override
                    public void onSuccess(GetRefBookAttributesResult result) {
                        getView().setReadOnlyMode(result.isReadOnly());
                        Long attrId = null;
                        for (RefBookColumn refBookColumn : result.getColumns()) {
                            if(refBookColumn.getAlias().toLowerCase().equals("name")){
                                attrId = refBookColumn.getId();
                            }
                        }
                        getView().setAttributeId(attrId);
                        getView().setVisibleEditLink(!result.isReadOnly() && !editMode);
                        getView().setReadOnlyMode(!(editMode && !result.isReadOnly()));
                        editFormPresenter.init(refBookDataId, result.isReadOnly());
                        getView().loadAndSelect();
                        getProxy().manualReveal(RefBookHierDataPresenter.this);
                    }
                }, this));

        dispatcher.execute(new GetNameAction(refBookDataId), CallbackUtils.defaultCallback(
                new AbstractCallback<GetNameResult>() {
                    @Override
                    public void onSuccess(GetNameResult result) {
                        getView().setRefBookNameDesc(result.getName());
                    }
                }, this));

    }

    @Override
    public boolean useManualReveal() {
        return true;
    }

    @Override
    public void onSetEditMode(){
        editMode = true;
        getView().setEditMode();
        editFormPresenter.onSetEditMode();
        versionPresenter.onSetEditMode();
    }

    @Override
    public void onSetDefaultMode(){
        editMode = false;
        getView().setDefaultMode();
        editFormPresenter.onSetDefaultMode();
        versionPresenter.onSetDefaultMode();
    }
}
