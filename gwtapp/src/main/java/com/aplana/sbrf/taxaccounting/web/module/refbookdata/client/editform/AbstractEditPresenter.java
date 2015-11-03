package com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.editform;

import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.gwt.client.dialog.DialogHandler;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogAddEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogCleanEvent;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.editform.event.RollbackTableRowSelection;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.editform.event.SetFormMode;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.editform.event.UpdateForm;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.editform.exception.BadValueException;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.event.ShowItemEvent;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.*;
import com.aplana.sbrf.taxaccounting.web.widget.logarea.shared.SaveLogEntriesAction;
import com.aplana.sbrf.taxaccounting.web.widget.logarea.shared.SaveLogEntriesResult;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.user.client.ui.HasValue;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.proxy.PlaceManager;

import java.util.*;

/**
 * User: avanteev
 */
public abstract class AbstractEditPresenter<V extends AbstractEditPresenter.MyView> extends PresenterWidget<V>
        implements EditFormUiHandlers, ShowItemEvent.ShowItemHandler {

    static final String DIALOG_MESSAGE = "Строка была изменена. Все не сохраненные данные будут потеряны. Продолжить?";

    /** Идентификатор справочника */
    Long currentRefBookId;
    /** Уникальный идентификатор версии записи справочника */
    Long currentUniqueRecordId, previousURId;
    /** Идентификатор записи справочника без учета версий */
    Long recordId;
    /** Признак того, что форма используется для работы с версиями записей справочника */
    boolean isVersionMode = false;
    /** Признак того, что справочник версионируемый */
    boolean versioned;
    boolean isFormModified = false;
    Map<String, Object> modifiedFields = new HashMap<String, Object>();
    FormMode mode;

    final DispatchAsync dispatchAsync;
    private final PlaceManager placeManager;

    public AbstractEditPresenter(EventBus eventBus, V view, final DispatchAsync dispatchAsync, PlaceManager placeManager) {
        super(eventBus, view);
        getView().setUiHandlers(this);
        this.dispatchAsync = dispatchAsync;
        this.placeManager = placeManager;
    }

    public interface MyView extends View, HasUiHandlers<EditFormUiHandlers> {
        //Алиас для идентификации что мы новую запись добавляем
        static final String NEW_RECORD_ALIAS = "NEW_RECORD";

        Map<RefBookColumn, HasValue> createInputFields(List<RefBookColumn> attributes);
        void fillInputFields(Map<String, RefBookValueSerializable> record);
        Map<String, RefBookValueSerializable> getFieldsValues() throws BadValueException;

        void setNeedToReload(boolean b);

        /** Обновление вьюшки для определенного состояния */
        void updateMode(FormMode mode);

        /**
         * Обновляет поля презентера
         */
        void updateRefBookPickerPeriod();
        void cleanFields();
        void cleanErrorFields();
        boolean checkChanges();
        HasClickHandlers getClickAllVersion();

        /**
         * Метод обновляет переменную, которая используется при проверке измения полей.
         * Обновляется после сохранения изменений.
         */
        void updateInputFields();
        void setVersionMode(boolean versionMode);
        void showVersioned(boolean versioned);
    }


    RecordChanges fillRecordChanges(Long recordId, Map<String, RefBookValueSerializable> map, Date start, Date end) {
        RecordChanges recordChanges = new RecordChanges();
        recordChanges.setId(recordId);
        Long parent = null;
        if (map.containsKey("PARENT_ID")) {
            parent = map.get("PARENT_ID").getReferenceValue();
        }
        recordChanges.setParentId(parent);
        String name = null;
        if (map.containsKey("NAME")) {
            name = map.get("NAME").getStringValue();
        }
        recordChanges.setName(name);
        recordChanges.setStart(start);
        recordChanges.setEnd(end);
        for (Map.Entry<String, RefBookValueSerializable> entry : map.entrySet()){
            switch (entry.getValue().getAttributeType()) {
                case NUMBER:
                    if (entry.getValue().getNumberValue() != null){
                        recordChanges.getInfo().put(entry.getKey(), entry.getValue().getNumberValue().toString());
                    }
                    break;
                case DATE:
                    if (entry.getValue().getDateValue() != null){
                        recordChanges.getInfo().put(entry.getKey(), entry.getValue().getDateValue().toString());
                    }
                    break;
                case STRING:
                    if (entry.getValue().getStringValue() != null){
                        recordChanges.getInfo().put(entry.getKey(), entry.getValue().getStringValue());
                    }
                    break;
                case REFERENCE:
                    if (entry.getValue().getReferenceValue() != null){
                        recordChanges.getInfo().put(entry.getKey(), entry.getValue().getDereferenceValue());
                    }
                    break;
                default:
                    break;
            }
        }
        return recordChanges;
    }

    public void setIsFormModified(boolean isFormModified) {
        this.isFormModified = isFormModified;
        if (isFormModified) {
            placeManager.setOnLeaveConfirmation("Вы подтверждаете отмену изменений?");
        } else {
            modifiedFields.clear();
            placeManager.setOnLeaveConfirmation(null);
        }
    }

    void showRecord(final Long refBookRecordId){
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
                            }
                        }, this));
    }

    public final void show(final Long refBookRecordId){
        if (refBookRecordId != null && refBookRecordId.equals(currentUniqueRecordId)) {
            showRecord(refBookRecordId);
            return;
        }
        if (mode.equals(FormMode.EDIT) && currentUniqueRecordId != null && getView().checkChanges()) {
            setIsFormModified(true);
        }
        if (isFormModified) {
            Dialog.confirmMessage(DIALOG_MESSAGE, new DialogHandler() {
                @Override
                public void yes() {
                    setIsFormModified(false);
                    showRecord(refBookRecordId);
                }

                @Override
                public void no() {
                    super.no();
                    RollbackTableRowSelection.fire(AbstractEditPresenter.this, currentUniqueRecordId);
                    SetFormMode.fire(AbstractEditPresenter.this, FormMode.EDIT);
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
            showRecord(refBookRecordId);
        }
    }

    /**
     * Используется только в случае если добавляем новый элемент
     * @param dereferenceValue имя ссылки справочника
     * @param recordId идентификатор записи
     */
    public void show(final String dereferenceValue, final long recordId){
        if (isFormModified) {
            Dialog.confirmMessage(DIALOG_MESSAGE, new DialogHandler() {
                @Override
                public void yes() {
                    setIsFormModified(false);
                    //showRecord(null);
                    RefBookValueSerializable refBookParent = new RefBookValueSerializable();
                    refBookParent.setAttributeType(RefBookAttributeType.REFERENCE);
                    refBookParent.setDereferenceValue(dereferenceValue);
                    refBookParent.setReferenceValue(recordId);
                    RefBookValueSerializable rbStringField = new RefBookValueSerializable();
                    rbStringField.setAttributeType(RefBookAttributeType.STRING);
                    rbStringField.setStringValue("Новая запись");
                    HashMap<String, RefBookValueSerializable> field = new HashMap<String, RefBookValueSerializable>(2);
                    field.put("PARENT_ID", refBookParent);
                    getView().fillInputFields(field);

                    getView().cleanErrorFields();
                    SetFormMode.fire(AbstractEditPresenter.this, mode);
                }

                @Override
                public void no() {
                    super.no();
                    RollbackTableRowSelection.fire(AbstractEditPresenter.this, currentUniqueRecordId);
                    SetFormMode.fire(AbstractEditPresenter.this, FormMode.EDIT);
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
            //showRecord(null);
            getView().cleanFields();
            RefBookValueSerializable refBookParent = new RefBookValueSerializable();
            refBookParent.setAttributeType(RefBookAttributeType.REFERENCE);
            refBookParent.setDereferenceValue(dereferenceValue);
            refBookParent.setReferenceValue(recordId);
            RefBookValueSerializable rbStringField = new RefBookValueSerializable();
            rbStringField.setAttributeType(RefBookAttributeType.STRING);
            rbStringField.setStringValue("Новая запись");
            HashMap<String, RefBookValueSerializable> field = new HashMap<String, RefBookValueSerializable>(2);
            field.put("PARENT_ID", refBookParent);
            field.put(MyView.NEW_RECORD_ALIAS, rbStringField);
            getView().fillInputFields(field);
        }
    }
    //Редактирование версии
    abstract void save() throws BadValueException;
    //Создание новой версии
    abstract void create() throws BadValueException;
    abstract void updateView(GetRefBookRecordResult result);
    public abstract void clean(Boolean isVersion);

    @Override
    public void onCancelClicked() {
        if (isFormModified) {
            Dialog.confirmMessage("Сохранение изменений", "Сохранить изменения?", new DialogHandler() {
                @Override
                public void yes() {
                    setIsFormModified(false);
                    onSaveClicked(false);
                    SetFormMode.fire(AbstractEditPresenter.this, FormMode.EDIT);
                    RollbackTableRowSelection.fire(AbstractEditPresenter.this, previousURId);
                }

                @Override
                public void no() {
                    setIsFormModified(false);
                    showRecord(previousURId);
                    getView().cleanErrorFields();
                    SetFormMode.fire(AbstractEditPresenter.this, FormMode.EDIT);
                    //В иерархических справониках выбирается предыдущий элемент
                    RollbackTableRowSelection.fire(AbstractEditPresenter.this, previousURId);
                }
            });
        } else {
            //Показать родительскую запись
            //setMode(FormMode.EDIT);
            showRecord(previousURId);
            getView().cleanErrorFields();
            SetFormMode.fire(AbstractEditPresenter.this, FormMode.EDIT);
            RollbackTableRowSelection.fire(this, previousURId);
        }
    }

    @Override
    public void onSaveClicked(boolean isEditButtonClicked) {
        final String title = (currentUniqueRecordId != null ? "Запись не сохранена" : "Запись не создана");
        try {
            LogCleanEvent.fire(AbstractEditPresenter.this);

            if (currentUniqueRecordId == null) {
                create();
            } else {
                save();
            }
        } catch (BadValueException bve) {
            Dialog.errorMessage(title, "Обнаружены фатальные ошибки!");
            List<LogEntry> logEntries = new ArrayList<LogEntry>();
            for (String entry : bve){
                logEntries.add(new LogEntry(LogLevel.ERROR, entry));
            }

            SaveLogEntriesAction action = new SaveLogEntriesAction();
            action.setLogEntries(logEntries);

            dispatchAsync.execute(action,
                    CallbackUtils.defaultCallback(
                            new AbstractCallback<SaveLogEntriesResult>() {
                                @Override
                                public void onSuccess(SaveLogEntriesResult result) {
                                    LogAddEvent.fire(AbstractEditPresenter.this, result.getUuid());
                                }
                            }, this));
        }
    }

    @Override
    public void valueChanged(String alias, Object value) {
        modifiedFields.put(alias, value);
        setIsFormModified(true);
    }

    @Override
    public void setMode(FormMode mode){
        this.mode = mode;
        getView().updateMode(mode);
    }

    @Override
    public String getTitle() {
        return currentUniqueRecordId != null ? "Запись не сохранена" : "Запись не создана";
    }

    @Override
    public boolean isVersioned() {
        return versioned;
    }

    public void setCurrentUniqueRecordId(Long currentUniqueRecordId) {
        this.currentUniqueRecordId = currentUniqueRecordId;
    }

    public void setRecordId(Long recordId) {
        this.recordId = recordId;
    }

    public HandlerRegistration addClickHandlerForAllVersions(ClickHandler clickHandler){
        if (versioned)
            return getView().getClickAllVersion().addClickHandler(clickHandler);
        else
            return new HandlerRegistration() {
                @Override
                public void removeHandler() {
                    //Nothing
                }
            };
    }

    public final HandlerRegistration addUpdateFormHandler(UpdateForm.UpdateFormHandler handler){
        if (versioned)
            return addHandler(UpdateForm.getType(), handler);
        else
            return new HandlerRegistration() {
                @Override
                public void removeHandler() {
                    //Nothing
                }
            };
    }

    public void init(final Long refbookId, boolean versioned) {
        currentRefBookId = refbookId;
        this.versioned = versioned;
        setIsFormModified(false);
        getView().showVersioned(versioned);
    }

    public void createFields(List<RefBookColumn> columns){
        getView().createInputFields(columns);
    }

    public boolean isFormModified() {
        return isFormModified;
    }

    public void setNeedToReload() {
        getView().setNeedToReload(true);
    }

    public void setVersionMode(boolean versionMode) {
        isVersionMode = versionMode;
        getView().setVersionMode(versionMode);
    }

    @Override
    protected void onBind() {
        super.onBind();
        addVisibleHandler(ShowItemEvent.getType(), this);
    }
}
