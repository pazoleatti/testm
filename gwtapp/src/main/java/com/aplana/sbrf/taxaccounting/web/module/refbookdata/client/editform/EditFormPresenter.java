package com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.editform;

import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.gwt.client.dialog.DialogHandler;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
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

import java.util.*;

public class EditFormPresenter extends AbstractEditPresenter<EditFormPresenter.MyView>{

    @ProxyEvent
    @Override
    public void onShowItem(ShowItemEvent event) {
        super.show(event.getRecordId());
    }

    public interface MyView extends AbstractEditPresenter.MyView {
        void setVersionFrom(Date value);
        void setVersionTo(Date value);
        Date getVersionFrom();
        Date getVersionTo();
        void fillVersionData(RefBookRecordVersionData versionData);
    }

	@Inject
	public EditFormPresenter(final EventBus eventBus, final MyView view, final DispatchAsync dispatchAsync,
                             PlaceManager placeManager) {
		super(eventBus, view, dispatchAsync, placeManager);
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
        recordChanges.setCreate(false);

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
                                LogAddEvent.fire(EditFormPresenter.this, result.getUuid());
                                if (result.isException()) {
                                    Dialog.errorMessage("Запись не сохранена", "Обнаружены фатальные ошибки!");
                                } else {
                                    setIsFormModified(false);
                                    SetFormMode.fire(EditFormPresenter.this, FormMode.EDIT);
                                    getView().updateInputFields();
                                }
                                //Обновление таблицы
                                UpdateForm.fire(EditFormPresenter.this, !result.isException(), recordChanges);
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
        recordChanges.setCreate(true);

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
                                LogAddEvent.fire(EditFormPresenter.this, result.getUuid());
                                setIsFormModified(false);
                                Long newId = result.getNewIds() != null && !result.getNewIds().isEmpty() ? result.getNewIds().get(0) : null;
                                recordChanges.setId(newId);
                                if (isVersionMode) {
                                    setCurrentUniqueRecordId(newId);
                                } else {
                                    setRecordId(newId);
                                }
                                RefBookRecordVersionData data = new RefBookRecordVersionData();
                                data.setVersionStart(getView().getVersionFrom());
                                data.setVersionEnd(getView().getVersionTo());
                                data.setVersionCount(1);
                                getView().cleanErrorFields();
                                getView().fillVersionData(data);
                                UpdateForm.fire(EditFormPresenter.this, true, recordChanges);
                                SetFormMode.fire(EditFormPresenter.this, FormMode.EDIT);
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
                                    setCurrentUniqueRecordId(null);
                                    getView().fillInputFields(result.getRecord());
                                    getView().setVersionFrom(result.getVersionData().getVersionStart());
                                    getView().setVersionTo(null);
                                }
                            }, EditFormPresenter.this));
        } else {
            setCurrentUniqueRecordId(null);
            getView().cleanFields();
            RefBookValueSerializable rbStringField = new RefBookValueSerializable();
            rbStringField.setAttributeType(RefBookAttributeType.STRING);
            rbStringField.setStringValue("Новая запись");
            HashMap<String, RefBookValueSerializable> field = new HashMap<String, RefBookValueSerializable>(1);
            field.put(MyView.NEW_RECORD_ALIAS, rbStringField);
            getView().fillInputFields(field);

            /*if (!isVersionMode && mode == FormMode.EDIT) {
                setMode(FormMode.CREATE);
            } else if(!isVersionMode && mode == FormMode.CREATE){
                setMode(FormMode.EDIT);
            } else {
                setMode(mode);
            } */
            getView().setVersionFrom(null);
            getView().setVersionTo(null);
        }
        //getView().updateRefBookPickerPeriod();
    }

    @Override
    void showRecord(final Long refBookRecordId) {
        if (refBookRecordId == null) {
            setCurrentUniqueRecordId(null);
            getView().cleanFields();
            //getView().fillInputFields(null);

            /*if (!isVersionMode && mode == FormMode.EDIT) {
                setMode(FormMode.CREATE);
            } else if(!isVersionMode && mode == FormMode.CREATE){
                setMode(FormMode.EDIT);
            } else {
                setMode(mode);
            } */
            /*getView().setVersionFrom(null);
            getView().setVersionTo(null);*/
            getView().updateRefBookPickerPeriod();
            return;
        }

        /*if (isFormModified) {
            Dialog.confirmMessage(DIALOG_MESSAGE, new DialogHandler() {
                @Override
                public void yes() {
                    setIsFormModified(false);
                    EditFormPresenter.super.showRecord(refBookRecordId);
                    getView().cleanErrorFields();
                    SetFormMode.fire(EditFormPresenter.this, mode);
                }

                @Override
                public void no() {
                    super.no();
                    RollbackTableRowSelection.fire(EditFormPresenter.this, currentUniqueRecordId);
                    SetFormMode.fire(EditFormPresenter.this, FormMode.EDIT);
                }

                @Override
                public void cancel() {
                    no();
                }

                @Override
                public void close() {
                    no();
                }

            });
        } else {
            super.showRecord(refBookRecordId);
        }*/
        super.showRecord(refBookRecordId);
    }

    @Override
    public void onCancelClicked() {
        if (isFormModified) {
            Dialog.confirmMessage("Сохранение изменений", "Сохранить изменения?", new DialogHandler() {
                @Override
                public void yes() {
                    setIsFormModified(false);
                    onSaveClicked(false);
                    SetFormMode.fire(EditFormPresenter.this, FormMode.EDIT);
                }

                @Override
                public void no() {
                    setIsFormModified(false);
                    showRecord(getPreviousURId());
                    getView().cleanErrorFields();
                    SetFormMode.fire(EditFormPresenter.this, FormMode.EDIT);
                }
            });
        } else {
            //Показать родительскую запись
            //setMode(FormMode.EDIT);
            showRecord(getPreviousURId());
            getView().cleanErrorFields();
            SetFormMode.fire(EditFormPresenter.this, FormMode.EDIT);
        }
    }
}
