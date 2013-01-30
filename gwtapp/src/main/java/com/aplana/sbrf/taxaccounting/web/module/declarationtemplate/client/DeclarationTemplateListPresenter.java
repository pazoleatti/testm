package com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.client;

import com.aplana.sbrf.taxaccounting.model.DeclarationTemplate;
import com.aplana.sbrf.taxaccounting.web.main.api.client.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.RevealContentTypeHolder;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.shared.DeclarationListAction;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.shared.DeclarationListResult;
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

import java.util.ArrayList;
import java.util.List;

/**
 * Presenter для страницы администрирования деклараций. Выполняет следующие действия:
 * <ol>
 *     <li>Загружает список деклараций</li>
 *     <li>Переходит к редактированию выбранной декларации</li>
 * </ol>
 */
public class DeclarationTemplateListPresenter extends Presenter<DeclarationTemplateListPresenter.MyView, DeclarationTemplateListPresenter.MyProxy>
		implements DeclarationTemplateListUiHandlers {

	@Title("Шаблоны деклараций")
	@ProxyCodeSplit
	@NameToken(DeclarationTemplateTokens.declarationTemplateList)
	public interface MyProxy extends Proxy<DeclarationTemplateListPresenter>, Place {
	}

	/**
	 * Интерфейс декларации, т.е. представления. Такой, каким видит его Presenter.
	 */
	public interface MyView extends View, HasUiHandlers<DeclarationTemplateListUiHandlers> {
		void setDeclarationTemplateRows(List<DeclarationTemplate> result);
	}

	private final DispatchAsync dispatcher;
	private final PlaceManager placeManager;

	@Inject
	public DeclarationTemplateListPresenter(EventBus eventBus, MyView view, MyProxy proxy, PlaceManager placeManager, DispatchAsync dispatcher) {
		super(eventBus, view, proxy);
		this.dispatcher = dispatcher;
		this.placeManager = placeManager;
		getView().setUiHandlers(this);
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

		dispatcher.execute(new DeclarationListAction(), new AbstractCallback<DeclarationListResult>() {
			@Override
			public void onReqSuccess(DeclarationListResult result) {
				List<DeclarationTemplate> templates = new ArrayList<DeclarationTemplate>();
				DeclarationTemplate declarationTemplate1 = new DeclarationTemplate();
				declarationTemplate1.setId(1);
				templates.add(declarationTemplate1);
				DeclarationTemplate declarationTemplate2 = new DeclarationTemplate();
				declarationTemplate2.setId(2);
				templates.add(declarationTemplate2);
				DeclarationTemplate declarationTemplate3 = new DeclarationTemplate();
				declarationTemplate3.setId(3);
				templates.add(declarationTemplate3);
				DeclarationTemplate declarationTemplate4 = new DeclarationTemplate();
				declarationTemplate4.setId(4);
				templates.add(declarationTemplate4);


				getView().setDeclarationTemplateRows(templates);
				/*
				if (result.getDeclarations() != null && !result.getDeclarations().isEmpty()) {
					getView().setDeclarationTemplateRows(result.getDeclarations());
				}*/
			}
		});
	}

	/**
	 * Когда пользователь выбирает какой-нибудь шаблон декларации, он переходит на страницу редактирования шаблона декарации.
	 *
	 * @param id идентификатор шаблона формы
	 */
	@Override
	public void selectDeclaration(Integer id) {
			placeManager.revealPlace(
					new PlaceRequest(DeclarationTemplateTokens.declarationTemplate).with(
							DeclarationTemplateTokens.declarationTemplateId, String.valueOf(id)
					)
			);
	}

	@Override
	protected void revealInParent() {
		RevealContentEvent.fire(this, RevealContentTypeHolder.getMainContent(), this);
	}

}
