package com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.EditForm;

import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.gwt.client.dialog.DialogHandler;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookType;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogAddEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogCleanEvent;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.EditForm.event.RollbackTableRowSelection;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.EditForm.event.UpdateForm;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.EditForm.exception.BadValueException;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.FormMode;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.RefBookDataTokens;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.*;
import com.aplana.sbrf.taxaccounting.web.widget.logarea.shared.SaveLogEntriesAction;
import com.aplana.sbrf.taxaccounting.web.widget.logarea.shared.SaveLogEntriesResult;
import com.google.gwt.user.client.ui.HasValue;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class EditFormPresenter extends PresenterWidget<EditFormPresenter.MyView> implements EditFormUiHandlers {
	private final PlaceManager placeManager;
	private final DispatchAsync dispatchAsync;
	private boolean isFormModified = false;
	private Date relevanceDate;
	private static final String DIALOG_MESSAGE = "Строка была изменена. Все не сохраненные данные будут потеряны. Продолжить?";

    /** Идентификатор справочника */
    private Long currentRefBookId;
    /** Уникальный идентификатор версии записи справочника */
    private Long currentUniqueRecordId;
    /** Идентификатор записи справочника без учета версий */
    private Long recordId;
    /** Признак того, что форма используется для работы с версиями записей справочника */
    private boolean isVersionMode = false;
    /** Режим показа формы */
    private FormMode mode;

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
    }

	@Inject
	public EditFormPresenter(final EventBus eventBus, final MyView view, final DispatchAsync dispatchAsync, PlaceManager placeManager) {
		super(eventBus, view);
		this.placeManager = placeManager;
		this.dispatchAsync = dispatchAsync;
		getView().setUiHandlers(this);
        mode = FormMode.VIEW;
        getView().updateMode(mode);
	}

	public void init(final Long refbookId, final boolean readOnly) {

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
                                }
                            }
                        }, this));
	}

	// TODO: отрефакторить, чтобы дата была общая с com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.RefBookDataView.getRelevanceDate() (Marat Fayzullin 2013-09-15)
	public Date getRelevanceDate() {
		if (relevanceDate == null) {
			relevanceDate = new Date();
		}
		return relevanceDate;
	}

	public void setRelevanceDate(Date relevanceDate) {
		this.relevanceDate = relevanceDate;
	}

	public void show(final Long refBookRecordId) {
		if (refBookRecordId != null && refBookRecordId.equals(currentUniqueRecordId)) {
			return;
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
                    rollbackIfNo();
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

    private void rollbackIfNo(){
        RollbackTableRowSelection.fire(this, currentUniqueRecordId);
    }

	private void showRecord(final Long refBookRecordId) {
        if (refBookRecordId == null) {
			currentUniqueRecordId = null;
			getView().fillInputFields(null);
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
								currentUniqueRecordId = refBookRecordId;
                                updateMode();
							}
						}, this));
	}

	@Override
	public void onSaveClicked() {
		try {
            if (getView().getVersionFrom() == null) {
                Dialog.warningMessage("Версия не сохранена", "Не указана дата начала актуальности");
                return;
            }
            if (getView().getVersionTo() != null && (getView().getVersionFrom().getTime() >= getView().getVersionTo().getTime())) {
                Dialog.warningMessage("Версия не сохранена", "Дата окончания должна быть больше даты начала актуальности");
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
                                        LogCleanEvent.fire(EditFormPresenter.this);
                                        LogAddEvent.fire(EditFormPresenter.this, result.getUuid());
                                        setIsFormModified(false);
                                        Long newId = result.getNewIds() != null && !result.getNewIds().isEmpty() ? result.getNewIds().get(0) : null;
                                        recordChanges.setId(newId);

                                        UpdateForm.fire(EditFormPresenter.this, true, recordChanges);
                                    }
                                }, this));
			} else {
                //Редактирование версии
                SaveRefBookRowVersionAction action = new SaveRefBookRowVersionAction();
                action.setRefBookId(currentRefBookId);
                action.setRecordId(currentUniqueRecordId);
                Map<String, RefBookValueSerializable> map = getView().getFieldsValues();
                action.setValueToSave(map);
                action.setVersionFrom(getView().getVersionFrom());
                action.setVersionTo(getView().getVersionTo());

                final RecordChanges recordChanges = fillRecordChanges(currentUniqueRecordId, map, action.getVersionFrom(), action.getVersionTo());
                dispatchAsync.execute(action,
                        CallbackUtils.defaultCallback(
                                new AbstractCallback<SaveRefBookRowVersionResult>() {
                                    @Override
                                    public void onSuccess(SaveRefBookRowVersionResult result) {
                                        LogCleanEvent.fire(EditFormPresenter.this);
                                        LogAddEvent.fire(EditFormPresenter.this, result.getUuid());
                                        setIsFormModified(false);
                                        UpdateForm.fire(EditFormPresenter.this, !result.isException(), recordChanges);
                                        if (result.isException()) {
                                            Dialog.errorMessage("Версия не сохранена", "Обнаружены фатальные ошибки!");
                                        }
                                    }
                                }, this));
			}
		} catch (BadValueException bve) {
            setIsFormModified(false);
            List<LogEntry> logEntries = new ArrayList<LogEntry>();
            logEntries.add(new LogEntry(LogLevel.ERROR, "\" " + bve.getFieldName() + "\": " + bve.getDescription()));
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
                    onSaveClicked();
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
	}

	@Override
	public void valueChanged() {
        setIsFormModified(true);
	}

    private void setIsFormModified(boolean isFormModified) {
        this.isFormModified = isFormModified;
        if (isFormModified) {
            placeManager.setOnLeaveConfirmation("Вы подтверждаете отмену изменений?");
        } else {
            placeManager.setOnLeaveConfirmation(null);
        }
    }

    public void setVersionMode(boolean versionMode) {
        isVersionMode = versionMode;
        getView().setVersionMode(versionMode);
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
