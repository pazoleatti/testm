package com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.EditForm;

import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogAddEvent;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.EditForm.event.UpdateForm;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.EditForm.exception.BadValueException;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.*;
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
	private Long currentRefBookId;
	private Long currentRecordId;
	private boolean isFormModified = false;
	private Date relevanceDate;
	private static final String DIALOG_MESSAGE = "Сохранить изменения в справочнике?";

	public interface MyView extends View, HasUiHandlers<EditFormUiHandlers> {
		Map<RefBookAttribute, HasValue> createInputFields(List<RefBookAttribute> attributes);
		void fillInputFields(Map<String, RefBookValueSerializable> record);
		Map<String, RefBookValueSerializable> getFieldsValues() throws BadValueException;
		void setSaveButtonEnabled(boolean enabled);
		void setCancelButtonEnabled(boolean enabled);
		void setEnabled(boolean enabled);
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
								getView().createInputFields(result.getAttributes());
								currentRefBookId = refbookId;
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

		if (isFormModified) {
			boolean confirmed = Window.confirm(DIALOG_MESSAGE);
			if (confirmed) {
				onSaveClicked();
			} else {
				onCancelClicked();
			}
		}
		showRecord(refBookRecordId);
	}

	private void showRecord(final Long refBookRecordId) {
		if (refBookRecordId == null) {
			currentRecordId = null;
			getView().fillInputFields(null);
			setEnabled(true);
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
								getView().fillInputFields(result.getRecord());
								currentRecordId = refBookRecordId;
								setEnabled(true);
							}
						}, this));
	}

	@Override
	public void onSaveClicked() {
		try {
			if (currentRecordId == null) {
				AddRefBookRowAction action = new AddRefBookRowAction();
				action.setRefBookId(currentRefBookId);
				List<Map<String, RefBookValueSerializable>> valuesToAdd = new ArrayList<Map<String, RefBookValueSerializable>>();
				valuesToAdd.add(getView().getFieldsValues());
				action.setRecords(valuesToAdd);
				action.setRelevanceDate(getRelevanceDate());
				dispatchAsync.execute(action,
						CallbackUtils.defaultCallback(
								new AbstractCallback<AddRefBookRowResult>() {
									@Override
									public void onSuccess(AddRefBookRowResult result) {
										isFormModified = false;
										getView().fillInputFields(null);
										setEnabled(false);
										UpdateForm.fire(EditFormPresenter.this, true);
									}
							}, this));
			} else {
					SaveRefBookRowAction action = new SaveRefBookRowAction();
					action.setRefBookId(currentRefBookId);
					action.setRecordId(currentRecordId);
					action.setValueToSave(getView().getFieldsValues());
					action.setRelevanceDate(getRelevanceDate());
					dispatchAsync.execute(action,
							CallbackUtils.defaultCallback(
									new AbstractCallback<SaveRefBookRowResult>() {
										@Override
										public void onSuccess(SaveRefBookRowResult result) {
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
			logEntries.add(new LogEntry(LogLevel.ERROR, "Некорректный тип значения в поле \" " + bve.getFieldName()  + "\""));
			LogAddEvent.fire(EditFormPresenter.this, logEntries);
		}
	}

	@Override
	public void onCancelClicked() {
		isFormModified = false;
		showRecord(currentRecordId);
	}

	@Override
	public void valueChanged() {
		isFormModified = true;
	}

	public void setEnabled(boolean enabled) {
		getView().setEnabled(enabled);
	}
}
