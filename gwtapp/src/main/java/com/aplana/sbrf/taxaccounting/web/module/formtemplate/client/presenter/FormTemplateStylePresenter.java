package com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.presenter;


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
import com.gwtplatform.mvp.client.annotations.*;
import com.gwtplatform.mvp.client.proxy.RevealContentEvent;
import com.gwtplatform.mvp.client.proxy.TabContentProxyPlace;

import java.util.List;

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
		super(eventBus, view, proxy);
		getView().setUiHandlers(this);
	}

	@ProxyEvent
	@Override
	public void onSet(FormTemplateSetEvent event) {
		boolean isFormChanged = formTemplateId != event.getFormTemplate().getId();
		formTemplateId = event.getFormTemplate().getId();
		getView().setViewData(event.getFormTemplate().getStyles(), isFormChanged);
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
}