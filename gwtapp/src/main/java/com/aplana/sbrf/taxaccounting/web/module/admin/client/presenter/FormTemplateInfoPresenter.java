package com.aplana.sbrf.taxaccounting.web.module.admin.client.presenter;


import com.aplana.sbrf.taxaccounting.model.FormTemplate;
import com.aplana.sbrf.taxaccounting.web.module.admin.client.AdminConstants;
import com.aplana.sbrf.taxaccounting.web.module.admin.client.event.FormTemplateFlushEvent;
import com.aplana.sbrf.taxaccounting.web.module.admin.client.event.FormTemplateSetEvent;
import com.aplana.sbrf.taxaccounting.web.module.admin.client.view.FormTemplateInfoUiHandlers;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.*;
import com.gwtplatform.mvp.client.proxy.RevealContentEvent;
import com.gwtplatform.mvp.client.proxy.TabContentProxyPlace;

public class FormTemplateInfoPresenter extends Presenter<FormTemplateInfoPresenter.MyView, FormTemplateInfoPresenter.MyProxy>
		implements FormTemplateInfoUiHandlers, FormTemplateSetEvent.MyHandler, FormTemplateFlushEvent.MyHandler{
	/**
	 * {@link FormTemplateInfoPresenter}'s proxy.
	 */
	@Title("Администрирование")
	@ProxyCodeSplit
	@NameToken(AdminConstants.NameTokens.formTemplateInfoPage)
	@TabInfo(container = FormTemplateMainPresenter.class,
			label = AdminConstants.TabLabels.formTemplateInfoLabel,
			priority = AdminConstants.TabPriorities.formTemplateInfoPriority)
	public interface MyProxy extends TabContentProxyPlace<FormTemplateInfoPresenter> {
	}

	public interface MyView extends View, HasUiHandlers<FormTemplateInfoUiHandlers> {
		void setViewData(String version, boolean numberedColumns);
		void onFlush();
	}

	private FormTemplate formTemplate;

	@Inject
	public FormTemplateInfoPresenter(final EventBus eventBus, final MyView view, final MyProxy proxy) {
		super(eventBus, view, proxy);
		getView().setUiHandlers(this);
	}

	@ProxyEvent
	@Override
	public void onSet(FormTemplateSetEvent event) {
		formTemplate = event.getFormTemplate();
		getView().setViewData(formTemplate.getVersion(), formTemplate.isNumberedColumns());
	}

	@ProxyEvent
	@Override
	public void onFlush(FormTemplateFlushEvent event) {
		getView().onFlush();
	}

	@Override
	protected void revealInParent() {
		RevealContentEvent.fire(this, FormTemplateMainPresenter.TYPE_SetTabContent, this);
	}

	@Override
	public void setNumberedColumns(boolean numberedColumns) {
		formTemplate.setNumberedColumns(numberedColumns);
	}

	@Override
	public void setVersion(String version) {
		formTemplate.setVersion(version);
	}
}