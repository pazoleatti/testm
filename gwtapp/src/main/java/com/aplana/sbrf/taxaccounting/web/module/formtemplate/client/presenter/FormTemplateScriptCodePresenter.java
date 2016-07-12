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
import com.gwtplatform.mvp.client.annotations.*;
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
	private FormTemplateMainPresenter formTemplateMainPresenter;

	@Inject
	public FormTemplateScriptCodePresenter(final EventBus eventBus,
			final MyView view, final MyProxy proxy, FormTemplateMainPresenter formTemplateMainPresenter) {
		super(eventBus, view, proxy, FormTemplateMainPresenter.TYPE_SetTabContent);
		getView().setUiHandlers(this);
		this.formTemplateMainPresenter = formTemplateMainPresenter;
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
		formTemplate = event.getFormTemplateExt().getFormTemplate();
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
		
		// TODO: [sgoryachkin] 
		// 1) В перегрузке этого метода нет необходимости
		// 2) В этом методе не должно быть логики  (для этого есть события - onReveal)

		// Workaround
		// Почему то тупит CodeMirror когда инициализация представления происходит до reveal
		getView().setScriptCode(getView().getScriptCode());
	}

	public void onDataViewChanged(){
		formTemplateMainPresenter.setOnLeaveConfirmation("Вы подтверждаете отмену изменений?");
	}

}