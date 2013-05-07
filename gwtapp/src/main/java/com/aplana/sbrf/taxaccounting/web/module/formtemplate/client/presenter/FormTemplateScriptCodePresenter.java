package com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.presenter;

import com.aplana.sbrf.taxaccounting.model.FormTemplate;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.AdminConstants;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.event.FormTemplateFlushEvent;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.event.FormTemplateSetEvent;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.view.FormTemplateScriptCodeUiHandlers;
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
import com.gwtplatform.mvp.client.proxy.RevealContentEvent;
import com.gwtplatform.mvp.client.proxy.TabContentProxyPlace;

public class FormTemplateScriptCodePresenter
		extends
		Presenter<FormTemplateScriptCodePresenter.MyView, FormTemplateScriptCodePresenter.MyProxy>
		implements FormTemplateScriptCodeUiHandlers,
		FormTemplateSetEvent.MyHandler, FormTemplateFlushEvent.MyHandler {
	/**
	 * {@link FormTemplateMainPresenter}'s proxy.
	 */
	@Title("Шаблоны налоговых форм")
	@ProxyCodeSplit
	@NameToken(AdminConstants.NameTokens.formTemplateScriptCodePage)
	@TabInfo(container = FormTemplateMainPresenter.class, label = AdminConstants.TabLabels.formTemplateScriptCodeLabel, priority = AdminConstants.TabPriorities.formTemplateScriptCodePriority)
	public interface MyProxy extends
			TabContentProxyPlace<FormTemplateScriptCodePresenter> {
	}

	public interface MyView extends View,
			HasUiHandlers<FormTemplateScriptCodeUiHandlers> {
		void setScriptCode(String script);
		String getScriptCode();
	}

	private FormTemplate formTemplate;

	@Inject
	public FormTemplateScriptCodePresenter(final EventBus eventBus,
			final MyView view, final MyProxy proxy) {
		super(eventBus, view, proxy);
		getView().setUiHandlers(this);
	}
	
	@Override
	protected void onBind() {
		super.onBind();
		// sgoryachkin
		// FormTemplateFlushEvent не должен быть ProxyEvent
		// т.к. 
		// 1) Нет нужны вызывать обработку если презентер не разу не активный.
		// 2) ProxyEvent - асинхронный, а нам это ненужно.
		addRegisteredHandler(FormTemplateFlushEvent.getType(), this);
	}

	@ProxyEvent
	@Override
	public void onSet(FormTemplateSetEvent event) {
		formTemplate = event.getFormTemplate();
		getView().setScriptCode(formTemplate.getScript());
	}

	@Override
	public void onFlush(FormTemplateFlushEvent event) {
		if (formTemplate != null) {
			formTemplate.setScript(getView().getScriptCode());
		}
	}

	@Override
	protected void revealInParent() {
		RevealContentEvent.fire(this,
				FormTemplateMainPresenter.TYPE_SetTabContent, this);
		// Workaround
		// Почему то тупит CodeMirror когда инициализация представления происходит до reveal
		getView().setScriptCode(getView().getScriptCode());
	}

}