package com.aplana.sbrf.taxaccounting.web.module.formdata.client.signers;

import com.aplana.sbrf.taxaccounting.model.FormDataPerformer;
import com.aplana.sbrf.taxaccounting.model.FormDataSigner;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.*;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.PopupViewImpl;

import java.util.List;

/**
 * Форма "Исполнитель и подписанты"
 */
public class SignersView extends PopupViewImpl implements
		SignersPresenter.MyView {

	public interface Binder extends UiBinder<PopupPanel, SignersView> {
	}

	@UiField
	DialogBox dialogBox;

	private final PopupPanel widget;

	@Inject
	public SignersView(Binder uiBinder, EventBus eventBus) {
		super(eventBus);
		widget = uiBinder.createAndBindUi(this);
		asPopupPanel().setModal(true);
		widget.setAutoHideEnabled(true);
		widget.setAnimationEnabled(true);
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void setPerformer(FormDataPerformer performer) {
		//System.out.println(performer.getName());
	}

	@Override
	public void setSigners(List<FormDataSigner> signers) {
		//To change body of implemented methods use File | Settings | File Templates.
	}
}
