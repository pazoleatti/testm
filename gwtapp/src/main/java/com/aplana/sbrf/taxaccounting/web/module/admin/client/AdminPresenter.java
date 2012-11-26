package com.aplana.sbrf.taxaccounting.web.module.admin.client;

import com.aplana.sbrf.taxaccounting.model.FormTemplate;
import com.aplana.sbrf.taxaccounting.web.main.api.client.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.RevealContentTypeHolder;
import com.aplana.sbrf.taxaccounting.web.module.admin.shared.FormListAction;
import com.aplana.sbrf.taxaccounting.web.module.admin.shared.FormListResult;
import com.google.gwt.user.client.ui.ListBox;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.*;

import java.util.List;

/**
 * Presenter для страницы администрирования. Выполняет следующие действия:
 * <ol>
 *     <li>Загружает список шаблонов форм</li>
 *     <li>Переходит к редактированию выбранного шаблона формы</li>
 * </ol>
 * @author Vitalii Samolovskikh
 */
public class AdminPresenter extends Presenter<AdminPresenter.MyView, AdminPresenter.MyProxy> implements AdminUiHandlers {
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
			public void onSuccess(FormListResult result) {
			    ListBox lb = getView().getListBox();
				lb.clear();
				for(FormTemplate ft: result.getForms()){
					lb.addItem(ft.getType().getName(), ft.getId().toString());
				}
			}
		});
	}

	/**
	 * Когда пользователь выбирает какой-нибудь шаблон формы, он переходит на страницу редактирования шаблона формы.
	 * Это происходит в этом методе. Получаем из списка шаблонов формы тот, который выбрал пользователь и
	 * переходим на страницу редактирования нужного шаблона.
	 */
	@Override
	public void select() {
		ListBox lb = getView().getListBox();
		int selectedIndex = lb.getSelectedIndex();
		if (selectedIndex >= 0) {
			placeManager.revealPlace(
					new PlaceRequest(AdminNameTokens.formTemplatePage).with(
							FormTemplatePresenter.PARAM_FORM_TEMPLATE_ID, lb.getValue(selectedIndex)
					)
			);
		}
	}

	/**
	 * TODO: понять
	 */
	@Override
	protected void revealInParent() {
		RevealContentEvent.fire(this, RevealContentTypeHolder.getMainContent(), this);
	}

	/**
	 * TODO: Про этот интерфейс тоже надо почитать.
	 */
	@ProxyCodeSplit
	@NameToken(AdminNameTokens.adminPage)
	public interface MyProxy extends Proxy<AdminPresenter>, Place {
	}

	/**
	 * Интерфейс формы, т.е. вида, т.е. представления. Такой, каким видит его Presenter.
	 */
	public interface MyView extends View, HasUiHandlers<AdminUiHandlers> {
		public ListBox getListBox();
	}
}
