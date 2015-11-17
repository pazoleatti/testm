package com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.editform;

import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogAddEvent;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.editform.event.SetFormMode;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.editform.event.UpdateForm;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.editform.exception.BadValueException;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.event.ShowItemEvent;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.*;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.annotations.ProxyEvent;
import com.gwtplatform.mvp.client.proxy.PlaceManager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * User: avanteev
 */
public class HierEditPresenter extends AbstractEditPresenter<HierEditPresenter.MyView> {

    @Inject
    public HierEditPresenter(EventBus eventBus, MyView view, DispatchAsync dispatchAsync, PlaceManager placeManager) {
        super(eventBus, view, dispatchAsync, placeManager);
    }

    @Override
    void showRecord(final Long refBookRecordId) {
        if (refBookRecordId == null) {
            setCurrentUniqueRecordId(null);
//            getView().fillInputFields(null);
            getView().updateRefBookPickerPeriod();
            getView().cleanFields();
            setNeedToReload();
            return;
        }
        super.showRecord(refBookRecordId);
    }

    @Override
    void save() throws BadValueException {
        Map<String, RefBookValueSerializable> map = getView().getFieldsValues();
        //Редактирование версии
        final SaveRefBookRowVersionAction action = new SaveRefBookRowVersionAction();
        action.setRefBookId(currentRefBookId);
        action.setRecordId(currentUniqueRecordId);
        action.setRecordCommonId(recordId);
        action.setValueToSave(map);
        action.setVersionFrom(getView().getVersionFrom());
        action.setVersionTo(getView().getVersionTo());

        final RecordChanges recordChanges = fillRecordChanges(currentUniqueRecordId, map, action.getVersionFrom(), action.getVersionTo());

        dispatchAsync.execute(action,
                CallbackUtils.defaultCallback(
                        new AbstractCallback<SaveRefBookRowVersionResult>() {
                            @Override
                            public void onSuccess(SaveRefBookRowVersionResult result) {
                                if (!result.isCheckRegion()) {
                                    String title = "Сохранение изменений";
                                    String msg = "Отсутствуют права доступа на редактирование записи для указанного региона!";
                                    Dialog.errorMessage(title, msg);
                                    return;
                                }
                                LogAddEvent.fire(HierEditPresenter.this, result.getUuid());
                                if (result.isException()) {
                                    Dialog.errorMessage("Запись не сохранена", "Обнаружены фатальные ошибки!");
                                } else {
                                    setIsFormModified(false);
                                    SetFormMode.fire(HierEditPresenter.this, FormMode.EDIT);
                                    getView().updateInputFields();
                                }
                                UpdateForm.fire(HierEditPresenter.this, !result.isException(), recordChanges);
                            }
                        }, this));
    }

    @Override
    void create() throws BadValueException {
        AddRefBookRowVersionAction action = new AddRefBookRowVersionAction();
        action.setRefBookId(currentRefBookId);
        if (isVersionMode) {
            action.setRecordId(recordId);
        } else {
            action.setRecordId(null);
        }

        Map<String, RefBookValueSerializable> map = getView().getFieldsValues();
        List<Map<String, RefBookValueSerializable>> valuesToAdd = new ArrayList<Map<String, RefBookValueSerializable>>();
        valuesToAdd.add(map);

        action.setRecords(valuesToAdd);
        action.setVersionFrom(getView().getVersionFrom());
        action.setVersionTo(getView().getVersionTo());

        final RecordChanges recordChanges = fillRecordChanges(recordId, map, action.getVersionFrom(), action.getVersionTo());

        dispatchAsync.execute(action,
                CallbackUtils.defaultCallback(
                        new AbstractCallback<AddRefBookRowVersionResult>() {
                            @Override
                            public void onSuccess(AddRefBookRowVersionResult result) {
                                if (!result.isCheckRegion()) {
                                    String title = (isVersionMode ? "Сохранение изменений" : "Создание элемента справочника");
                                    String msg = (isVersionMode ?
                                            "Отсутствуют права доступа на создание записи для указанного региона!" :
                                            "Отсутствуют права доступа на редактирование записи для указанного региона!");
                                    Dialog.errorMessage(title, msg);
                                    return;
                                }
                                LogAddEvent.fire(HierEditPresenter.this, result.getUuid());
                                setIsFormModified(false);
                                Long newId = result.getNewIds() != null && !result.getNewIds().isEmpty() ? result.getNewIds().get(0) : null;
                                recordChanges.setId(newId);
                                currentUniqueRecordId = newId;
                                RefBookRecordVersionData data = new RefBookRecordVersionData();
                                data.setVersionStart(getView().getVersionFrom());
                                data.setVersionEnd(getView().getVersionTo());
                                data.setVersionCount(1);
                                getView().cleanErrorFields();
                                getView().fillVersionData(data);
                                UpdateForm.fire(HierEditPresenter.this, true, recordChanges);
                                SetFormMode.fire(HierEditPresenter.this, FormMode.EDIT);
                                getView().updateInputFields();
                            }
                        }, this));
    }

    @Override
    void updateView(GetRefBookRecordResult result) {
        getView().fillVersionData(result.getVersionData());
        getView().fillInputFields(result.getRecord());
    }

    @Override
    public void clean(boolean isVersion) {
        if (isVersion) {
            GetRefBookRecordAction action = new GetRefBookRecordAction();
            action.setRefBookId(currentRefBookId);
            action.setUniqueRecordId(currentUniqueRecordId);
            action.setCreate(true);
            dispatchAsync.execute(action,
                    CallbackUtils.defaultCallback(
                            new AbstractCallback<GetRefBookRecordResult>() {
                                @Override
                                public void onSuccess(GetRefBookRecordResult result) {
                                    currentUniqueRecordId = null;
                                    getView().fillInputFields(result.getRecord());
                                    getView().setVersionFrom(result.getVersionData().getVersionStart());
                                    getView().setVersionTo(null);
                                }
                            }, HierEditPresenter.this));
        } else {
            currentUniqueRecordId = null;
//            getView().fillInputFields(null);
            getView().cleanFields();
            getView().updateRefBookPickerPeriod();
            setNeedToReload();
        }
        getView().setVersionFrom(null);
        getView().setVersionTo(null);
    }

    @ProxyEvent
    @Override
    public void onShowItem(ShowItemEvent event) {
        if (event.getDereferenceValue()!=null){
            super.show(event.getDereferenceValue(), event.getRecordId());
        } else {
            super.show(event.getRecordId());
        }
    }

    public interface MyView extends AbstractEditPresenter.MyView {
        void setVersionFrom(Date value);
        void setVersionTo(Date value);
        Date getVersionFrom();
        Date getVersionTo();
        void fillVersionData(RefBookRecordVersionData versionData);
    }


}
