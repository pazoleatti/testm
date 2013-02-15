package com.aplana.sbrf.taxaccounting.web.module.formdata.client.signers;

import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.FormDataPerformer;
import com.aplana.sbrf.taxaccounting.model.FormDataSigner;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;
import com.gwtplatform.mvp.client.PresenterWidget;

import java.util.List;

/**
 * Презентор "Исполнитель и подписанты"
 */

public class SignersPresenter extends PresenterWidget<SignersPresenter.MyView> implements SignersUiHandlers {
	private boolean readOnlyMode;
	private FormData formData;

	public interface MyView extends PopupView, HasUiHandlers<SignersUiHandlers> {
		void setPerformer(FormDataPerformer performer);
		void setSigners(List<FormDataSigner> signers);
		void setReadOnlyMode(boolean readOnlyMode);
	}

	@Inject
	public SignersPresenter(final EventBus eventBus, final MyView view) {
		super(eventBus, view);
		getView().setUiHandlers(this);
	}

	@Override
	protected void onReveal() {
		super.onReveal();
		getView().setReadOnlyMode(readOnlyMode);
		getView().setPerformer(formData.getPerformer());
		getView().setSigners(formData.getSigners());
	}

	@Override
	public void onSave(FormDataPerformer performer, List<FormDataSigner> signers) {
		formData.setPerformer(performer);
		formData.setSigners(signers);
		getView().hide();
	}

	public void setFormData(FormData formData) {
		this.formData = formData;
	}

	public void setReadOnlyMode(boolean readOnlyMode) {
		this.readOnlyMode = readOnlyMode;
	}
}
