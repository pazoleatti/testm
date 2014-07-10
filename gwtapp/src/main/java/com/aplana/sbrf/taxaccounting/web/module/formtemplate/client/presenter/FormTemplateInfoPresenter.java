package com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.presenter;


import com.aplana.sbrf.taxaccounting.model.FormTemplate;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.AdminConstants;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.event.FormTemplateFlushEvent;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.event.FormTemplateSetEvent;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.view.FormTemplateInfoUiHandlers;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.shared.FormTemplateExt;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.*;
import com.gwtplatform.mvp.client.proxy.TabContentProxyPlace;

import java.util.Date;

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
		void setViewData(Date versionBegin, Date versionEnd, boolean fixedRows, boolean monthlyForm, String name, String fullName, String header);
		void onFlush();
	}

	private FormTemplateExt formTemplateExt;
    private FormTemplate formTemplate;

	@Inject
	public FormTemplateInfoPresenter(final EventBus eventBus, final MyView view, final MyProxy proxy) {
		super(eventBus, view, proxy, FormTemplateMainPresenter.TYPE_SetTabContent);
		getView().setUiHandlers(this);
	}
	
	@Override
	protected void onBind() {
		super.onBind();
		addRegisteredHandler(FormTemplateFlushEvent.getType(), this);
	}

	@ProxyEvent
	@Override
	public void onSet(FormTemplateSetEvent event) {
        formTemplateExt = event.getFormTemplateExt();
        formTemplate = formTemplateExt.getFormTemplate();
		getView().setViewData(formTemplate.getVersion(), formTemplateExt.getActualEndVersionDate(), formTemplate.isFixedRows(),
                formTemplate.isMonthly(), formTemplate.getName(), formTemplate.getFullName(), formTemplate.getHeader());
	}

	@Override
	public void onFlush(FormTemplateFlushEvent event) {
		getView().onFlush();
	}

	@Override
	public void setRangeRelevanceVersion(Date versionBegin, Date versionEnd) {
		formTemplate.setVersion(versionBegin);
        formTemplateExt.setActualEndVersionDate(versionEnd);
	}

	@Override
	public void setFixedRows(boolean fixedRows){
		formTemplate.setFixedRows(fixedRows);
	}

	@Override
	public void setMonthlyForm(boolean monthlyForm) {
		formTemplate.setMonthly(monthlyForm);
	}

	@Override
	public void setName(String name) {
		formTemplate.setName(name);
	}

	@Override
	public void setFullname(String fullName) {
		formTemplate.setFullName(fullName);
		
	}

	@Override
	public void setHeader(String header) {
		formTemplate.setHeader(header);
	}
}