package com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.EditForm;

import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.EditForm.event.UpdateForm;
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
import java.util.List;
import java.util.Map;

public class EditFormPresenter extends PresenterWidget<EditFormPresenter.MyView> implements EditFormUiHandlers {
	private final PlaceManager placeManager;
	private final DispatchAsync dispatchAsync;
	private Long currentRefBookId;
	private Long currentRecordId;
	private boolean isFormModified = false;
	private static final String DIALOG_MESSAGE = "Сохранить изменения в справочнике?";

	public interface MyView extends View, HasUiHandlers<EditFormUiHandlers> {
		Map<RefBookAttribute, HasValue> createInputFields(List<RefBookAttribute> attributes);
		void fillInputFields(Map<String, RefBookValueSerializable> record);
		Map<String, RefBookValueSerializable> getFieldsValues();
		void setSaveButtonEnabled(boolean enabled);
		void setCancelButtonEnabled(boolean enabled);
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
		action.setRefbookId(refbookId);
		dispatchAsync.execute(action,
				CallbackUtils.defaultCallback(
						new AbstractCallback<GetRefBookAttributesResult>() {
							@Override
							public void onSuccess(GetRefBookAttributesResult result) {
								getView().createInputFields(result.getAttributes());
								currentRefBookId = refbookId;
							}
						}, this));
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
			return;
		}
		GetRefBookRecordAction action = new GetRefBookRecordAction();
		action.setRefBookDataId(currentRefBookId);
		action.setRefBookRecordId(refBookRecordId);
		dispatchAsync.execute(action,
				CallbackUtils.defaultCallback(
						new AbstractCallback<GetRefBookRecordResult>() {
							@Override
							public void onSuccess(GetRefBookRecordResult result) {
								getView().fillInputFields(result.getRecord());
								currentRecordId = refBookRecordId;
							}
						}, this));
	}

	@Override
	public void onSaveClicked() {
		getView().setCancelButtonEnabled(false);
		getView().setSaveButtonEnabled(false);
		if (currentRecordId == null) {
			AddRefBookRowAction action = new AddRefBookRowAction();
			action.setRefbookId(currentRefBookId);
			List<Map<String, RefBookValueSerializable>> valuesToAdd = new ArrayList<Map<String, RefBookValueSerializable>>();
			valuesToAdd.add(getView().getFieldsValues());
			action.setRecords(valuesToAdd);
			dispatchAsync.execute(action,
					CallbackUtils.defaultCallback(
							new AbstractCallback<AddRefBookRowResult>() {
								@Override
								public void onSuccess(AddRefBookRowResult result) {
									isFormModified = false;
									getView().fillInputFields(null);
									UpdateForm.fire(EditFormPresenter.this, true);
								}
							}, this));
		} else {
			SaveRefBookRowAction action = new SaveRefBookRowAction();
			action.setRefbookId(currentRefBookId);
			action.setRecordId(currentRecordId);
			action.setValueToSave(getView().getFieldsValues());
			dispatchAsync.execute(action,
					CallbackUtils.defaultCallback(
							new AbstractCallback<SaveRefBookRowResult>() {
								@Override
								public void onSuccess(SaveRefBookRowResult result) {
									isFormModified = false;
									UpdateForm.fire(EditFormPresenter.this, true);
								}
							}, this));
		}
	}

	@Override
	public void onCancelClicked() {
		isFormModified = false;
		getView().setCancelButtonEnabled(false);
		getView().setSaveButtonEnabled(false);
		showRecord(currentRecordId);
	}

	@Override
	public void valueChanged() {
		getView().setCancelButtonEnabled(true);
		getView().setSaveButtonEnabled(true);
		isFormModified = true;
	}

}
