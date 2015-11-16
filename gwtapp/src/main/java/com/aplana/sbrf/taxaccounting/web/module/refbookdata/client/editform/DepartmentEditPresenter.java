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

import java.util.*;


public class DepartmentEditPresenter extends AbstractEditPresenter<DepartmentEditPresenter.MyView> {

    //Тип подразделения
    private long depType = 0;

    protected final RenameDialogPresenter renameDialogPresenter;

    private static final String OPEN_PERIOD_ERROR =
            "Подразделение не может быть отредактировано, так как для него нельзя изменить тип \"ТБ\", если для него существует период!";
    private static final String DEPARTMENT_ATTRIBUTE_TYPE = "TYPE";
    private static final String DEPARTMENT_ATTRIBUTE_NAME = "NAME";

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
            setCurrentUniqueRecordId(null);
            getView().fillInputFields(null);
            getView().updateRefBookPickerPeriod();
            return;
        }
        if (getPreviousURId()!=null&&getPreviousURId().equals(refBookRecordId)){
            return;
        }
        setPreviousURId(refBookRecordId);
        GetRefBookRecordAction action = new GetRefBookRecordAction();
        action.setRefBookId(currentRefBookId);
        action.setRefBookRecordId(refBookRecordId);
        dispatchAsync.execute(action,
                CallbackUtils.defaultCallback(
                        new AbstractCallback<GetRefBookRecordResult>() {
                            @Override
                            public void onSuccess(GetRefBookRecordResult result) {
                                setCurrentUniqueRecordId(refBookRecordId);
                                //recordId = refBookRecordId;
                                updateView(result);
                                if (result.getRecord().containsKey(DEPARTMENT_ATTRIBUTE_TYPE)) {
                                    RefBookValueSerializable v = result.getRecord().get(DEPARTMENT_ATTRIBUTE_TYPE);
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
        if (map.containsKey(DEPARTMENT_ATTRIBUTE_TYPE) && map.get(DEPARTMENT_ATTRIBUTE_TYPE).getReferenceValue() != null) {
            newDepType = map.get(DEPARTMENT_ATTRIBUTE_TYPE).getReferenceValue();
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
                                if (result.isException()) {
                                    Dialog.errorMessage("Запись не сохранена", "Обнаружены фатальные ошибки!");
                                } else {
                                    //4А. Система проверяет, верно ли, что в результате редактирования произошло изменение,
                                    // влияющее на актуальность наименования подразделения в печатных представлениях налоговых форм
                                    // (изменен тип подразделения с "ТБ" на другой или выполнено переименование подразделения).
                                    final UnitEditingAction editingAction = new UnitEditingAction();
                                    editingAction.setDepId(currentUniqueRecordId.intValue());
                                    editingAction.setValueToSave(map);
                                    if (modifiedFields.containsKey(DEPARTMENT_ATTRIBUTE_TYPE) && depType == 2) {
                                        CheckOpenReportPeriodAction periodAction = new CheckOpenReportPeriodAction();
                                        periodAction.setDepId(currentUniqueRecordId.intValue());
                                        dispatchAsync.execute(periodAction, CallbackUtils.defaultCallback(
                                                new AbstractCallback<CheckOpenReportPeriodResult>() {
                                                    @Override
                                                    public void onSuccess(CheckOpenReportPeriodResult result) {
                                                        if (result.isHaveOpenPeriod()){
                                                            Dialog.errorMessage(OPEN_PERIOD_ERROR);
                                                            LogAddEvent.fire(DepartmentEditPresenter.this, result.getUuid());
                                                        } else {
                                                            showRenameDialog(editingAction, map.get(DEPARTMENT_ATTRIBUTE_NAME).toString(), newDepType, recordChanges);

                                                        }
                                                    }
                                                }, DepartmentEditPresenter.this));
                                    } else if(modifiedFields.containsKey(DEPARTMENT_ATTRIBUTE_NAME)){
                                        showRenameDialog(editingAction, map.get(DEPARTMENT_ATTRIBUTE_NAME).getStringValue(), newDepType, recordChanges);
                                    } else {
                                        dispatchAsync.execute(editingAction,
                                                CallbackUtils.defaultCallback(
                                                        new AbstractCallback<UnitEditingResult>() {
                                                            @Override
                                                            public void onSuccess(UnitEditingResult result) {
                                                                depType = newDepType;
                                                                setIsFormModified(false);
                                                                //SetFormMode.fire(DepartmentEditPresenter.this, FormMode.EDIT);
                                                                getView().updateInputFields();
                                                                UpdateForm.fire(DepartmentEditPresenter.this, true, recordChanges);
                                                            }
                                                        }, DepartmentEditPresenter.this));
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
    public void clean(boolean isVersion) {
        //isVersion всегда должен быть null или false
        currentUniqueRecordId = null;
        getView().fillInputFields(new HashMap<String, RefBookValueSerializable>());
        getView().updateRefBookPickerPeriod();
    }

    private void showRenameDialog(final UnitEditingAction editingAction, final String depAttrName, final Long newDepType, final RecordChanges recordChanges){
        renameDialogPresenter.open(new ConfirmButtonClickHandler() {
            @Override
            public void onClick(Date dateFrom, Date dateTo) {
                                                                    /*// тут дальнейшая обработка по сценаарию постановки
                                                                    Dialog.infoMessage("Переименовываем с " + WidgetUtils.getDateString(dateFrom) +
                                                                            " по " + WidgetUtils.getDateString(dateTo) + "на имя \"" + modifiedFields.get("NAME") + "\"")*/
                editingAction.setChangeType(modifiedFields.containsKey(DEPARTMENT_ATTRIBUTE_TYPE));
                editingAction.setDepName(depAttrName);
                editingAction.setVersionFrom(dateFrom);
                editingAction.setVersionTo(dateTo);
                renameDialogPresenter.getView().cleanDates();
                dispatchAsync.execute(editingAction,
                        CallbackUtils.defaultCallback(
                                new AbstractCallback<UnitEditingResult>() {
                                    @Override
                                    public void onSuccess(UnitEditingResult result) {
                                        depType = newDepType;
                                        Dialog.infoMessage(
                                                "Редактирование подразделения",
                                                "Изменено наименования подразделения в печатных представлениях налоговых форм");

                                        setIsFormModified(false);
                                        //SetFormMode.fire(DepartmentEditPresenter.this, FormMode.EDIT);
                                        getView().updateInputFields();
                                        UpdateForm.fire(DepartmentEditPresenter.this, true, recordChanges);
                                    }
                                }, DepartmentEditPresenter.this));
            }
        });
    }

}
