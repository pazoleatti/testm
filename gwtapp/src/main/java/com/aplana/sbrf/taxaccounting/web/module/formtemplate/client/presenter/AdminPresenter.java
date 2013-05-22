package com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.presenter;

import java.util.List;

import com.aplana.sbrf.taxaccounting.model.FormTemplate;
import com.aplana.sbrf.taxaccounting.web.main.api.client.RevealContentTypeHolder;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.AdminConstants;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.shared.GetFormTemplateListAction;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.shared.GetFormTemplateListResult;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.annotations.Title;
import com.gwtplatform.mvp.client.proxy.*;

/**
 * Presenter для страницы администрирования. Выполняет следующие действия:
 * <ol>
 * <li>Загружает список шаблонов форм</li>
 * <li>Переходит к редактированию выбранного шаблона формы</li>
 * </ol>
 * 
 * @author Vitalii Samolovskikh
 */
public class AdminPresenter extends
		Presenter<AdminPresenter.MyView, AdminPresenter.MyProxy> {

	/**
	 * {@link AdminPresenter}'s proxy.
	 */
	@Title("Шаблоны налоговых форм")
	@ProxyCodeSplit
	@NameToken(AdminConstants.NameTokens.adminPage)
	public interface MyProxy extends ProxyPlace<AdminPresenter> {
	}

	/**
	 * Интерфейс формы, т.е. вида, т.е. представления. Такой, каким видит его
	 * Presenter.
	 */
	public interface MyView extends View {
		void setFormTemplateTable(List<FormTemplate> formTemplates);
	}

	private final DispatchAsync dispatcher;

	@Inject
	public AdminPresenter(EventBus eventBus, MyView view, MyProxy proxy, DispatchAsync dispatcher) {
		super(eventBus, view, proxy, RevealContentTypeHolder.getMainContent());
		this.dispatcher = dispatcher;
	}

	@Override
	public boolean useManualReveal() {
		return true;
	}

	/**
	 * Здесь происходит подготовка формы администрирования.
	 * 
	 * Загружается список шаблонов форм и складывается в список для выбора.
	 * 
	 * @param request
	 *            запрос
	 */
	@Override
	public void prepareFromRequest(PlaceRequest request) {
		super.prepareFromRequest(request);
		dispatcher.execute(
				new GetFormTemplateListAction(),
				CallbackUtils.defaultCallback(
						new AbstractCallback<GetFormTemplateListResult>() {
							@Override
							public void onSuccess(
									GetFormTemplateListResult result) {
								getView().setFormTemplateTable(
										result.getForms());
							}
						}, this).addCallback(
						new ManualRevealCallback<GetFormTemplateListResult>(
								AdminPresenter.this)));
	}

}
