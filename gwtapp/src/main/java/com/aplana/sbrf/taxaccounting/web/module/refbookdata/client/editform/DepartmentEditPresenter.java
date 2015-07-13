package com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.editform;

import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogAddEvent;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.editform.event.SetFormMode;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.editform.event.UpdateForm;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.editform.exception.BadValueException;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.editform.renamedialog.ConfirmButtonClickHandler;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.editform.renamedialog.RenameDialogPresenter;
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


public class DepartmentEditPresenter extends AbstractEditPresenter<DepartmentEditPresenter.MyView> {

    //Тип подразделения
    private long depType = 0;

    protected final RenameDialogPresenter renameDialogPresenter;

    @Inject
    public DepartmentEditPresenter(EventBus eventBus, MyView view, DispatchAsync dispatchAsync, PlaceManager placeManager, RenameDialogPresenter renameDialogPresenter) {
        super(eventBus, view, dispatchAsync, placeManager);
        this.renameDialogPresenter = renameDialogPresenter;
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
    }

    @Override
    void showRecord(final Long refBookRecordId) {
        if (refBookRecordId == null) {
            setNeedToReload();
            getView().cleanFields();
            currentUniqueRecordId = null;
            getView().fillInputFields(null);
            getView().updateRefBookPickerPeriod();
            return;
        }
        previousURId = refBookRecordId;
        GetRefBookRecordAction action = new GetRefBookRecordAction();
        action.setRefBookId(currentRefBookId);
        action.setRefBookRecordId(refBookRecordId);
        dispatchAsync.execute(action,
                CallbackUtils.defaultCallback(
                        new AbstractCallback<GetRefBookRecordResult>() {
                            @Override
                            public void onSuccess(GetRefBookRecordResult result) {
                                currentUniqueRecordId = refBookRecordId;
                                //recordId = refBookRecordId;
                                updateView(result);
                                if (result.getRecord().containsKey("TYPE")) {
                                    RefBookValueSerializable v = result.getRecord().get("TYPE");
                                    if (v.getAttributeType() == RefBookAttributeType.REFERENCE) {
                                        depType = v.getReferenceValue();
                                    }
                                }
                            }
                        }, this));
    }

    @Override
    void save() throws BadValueException {
        final Map<String, RefBookValueSerializable> map = getView().getFieldsValues();

        final SaveRefBookRowVersionAction action = new SaveRefBookRowVersionAction();
        action.setRefBookId(currentRefBookId);
        action.setRecordId(currentUniqueRecordId);
        action.setRecordCommonId(recordId);
        action.setValueToSave(map);

        final RecordChanges recordChanges = fillRecordChanges(currentUniqueRecordId, map, action.getVersionFrom(), action.getVersionTo());
        final Long newDepType;
        if (map.containsKey("TYPE") && map.get("TYPE").getReferenceValue() != null) {
            newDepType = map.get("TYPE").getReferenceValue();
        } else {
            newDepType = 0L;
        }

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
                                LogAddEvent.fire(DepartmentEditPresenter.this, result.getUuid());
                                UpdateForm.fire(DepartmentEditPresenter.this, !result.isException(), recordChanges);
                                if (result.isException()) {
                                    Dialog.errorMessage("Запись не сохранена", "Обнаружены фатальные ошибки!");
                                } else {
                                    setIsFormModified(false);
                                    SetFormMode.fire(DepartmentEditPresenter.this, FormMode.EDIT);
                                    getView().updateInputFields();

                                    //Проверяем изменилось ли имя либо тип подразделения с типа ТБ
                                    if(modifiedFields.containsKey("NAME") || (modifiedFields.containsKey("TYPE") && depType == 2)){
                                        renameDialogPresenter.open(new ConfirmButtonClickHandler() {
                                            @Override
                                            public void onClick(Date dateFrom, Date dateTo) {
                                /*// тут дальнейшая обработка по сценаарию постановки
                                Dialog.infoMessage("Переименовываем с " + WidgetUtils.getDateString(dateFrom) +
                                        " по " + WidgetUtils.getDateString(dateTo) + "на имя \"" + modifiedFields.get("NAME") + "\"")*/
                                                EditPrintFDAction fdAction = new EditPrintFDAction();
                                                fdAction.setChangeType(modifiedFields.containsKey("TYPE"));
                                                fdAction.setDepName(map.get("NAME").toString());
                                                fdAction.setVersionFrom(dateFrom);
                                                fdAction.setVersionTo(dateTo);
                                                fdAction.setDepId(currentUniqueRecordId.intValue());
                                                renameDialogPresenter.getView().cleanDates();

                                                dispatchAsync.execute(fdAction,
                                                        CallbackUtils.defaultCallback(
                                                                new AbstractCallback<EditPrintFDResult>() {
                                                                    @Override
                                                                    public void onSuccess(EditPrintFDResult result) {
                                                                        depType = newDepType;
                                                                        Dialog.infoMessage(
                                                                                "Редактирование подразделения",
                                                                                "Изменено наименования подразделения в печатных представлениях налоговых форм");
                                                                    }
                                                                }, DepartmentEditPresenter.this));
                                            }
                                        });
                                    }
                                }

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
                                LogAddEvent.fire(DepartmentEditPresenter.this, result.getUuid());
                                setIsFormModified(false);
                                Long newId = result.getNewIds() != null && !result.getNewIds().isEmpty() ? result.getNewIds().get(0) : null;
                                recordChanges.setId(newId);
                                currentUniqueRecordId = newId;
                                RefBookRecordVersionData data = new RefBookRecordVersionData();
                                data.setVersionCount(1);
                                getView().cleanErrorFields();
                                UpdateForm.fire(DepartmentEditPresenter.this, true, recordChanges);
                                SetFormMode.fire(DepartmentEditPresenter.this, FormMode.EDIT);
                                getView().updateInputFields();
                            }
                        }, this));
    }

    @Override
    void updateView(GetRefBookRecordResult result) {
        getView().fillInputFields(result.getRecord());
    }

    @Override
    public void clean() {
        currentUniqueRecordId = null;
        getView().fillInputFields(null);
        getView().updateRefBookPickerPeriod();
    }

}
