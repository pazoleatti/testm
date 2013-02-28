package com.aplana.sbrf.taxaccounting.web.module.admin.client.presenter;


import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.web.module.admin.client.*;
import com.aplana.sbrf.taxaccounting.web.module.admin.client.event.*;
import com.aplana.sbrf.taxaccounting.web.module.admin.client.view.*;
import com.google.inject.*;
import com.google.web.bindery.event.shared.*;
import com.gwtplatform.mvp.client.*;
import com.gwtplatform.mvp.client.annotations.*;
import com.gwtplatform.mvp.client.proxy.*;

public class FormTemplateInfoPresenter extends Presenter<FormTemplateInfoPresenter.MyView, FormTemplateInfoPresenter.MyProxy>
		implements FormTemplateInfoUiHandlers, FormTemplateSetEvent.MyHandler, FormTemplateFlushEvent.MyHandler{
	/**
	 * {@link FormTemplateInfoPresenter}'s proxy.
	 */
	@Title("Шаблоны налоговых форм")
	@ProxyCodeSplit
	@NameToken(AdminConstants.NameTokens.formTemplateInfoPage)
	@TabInfo(container = FormTemplateMainPresenter.class,
			label = AdminConstants.TabLabels.formTemplateInfoLabel,
			priority = AdminConstants.TabPriorities.formTemplateInfoPriority)
	public interface MyProxy extends TabContentProxyPlace<FormTemplateInfoPresenter> {
	}

	public interface MyView extends View, HasUiHandlers<FormTemplateInfoUiHandlers> {
		void setViewData(String version, boolean numberedColumns, boolean fixedRows);
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
		getView().setViewData(formTemplate.getVersion(), formTemplate.isNumberedColumns(), formTemplate.isFixedRows());
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

	@Override
	public void setFixedRows(boolean fixedRows){
		formTemplate.setFixedRows(fixedRows);
	}
}