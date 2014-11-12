package com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.editform;

import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.gwt.client.dialog.DialogHandler;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookType;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogAddEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogCleanEvent;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.FormMode;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.RefBookDataTokens;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.editform.event.RollbackTableRowSelection;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.editform.event.SetFormMode;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.editform.event.UpdateForm;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.editform.exception.BadValueException;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.editform.renamedialog.ConfirmButtonClickHandler;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.editform.renamedialog.RenameDialogPresenter;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.hierarchy.RefBookHierDataPresenter;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.*;
import com.aplana.sbrf.taxaccounting.web.widget.logarea.shared.SaveLogEntriesAction;
import com.aplana.sbrf.taxaccounting.web.widget.logarea.shared.SaveLogEntriesResult;
import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.shared.model.RefBookTreeItem;
import com.aplana.sbrf.taxaccounting.web.widget.utils.WidgetUtils;
import com.google.gwt.user.client.ui.HasValue;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;

import java.util.*;

public class EditFormPresenter extends PresenterWidget<EditFormPresenter.MyView> implements EditFormUiHandlers {
	private final PlaceManager placeManager;
	private final DispatchAsync dispatchAsync;
	private boolean isFormModified = false;
	private static final String DIALOG_MESSAGE = "Строка была изменена. Все не сохраненные данные будут потеряны. Продолжить?";

    /** Идентификатор справочника */
    private Long currentRefBookId;
    /** Уникальный идентификатор версии записи справочника */
    Long currentUniqueRecordId;
    /** Идентификатор записи справочника без учета версий */
    private Long recordId;
    /** Признак того, что форма используется для работы с версиями записей справочника */
    private boolean isVersionMode = false;
    /** Режим показа формы */
    private FormMode mode;
    /**Может ли справочник работать с версиями*/
    private boolean canVersion= false;
    // Признак того, что справочник подразделений
    private boolean isDepartments = false;
    //Тип подразделения
    private long depType = 0;
    Map<String, Object> modifiedFields = new HashMap<String, Object>();

    public void setNeedToReload() {
        getView().setNeedToReload(true);
    }

    public interface MyView extends View, HasUiHandlers<EditFormUiHandlers> {
		Map<RefBookColumn, HasValue> createInputFields(List<RefBookColumn> attributes);
		void fillInputFields(Map<String, RefBookValueSerializable> record);
		Map<String, RefBookValueSerializable> getFieldsValues() throws BadValueException;

        void setHierarchy(boolean isHierarchy);

        boolean isHierarchy();

		void fillVersionData(RefBookRecordVersionData versionData, Long currentRefBookId, Long refBookRecordId);
        void setVersionMode(boolean versionMode);
        Date getVersionFrom();
        Date getVersionTo();
        void setVersionFrom(Date value);
        void setVersionTo(Date value);
        void setNeedToReload(boolean b);
        /** Обновление вьюшки для определенного состояния */
        void updateMode(FormMode mode);
        void updateRefBookPickerPeriod();
        void setVisibleFields(boolean isVisible);
        // Устанавливает видимость для поля "Все версии"
        void setAllVersionField(boolean isVisible);
    }

    protected final RenameDialogPresenter renameDialogPresenter;

    //TODO: взаимодействие в зависимом виджете д.б. через события, а не через "левое" view
    private RefBookHierDataPresenter.MyView refBookHierDataPresenterMyView;

	@Inject
	public EditFormPresenter(final EventBus eventBus, final MyView view, final DispatchAsync dispatchAsync,
                             PlaceManager placeManager, RenameDialogPresenter renameDialogPresenter,
                             RefBookHierDataPresenter.MyView refBookHierDataPresenterMyView) {
		super(eventBus, view);
		this.placeManager = placeManager;
		this.dispatchAsync = dispatchAsync;
        this.renameDialogPresenter = renameDialogPresenter;
        this.refBookHierDataPresenterMyView = refBookHierDataPresenterMyView;
        getView().setUiHandlers(this);
	}

	public void init(final Long refbookId, final boolean readOnly) {
        isDepartments = refbookId == 30;
        GetRefBookAttributesAction action = new GetRefBookAttributesAction();
        action.setRefBookId(refbookId);
        currentRefBookId = refbookId;
        dispatchAsync.execute(action,
                CallbackUtils.defaultCallback(
                        new AbstractCallback<GetRefBookAttributesResult>() {
                            @Override
                            public void onSuccess(GetRefBookAttributesResult result) {
                                getView().setHierarchy(RefBookType.HIERARCHICAL.getId() == result.getRefBookType());

                                getView().createInputFields(result.getColumns());
                                setIsFormModified(false);
                                if (readOnly) {
                                    setMode(FormMode.READ);
                                } else {
                                    updateMode();
                                }
                            }
                        }, this));
    }

	public void show(Long refBookRecordId) {
        show(refBookRecordId, null);
	}

    public void show(final Long refBookRecordId, final RefBookTreeItem parentRefBookRecordId) {
        if (refBookRecordId != null && refBookRecordId.equals(currentUniqueRecordId)) {
            return;
        }
        if (isFormModified) {
            Dialog.confirmMessage(DIALOG_MESSAGE, new DialogHandler() {
                @Override
                public void yes() {
                    setIsFormModified(false);
                    showRecord(refBookRecordId, parentRefBookRecordId);
                }

                @Override
                public void no() {
                    rollbackIfNo();
                }

                @Override
                public void close() {
                    no();
                }
            });
        } else {
            showRecord(refBookRecordId, parentRefBookRecordId);
        }
    }

    private void rollbackIfNo(){
        RollbackTableRowSelection.fire(this, currentUniqueRecordId);
    }

	private void showRecord(Long refBookRecordId) {
        showRecord(refBookRecordId, null);
	}

    private void showRecord(final Long refBookRecordId, RefBookTreeItem parentRefBook) {
        if (refBookRecordId == null) {
            currentUniqueRecordId = null;
            getView().fillInputFields(null);
            if (parentRefBook != null){
                RefBookValueSerializable refBookParent = new RefBookValueSerializable();
                refBookParent.setAttributeType(RefBookAttributeType.REFERENCE);
                refBookParent.setDereferenceValue(parentRefBook.getDereferenceValue());
                refBookParent.setReferenceValue(parentRefBook.getId());
                HashMap<String, RefBookValueSerializable> field = new HashMap<String, RefBookValueSerializable>(1);
                field.put("PARENT_ID", refBookParent);
                getView().fillInputFields(field);
            }
            if (!isVersionMode && mode == FormMode.EDIT) {
                getView().updateMode(FormMode.CREATE);
            } else {
                setMode(mode);
            }
            getView().setVersionFrom(null);
            getView().setVersionTo(null);
            getView().updateRefBookPickerPeriod();
            return;
        }
        GetRefBookRecordAction action = new GetRefBookRecordAction();
        action.setRefBookId(currentRefBookId);
        action.setRefBookRecordId(refBookRecordId);
        dispatchAsync.execute(action,
                CallbackUtils.defaultCallback(
                        new AbstractCallback<GetRefBookRecordResult>() {
                            @Override
                            public void onSuccess(GetRefBookRecordResult result) {
                                getView().fillVersionData(result.getVersionData(), currentRefBookId, refBookRecordId);
                                getView().fillInputFields(result.getRecord());
                                if (result.getRecord().containsKey("TYPE")) {
                                    RefBookValueSerializable v = result.getRecord().get("TYPE");
                                    if (v.getAttributeType() == RefBookAttributeType.REFERENCE) {
                                        depType = v.getReferenceValue();
                                    }
                                }
                                currentUniqueRecordId = refBookRecordId;
                                updateMode();
                            }
                        }, this));
    }

	@Override
	public void onSaveClicked(boolean isEditButtonClicked) {
		try {
            LogCleanEvent.fire(EditFormPresenter.this);
            if (canVersion && getView().getVersionFrom() == null) {
                Dialog.warningMessage("Версия не сохранена", "Не указана дата начала актуальности");
                return;
            }
            Map<String, RefBookValueSerializable> map = getView().getFieldsValues();
            //TODO : Специфические для справочника подразделений проверки. Подумать над возможностью избавиться
            if (isDepartments && modifiedFields.containsKey("TYPE") &&
                    map.get("TYPE").getReferenceValue().intValue() != 1 &&  map.get("PARENT_ID").getReferenceValue() == null){
                Dialog.errorMessage("Родительское подразделение должно быть заполнено!");
                return;
            }
			if (currentUniqueRecordId == null) {
                //Создание новой версии
                AddRefBookRowVersionAction action = new AddRefBookRowVersionAction();
                action.setRefBookId(currentRefBookId);
                if (isVersionMode) {
                    action.setRecordId(recordId);
                } else {
                    action.setRecordId(null);
                }

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
                                            String title = (isVersionMode ? "Создание записи справочника" : "Создание элемента справочника");
                                            String msg = (isVersionMode ?
                                                    "Отсутствуют права доступ на создание записи для указанного региона!" :
                                                    "Отсутствуют права доступ на редактирование записи для указанного региона!");
                                            Dialog.errorMessage(title, msg);
                                            return;
                                        }
                                        LogAddEvent.fire(EditFormPresenter.this, result.getUuid());
                                        setIsFormModified(false);
                                        Long newId = result.getNewIds() != null && !result.getNewIds().isEmpty() ? result.getNewIds().get(0) : null;
                                        recordChanges.setId(newId);
                                        currentUniqueRecordId = newId;
                                        UpdateForm.fire(EditFormPresenter.this, true, recordChanges);
                                        SetFormMode.fire(EditFormPresenter.this, FormMode.EDIT);

                                    }
                                }, this));
			} else {
                //Редактирование версии
                final SaveRefBookRowVersionAction action = new SaveRefBookRowVersionAction();
                action.setRefBookId(currentRefBookId);
                action.setRecordId(currentUniqueRecordId);
                action.setRecordCommonId(recordId);
                action.setValueToSave(map);
                action.setVersionFrom(getView().getVersionFrom());
                action.setVersionTo(getView().getVersionTo());

                final RecordChanges recordChanges = fillRecordChanges(currentUniqueRecordId, map, action.getVersionFrom(), action.getVersionTo());
                final Long newDepType;
                if (map.containsKey("TYPE")) {
                    newDepType = map.get("TYPE").getReferenceValue();
                } else {
                    newDepType = 0L;
                }
                if (isDepartments) {
                    //Проверяем изменилось ли имя либо тип подразделения с типа ТБ
                    if(modifiedFields.containsKey("NAME") || (modifiedFields.containsKey("TYPE") && depType == 2)){
                        renameDialogPresenter.open(new ConfirmButtonClickHandler() {
                            @Override
                            public void onClick(Date dateFrom, Date dateTo) {
                                /*// тут дальнейшая обработка по сценаарию постановки
                                Dialog.infoMessage("Переименовываем с " + WidgetUtils.getDateString(dateFrom) +
                                        " по " + WidgetUtils.getDateString(dateTo) + "на имя \"" + modifiedFields.get("NAME") + "\"")*/
                                action.setVersionFrom(WidgetUtils.getDateWithOutTime(dateFrom));
                                action.setVersionTo(WidgetUtils.getDateWithOutTime(dateTo));
                                renameDialogPresenter.getView().cleanDates();

                                dispatchAsync.execute(action,
                                        CallbackUtils.defaultCallback(
                                                new AbstractCallback<SaveRefBookRowVersionResult>() {
                                                    @Override
                                                    public void onSuccess(SaveRefBookRowVersionResult result) {
                                                        LogAddEvent.fire(EditFormPresenter.this, result.getUuid());
                                                        UpdateForm.fire(EditFormPresenter.this, !result.isException(), recordChanges);
                                                        if (result.isException()) {
                                                            Dialog.errorMessage("Версия не сохранена", "Обнаружены фатальные ошибки!");
                                                        } else {
                                                            depType = newDepType;
                                                            setIsFormModified(false);
                                                            SetFormMode.fire(EditFormPresenter.this, FormMode.EDIT);
                                                        }
                                                    }
                                                }, EditFormPresenter.this));
                            }
                        });
                        return;
                    }
                }

                dispatchAsync.execute(action,
                        CallbackUtils.defaultCallback(
                                new AbstractCallback<SaveRefBookRowVersionResult>() {
                                    @Override
                                    public void onSuccess(SaveRefBookRowVersionResult result) {
                                        if (!result.isCheckRegion()) {
                                            String title = "Редактирование записи справочника";
                                            String msg = "Отсутствуют права доступ на редактирование записи для указанного региона!";
                                            Dialog.errorMessage(title, msg);
                                            return;
                                        }
                                        LogAddEvent.fire(EditFormPresenter.this, result.getUuid());
                                        UpdateForm.fire(EditFormPresenter.this, !result.isException(), recordChanges);
                                        if (result.isException()) {
                                            Dialog.errorMessage("Версия не сохранена", "Обнаружены фатальные ошибки!");
                                        } else {
                                            depType = newDepType;
                                            setIsFormModified(false);
                                            SetFormMode.fire(EditFormPresenter.this, FormMode.EDIT);
                                        }
                                    }
                                }, this));
			}
		} catch (BadValueException bve) {
            Dialog.errorMessage("Версия не сохранена", "Обнаружены фатальные ошибки!");
            List<LogEntry> logEntries = new ArrayList<LogEntry>();
            logEntries.add(new LogEntry(LogLevel.ERROR, bve.toString()));
            SaveLogEntriesAction action = new SaveLogEntriesAction();
            action.setLogEntries(logEntries);

            dispatchAsync.execute(action,
                    CallbackUtils.defaultCallback(
                            new AbstractCallback<SaveLogEntriesResult>() {
                                @Override
                                public void onSuccess(SaveLogEntriesResult result) {
                                    LogAddEvent.fire(EditFormPresenter.this, result.getUuid());
                                }
                            }, this));
        }
    }

    private RecordChanges fillRecordChanges(Long recordId, Map<String, RefBookValueSerializable> map, Date start, Date end) {
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
        return recordChanges;
    }

	@Override
	public void onCancelClicked() {
        if (isFormModified) {
            Dialog.confirmMessage("Редактирование версии", "Сохранить изменения?", new DialogHandler() {
                @Override
                public void yes() {
                    setIsFormModified(false);
                    onSaveClicked(false);
                }

                @Override
                public void no() {
                    setIsFormModified(false);
                    showRecord(currentUniqueRecordId);
                }
            });
        } else {
            showRecord(currentUniqueRecordId);
        }

        if (isDepartments) refBookHierDataPresenterMyView.updateMode(FormMode.EDIT);
    }

	@Override
	public void valueChanged(String alias, Object value) {
        modifiedFields.put(alias, value);
        setIsFormModified(true);
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

    public boolean isFormModified() {
        return isFormModified;
    }

    public void setVersionMode(boolean versionMode) {
        isVersionMode = versionMode;
        getView().setVersionMode(versionMode);
    }

    public void setCanVersion(boolean canVersion) {
        this.canVersion = canVersion;
        getView().setVisibleFields(canVersion);
    }

    public void setAllVersionVisible(boolean isVisible) {
        getView().setAllVersionField(isVisible);
    }

    public void setCurrentUniqueRecordId(Long currentUniqueRecordId) {
        this.currentUniqueRecordId = currentUniqueRecordId;
    }

    public void setRecordId(Long recordId) {
        this.recordId = recordId;
    }

    @Override
    public void setMode(FormMode mode){
        this.mode = mode;
        getView().updateMode(mode);
    }

    private void updateMode() {
        getView().updateMode(mode);
    }

    @Override
    public void updateHistory() {
        PlaceRequest currentPlaceRequest = placeManager.getCurrentPlaceRequest();
        placeManager.updateHistory(new PlaceRequest.Builder().nameToken(currentPlaceRequest.getNameToken())
                .with(RefBookDataTokens.REFBOOK_DATA_ID, currentPlaceRequest.getParameter(RefBookDataTokens.REFBOOK_DATA_ID, null))
                .with(RefBookDataTokens.REFBOOK_RECORD_ID, recordId.toString())
                .build(), true);
    }
}
