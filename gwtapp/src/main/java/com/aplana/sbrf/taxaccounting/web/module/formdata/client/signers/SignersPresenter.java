package com.aplana.sbrf.taxaccounting.web.module.formdata.client.signers;

import com.aplana.sbrf.taxaccounting.model.FormDataPerformer;
import com.aplana.sbrf.taxaccounting.model.FormDataSigner;
import com.google.gwt.user.client.Window;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;
import com.gwtplatform.mvp.client.PresenterWidget;

import java.util.ArrayList;
import java.util.List;

/**
 * Презентор "Исполнитель и подписанты"
 */

public class SignersPresenter extends PresenterWidget<SignersPresenter.MyView> implements SignersUiHandlers {
	private FormDataPerformer performer;
	private List<FormDataSigner> signers;

	public interface MyView extends PopupView, HasUiHandlers<SignersUiHandlers> {
		void setPerformer(FormDataPerformer performer);
		void setSigners(List<FormDataSigner> signers);
	}

	@Inject
	public SignersPresenter(final EventBus eventBus, final MyView view) {
		super(eventBus, view);
		getView().setUiHandlers(this);
	}
	
	@Override
	protected void onReveal() {
		super.onReveal();
		performer = new FormDataPerformer();
		performer.setName("performer");
		performer.setPhone("phone");

		signers = new ArrayList<FormDataSigner>();
		for (int i = 0; i < 20; i++) {
			FormDataSigner signer =new FormDataSigner();
			signer.setName("signer " + (i+1));
			signer.setPosition("position " + (i+1));
			signers.add(signer);
		}

		getView().setPerformer(performer);
		getView().setSigners(signers);
	}

	@Override
	public void onSave() {
		getView().hide();
		Window.alert("Do save");
	}

	@Override
	public void onCancel() {
		getView().hide();
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
