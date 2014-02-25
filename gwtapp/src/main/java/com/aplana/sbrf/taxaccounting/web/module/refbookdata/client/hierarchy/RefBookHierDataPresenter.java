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
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.*;
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

    EditFormPresenter editFormPresenter;

    private final DispatchAsync dispatcher;
    private final TaPlaceManager placeManager;

    public interface MyView extends View, HasUiHandlers<RefBookHierDataUiHandlers> {

        void setSelected(Long recordId);

        void load();

        void reload();

        void setRefBookNameDesc(String desc);

        Long getSelectedId();

        Date getRelevanceDate();

        void setReadOnlyMode(boolean readOnly);

        void setAttributeId(Long attrId);
    }

    @Inject
    public RefBookHierDataPresenter(final EventBus eventBus, final MyView view, EditFormPresenter editFormPresenter,
                                    PlaceManager placeManager, final MyProxy proxy, DispatchAsync dispatcher) {
        super(eventBus, view, proxy, RevealContentTypeHolder.getMainContent());
        this.dispatcher = dispatcher;
        this.placeManager = (TaPlaceManager) placeManager;
        this.editFormPresenter = editFormPresenter;
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
        getView().reload();
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
        action.setRecordsId(Arrays.asList(getView().getSelectedId()));
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
                        }
                        editFormPresenter.show(null);
                        editFormPresenter.setEnabled(false);

                        getView().reload();
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
                        editFormPresenter.init(refBookDataId, result.isReadOnly());
                        getView().load();
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
}
