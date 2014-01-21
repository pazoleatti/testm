package com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.EditForm;

import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogAddEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogCleanEvent;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.EditForm.event.RollbackTableRowSelection;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.EditForm.event.UpdateForm;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.EditForm.exception.BadValueException;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.*;
import com.aplana.sbrf.taxaccounting.web.widget.logarea.shared.SaveLogEntriesAction;
import com.aplana.sbrf.taxaccounting.web.widget.logarea.shared.SaveLogEntriesResult;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HasValue;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.proxy.PlaceManager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class EditFormPresenter extends PresenterWidget<EditFormPresenter.MyView> implements EditFormUiHandlers {
	private final PlaceManager placeManager;
	private final DispatchAsync dispatchAsync;
	private boolean isFormModified = false;
	private Date relevanceDate;
	private static final String DIALOG_MESSAGE = "Строка была изменена. Все не сохраненные данные будут потеряны.";

    /** Идентификатор справочника */
    private Long currentRefBookId;
    /** Уникальный идентификатор версии записи справочника */
    private Long currentUniqueRecordId;
    /** Идентификатор записи справочника без учета версий */
    private Long recordId;
    /** Признак того, что форма используется для работы с версиями записей справочника */
    private boolean isVersionMode = false;

    public interface MyView extends View, HasUiHandlers<EditFormUiHandlers> {
		Map<RefBookColumn, HasValue> createInputFields(List<RefBookColumn> attributes);
		void fillInputFields(Map<String, RefBookValueSerializable> record);
		Map<String, RefBookValueSerializable> getFieldsValues() throws BadValueException;
		void setSaveButtonEnabled(boolean enabled);
		void setCancelButtonEnabled(boolean enabled);
		void setEnabled(boolean enabled);
        void fillVersionData(RefBookRecordVersionData versionData, Long currentRefBookId, Long refBookRecordId);
        void setVersionMode(boolean versionMode);
        Date getVersionFrom();
        Date getVersionTo();
        void setVersionFrom(Date value);
        void setVersionTo(Date value);
    }

	@Inject
	public EditFormPresenter(final EventBus eventBus, final MyView view, final DispatchAsync dispatchAsync, PlaceManager placeManager) {
		super(eventBus, view);
		this.placeManager = placeManager;
		this.dispatchAsync = dispatchAsync;
		getView().setUiHandlers(this);
	}

	public void init(final Long refbookId) {

		GetRefBookAttributesAction action = new GetRefBookAttributesAction();
		action.setRefBookId(refbookId);
		dispatchAsync.execute(action,
				CallbackUtils.defaultCallback(
						new AbstractCallback<GetRefBookAttributesResult>() {
							@Override
							public void onSuccess(GetRefBookAttributesResult result) {
								getView().createInputFields(result.getColumns());
								currentRefBookId = refbookId;
								isFormModified = false;
								setEnabled(false);
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
			boolean confirmed = Window.confirm(DIALOG_MESSAGE);
			if (confirmed) {
				isFormModified = false;
				showRecord(refBookRecordId);
			} else {
				RollbackTableRowSelection.fire(this, currentUniqueRecordId);
			}
		} else {
			showRecord(refBookRecordId);
		}
	}

	private void showRecord(final Long refBookRecordId) {
		if (refBookRecordId == null) {
			currentUniqueRecordId = null;
			getView().fillInputFields(null);
			setEnabled(true);
            getView().setVersionFrom(null);
            getView().setVersionTo(null);
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
								setEnabled(true);
							}
						}, this));
	}

	@Override
	public void onSaveClicked() {
		try {
            if (getView().getVersionFrom() == null) {
                Window.alert("Не указана дата начала актуальности");
                return;
            }
            if (getView().getVersionTo() != null && (getView().getVersionFrom().getTime() >= getView().getVersionTo().getTime())) {
                Window.alert("Дата окончания должна быть больше даты начала актуальности");
                return;
            }
			if (currentUniqueRecordId == null) {
                //Создание новой версии
                AddRefBookRowVersionAction action = new AddRefBookRowVersionAction();
                action.setRefBookId(currentRefBookId);
                action.setRecordId(recordId);
                List<Map<String, RefBookValueSerializable>> valuesToAdd = new ArrayList<Map<String, RefBookValueSerializable>>();
                valuesToAdd.add(getView().getFieldsValues());

                action.setRecords(valuesToAdd);
                action.setVersionFrom(getView().getVersionFrom());
                action.setVersionTo(getView().getVersionTo());

                dispatchAsync.execute(action,
                        CallbackUtils.defaultCallback(
                                new AbstractCallback<AddRefBookRowVersionResult>() {
                                    @Override
                                    public void onSuccess(AddRefBookRowVersionResult result) {
                                        LogCleanEvent.fire(EditFormPresenter.this);
                                        LogAddEvent.fire(EditFormPresenter.this, result.getUuid());
                                        isFormModified = false;
                                        getView().fillInputFields(null);
                                        setEnabled(false);
                                        UpdateForm.fire(EditFormPresenter.this, true);
                                    }
                                }, this));
			} else {
                //Редактирование версии
                SaveRefBookRowVersionAction action = new SaveRefBookRowVersionAction();
                action.setRefBookId(currentRefBookId);
                action.setRecordId(currentUniqueRecordId);
                action.setValueToSave(getView().getFieldsValues());
                action.setVersionFrom(getView().getVersionFrom());
                action.setVersionTo(getView().getVersionTo());
                dispatchAsync.execute(action,
                        CallbackUtils.defaultCallback(
                                new AbstractCallback<SaveRefBookRowVersionResult>() {
                                    @Override
                                    public void onSuccess(SaveRefBookRowVersionResult result) {
                                        LogCleanEvent.fire(EditFormPresenter.this);
                                        LogAddEvent.fire(EditFormPresenter.this, result.getUuid());
                                        isFormModified = false;
                                        getView().fillInputFields(null);
                                        setEnabled(false);
                                        UpdateForm.fire(EditFormPresenter.this, true);
                                    }
                                }, this));
			}
		} catch (BadValueException bve) {
            isFormModified = false;
            List<LogEntry> logEntries = new ArrayList<LogEntry>();
            logEntries.add(new LogEntry(LogLevel.ERROR, "\" " + bve.getFieldName() + "\" - " + bve.getDescription()));
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

	@Override
	public void onCancelClicked() {
		isFormModified = false;
		showRecord(currentUniqueRecordId);
	}

	@Override
	public void valueChanged() {
		isFormModified = true;
	}

    public void setEnabled(boolean enabled) {
		getView().setEnabled(enabled);
	}

    public boolean isVersionMode() {
        return isVersionMode;
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
}
