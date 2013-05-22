package com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.presenter;


import java.util.List;

import com.aplana.sbrf.taxaccounting.model.FormStyle;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.AdminConstants;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.event.FormTemplateFlushEvent;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.event.FormTemplateSetEvent;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.view.FormTemplateStyleUiHandlers;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.annotations.ProxyEvent;
import com.gwtplatform.mvp.client.annotations.TabInfo;
import com.gwtplatform.mvp.client.annotations.Title;
import com.gwtplatform.mvp.client.proxy.TabContentProxyPlace;

public class FormTemplateStylePresenter extends Presenter<FormTemplateStylePresenter.MyView, FormTemplateStylePresenter.MyProxy>
		implements FormTemplateStyleUiHandlers, FormTemplateSetEvent.MyHandler, FormTemplateFlushEvent.MyHandler{
	/**
	 * {@link FormTemplateStylePresenter}'s proxy.
	 */
	@Title("Шаблоны налоговых форм")
	@ProxyCodeSplit
	@NameToken(AdminConstants.NameTokens.formTemplateStylePage)
	@TabInfo(container = FormTemplateMainPresenter.class,
			label = AdminConstants.TabLabels.formTemplateStyleLabel,
			priority = AdminConstants.TabPriorities.formTemplateStylePriority)
	public interface MyProxy extends TabContentProxyPlace<FormTemplateStylePresenter> {
	}

	public interface MyView extends View, HasUiHandlers<FormTemplateStyleUiHandlers> {
		void setViewData(List<FormStyle> styles, boolean isFormChanged);
		void onFlush();
	}

	private int formTemplateId;

	@Inject
	public FormTemplateStylePresenter(final EventBus eventBus, final MyView view, final MyProxy proxy) {
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
		boolean isFormChanged = formTemplateId != event.getFormTemplate().getId();
		formTemplateId = event.getFormTemplate().getId();
		getView().setViewData(event.getFormTemplate().getStyles(), isFormChanged);
	}

	@Override
	public void onFlush(FormTemplateFlushEvent event) {
		getView().onFlush();
	}

}