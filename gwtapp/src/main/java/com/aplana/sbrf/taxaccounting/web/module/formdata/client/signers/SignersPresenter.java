package com.aplana.sbrf.taxaccounting.web.module.formdata.client.signers;

import com.aplana.sbrf.taxaccounting.model.FormDataPerformer;
import com.aplana.sbrf.taxaccounting.model.FormDataSigner;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.PopupView;
import com.gwtplatform.mvp.client.PresenterWidget;

import java.util.List;

/**
 * Презентор "Исполнитель и подписанты"
 */

public class SignersPresenter extends
		PresenterWidget<SignersPresenter.MyView> {
	private FormDataPerformer performer;
	private List<FormDataSigner> signers;

	public interface MyView extends PopupView {
		void setPerformer(FormDataPerformer performer);
		void setSigners(List<FormDataSigner> signers);
	}

	@Inject
	public SignersPresenter(final EventBus eventBus, final MyView view) {
		super(eventBus, view);
	}
	
	@Override
	protected void onReveal() {
		getView().setPerformer(performer);
		getView().setSigners(signers);
		super.onReveal();
	}

	public FormDataPerformer getPerformer() {
		return performer;
	}

	public void setPerformer(FormDataPerformer performer) {
		this.performer = performer;
	}

	public List<FormDataSigner> getSigners() {
		return signers;
	}

	public void setSigners(List<FormDataSigner> signers) {
		this.signers = signers;
	}
}
