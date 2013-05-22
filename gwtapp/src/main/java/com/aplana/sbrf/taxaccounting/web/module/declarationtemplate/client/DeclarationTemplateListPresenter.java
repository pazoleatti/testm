package com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.client;

import java.util.List;

import com.aplana.sbrf.taxaccounting.model.DeclarationTemplate;
import com.aplana.sbrf.taxaccounting.web.main.api.client.RevealContentTypeHolder;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.shared.DeclarationListAction;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.shared.DeclarationListResult;
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
 * Presenter для страницы администрирования деклараций. Выполняет следующие действия:
 * <ol>
 *     <li>Загружает список деклараций</li>
 *     <li>Переходит к редактированию выбранной декларации</li>
 * </ol>
 */
public class DeclarationTemplateListPresenter extends Presenter<DeclarationTemplateListPresenter.MyView, DeclarationTemplateListPresenter.MyProxy> {

	@Title("Шаблоны деклараций")
	@ProxyCodeSplit
	@NameToken(DeclarationTemplateTokens.declarationTemplateList)
	public interface MyProxy extends ProxyPlace<DeclarationTemplateListPresenter> {
	}

	/**
	 * Интерфейс декларации, т.е. представления. Такой, каким видит его Presenter.
	 */
	public interface MyView extends View {
		void setDeclarationTemplateRows(List<DeclarationTemplate> result);
	}

	private final DispatchAsync dispatcher;

	@Inject
	public DeclarationTemplateListPresenter(EventBus eventBus, MyView view, MyProxy proxy, DispatchAsync dispatcher) {
		super(eventBus, view, proxy, RevealContentTypeHolder.getMainContent());
		this.dispatcher = dispatcher;
	}

	/**
	 * Здесь происходит подготовка декларации администрирования.
	 *
	 * Загружается список шаблонов деклараций и складывается в список для выбора.
	 *
	 * @param request запрос
	 */
	@Override
	public void prepareFromRequest(PlaceRequest request) {
		super.prepareFromRequest(request);
		dispatcher.execute(new DeclarationListAction(),	CallbackUtils.defaultCallback(
						new AbstractCallback<DeclarationListResult>() {
							@Override
							public void onSuccess(
									DeclarationListResult result) {
								if (result.getDeclarations() != null && !result.getDeclarations().isEmpty()) {
									getView().setDeclarationTemplateRows(result.getDeclarations());
								}
							}
						}, this).addCallback(
						new ManualRevealCallback<DeclarationListResult>(
								DeclarationTemplateListPresenter.this)));
	}

	@Override
	public boolean useManualReveal() {
		return true;
	}
}
