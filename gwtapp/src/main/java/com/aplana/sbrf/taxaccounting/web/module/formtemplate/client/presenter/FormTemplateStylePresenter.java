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
	private FormTemplateMainPresenter formTemplateMainPresenter;

	@Inject
	public FormTemplateStylePresenter(final EventBus eventBus, final MyView view, final MyProxy proxy, FormTemplateMainPresenter formTemplateMainPresenter) {
		super(eventBus, view, proxy, FormTemplateMainPresenter.TYPE_SetTabContent);
		getView().setUiHandlers(this);
		this.formTemplateMainPresenter = formTemplateMainPresenter;
	}
	
	@Override
	protected void onBind() {
		super.onBind();
		addRegisteredHandler(FormTemplateFlushEvent.getType(), this);
	}

	@ProxyEvent
	@Override
	public void onSet(FormTemplateSetEvent event) {
		boolean isFormChanged = event.getFormTemplateExt().getFormTemplate().getId() == null ||
                formTemplateId != event.getFormTemplateExt().getFormTemplate().getId();
		formTemplateId = event.getFormTemplateExt().getFormTemplate().getId() != null?
                event.getFormTemplateExt().getFormTemplate().getId() : 0;
		getView().setViewData(event.getFormTemplateExt().getFormTemplate().getStyles(), isFormChanged);
	}

	@Override
	public void onFlush(FormTemplateFlushEvent event) {
		getView().onFlush();
	}

	public void onDataViewChanged(){
		formTemplateMainPresenter.setOnLeaveConfirmation("Вы подтверждаете отмену изменений?");
	}

}