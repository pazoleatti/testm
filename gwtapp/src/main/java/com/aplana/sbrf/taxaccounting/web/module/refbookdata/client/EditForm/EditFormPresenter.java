package com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.EditForm;

import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.EditForm.event.UpdateForm;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.*;
import com.google.gwt.user.client.ui.HasValue;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.proxy.PlaceManager;

import java.util.List;
import java.util.Map;

public class EditFormPresenter extends PresenterWidget<EditFormPresenter.MyView> implements EditFormUiHandlers {
	private final PlaceManager placeManager;
	private final DispatchAsync dispatchAsync;
	private Long currentRefBookId;
	private Long currentRecordId;

	public interface MyView extends View, HasUiHandlers<EditFormUiHandlers> {
		Map<RefBookAttribute, HasValue> createInputFields(List<RefBookAttribute> attributes);
		void fillInputFields(Map<String, RefBookValueSerializable> record);
		Map<String, RefBookValueSerializable> getFieldsValues();
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

	public void showRecord(final Long refBookDataId, final Long refBookRecordId) {
		GetRefBookRecordAction action = new GetRefBookRecordAction();
		action.setRefBookDataId(refBookDataId);
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
		System.out.println("Save " + getView().getFieldsValues());
		SaveRefBookRowAction action = new SaveRefBookRowAction();
		action.setRefbookId(currentRefBookId);
		action.setRecordId(currentRecordId);
		action.setValueToSave(getView().getFieldsValues());
		dispatchAsync.execute(action,
				CallbackUtils.defaultCallback(
						new AbstractCallback<SaveRefBookRowResult>() {
							@Override
							public void onSuccess(SaveRefBookRowResult result) {
//								getView().fillInputFields(result.getRecord());
								UpdateForm.fire(EditFormPresenter.this, true);
							}
						}, this));
	}

}
