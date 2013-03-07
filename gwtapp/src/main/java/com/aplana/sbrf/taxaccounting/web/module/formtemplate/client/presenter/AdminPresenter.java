package com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.presenter;

import com.aplana.sbrf.taxaccounting.web.main.api.client.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.RevealContentTypeHolder;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.AdminConstants;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.view.AdminUiHandlers;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.shared.FormListAction;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.shared.FormListResult;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.annotations.Title;
import com.gwtplatform.mvp.client.proxy.*;

/**
 * Presenter для страницы администрирования. Выполняет следующие действия:
 * <ol>
 *     <li>Загружает список шаблонов форм</li>
 *     <li>Переходит к редактированию выбранного шаблона формы</li>
 * </ol>
 * @author Vitalii Samolovskikh
 */
public class AdminPresenter extends Presenter<AdminPresenter.MyView, AdminPresenter.MyProxy> implements AdminUiHandlers {

	/**
	 * {@link AdminPresenter}'s proxy.
	 */
	@Title("Шаблоны налоговых форм")
	@ProxyCodeSplit
	@NameToken(AdminConstants.NameTokens.adminPage)
	public interface MyProxy extends Proxy<AdminPresenter>, Place {
	}

	/**
	 * Интерфейс формы, т.е. вида, т.е. представления. Такой, каким видит его Presenter.
	 */
	public interface MyView extends View, HasUiHandlers<AdminUiHandlers> {
		void setFormTemplateTable(FormListResult result);
	}

	private final DispatchAsync dispatcher;
	private final PlaceManager placeManager;

	@Inject
	public AdminPresenter(EventBus eventBus, MyView view, MyProxy proxy, PlaceManager placeManager, DispatchAsync dispatcher) {
		super(eventBus, view, proxy);
		this.dispatcher = dispatcher;
		this.placeManager = placeManager;
		getView().setUiHandlers(this);
	}

	/**
	 * Здесь происходит подготовка формы администрирования.
	 *
	 * Загружается список шаблонов форм и складывается в список для выбора.
	 *
	 * @param request запрос
	 */
	@Override
	public void prepareFromRequest(PlaceRequest request) {
		super.prepareFromRequest(request);
		dispatcher.execute(new FormListAction(), new AbstractCallback<FormListResult>() {
			@Override
			public void onReqSuccess(FormListResult result) {
				getView().setFormTemplateTable(result);
			}
		});
	}

	/**
	 * Когда пользователь выбирает какой-нибудь шаблон формы, он переходит на страницу редактирования шаблона формы.
	 * Это происходит в этом методе.
	 *
	 * @param id идентификатор шаблона формы
	 */
	@Override
	public void selectForm(Integer id) {
			placeManager.revealPlace(
					new PlaceRequest(AdminConstants.NameTokens.formTemplateInfoPage).with(
							AdminConstants.NameTokens.formTemplateId, String.valueOf(id)
					)
			);
	}

	@Override
	protected void revealInParent() {
		RevealContentEvent.fire(this, RevealContentTypeHolder.getMainContent(), this);
	}

}
