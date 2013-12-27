package com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.client;

import java.util.ArrayList;
import java.util.List;

import com.aplana.sbrf.taxaccounting.model.DeclarationTemplate;
import com.aplana.sbrf.taxaccounting.model.TemplateFilter;
import com.aplana.sbrf.taxaccounting.web.main.api.client.RevealContentTypeHolder;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.client.filter.DeclarationTemplateApplyEvent;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.client.filter.FilterDeclarationTemplatePresenter;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.client.filter.FilterDeclarationTemplateReadyEvent;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.shared.DeclarationListAction;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.shared.DeclarationListResult;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;
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
public class DeclarationTemplateListPresenter
        extends Presenter<DeclarationTemplateListPresenter.MyView, DeclarationTemplateListPresenter.MyProxy>
        implements DeclarationTemplateApplyEvent.MyHandler, FilterDeclarationTemplateReadyEvent.MyHandler{

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
    protected final FilterDeclarationTemplatePresenter filterPresenter;
    public static final Object OBJECT = new Object();
    List<HandlerRegistration> handlers = new ArrayList<HandlerRegistration>();

	@Inject
	public DeclarationTemplateListPresenter(EventBus eventBus, MyView view, MyProxy proxy, DispatchAsync dispatcher, FilterDeclarationTemplatePresenter filterPresenter) {
		super(eventBus, view, proxy, RevealContentTypeHolder.getMainContent());
		this.dispatcher = dispatcher;
        this.filterPresenter = filterPresenter;
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
        TemplateFilter defaultFilter = new TemplateFilter();
        defaultFilter.setTaxType(null);
        defaultFilter.setActive(true);
        filterPresenter.initFilter(defaultFilter);

	}

	@Override
	public boolean useManualReveal() {
		return true;
	}


    @Override
    protected void onBind() {
        addRegisteredHandler(FilterDeclarationTemplateReadyEvent.getType(), this);
        addRegisteredHandler(DeclarationTemplateApplyEvent.getType(), this);
        super.onBind();
    }

    @Override
    protected void onReveal() {
        super.onReveal();
        setInSlot(OBJECT, filterPresenter);
    }

    @Override
    protected void onHide() {
        super.onHide();
        clearSlot(OBJECT);
    }

    @Override
    public void onClickFind(DeclarationTemplateApplyEvent event) {
        updateDeclarationData();
    }

    @Override
    public void onFilterReady(FilterDeclarationTemplateReadyEvent event) {
        updateDeclarationData();
    }

    public void updateDeclarationData() {
        DeclarationListAction action = new DeclarationListAction();
        action.setFilter(filterPresenter.getFilterData());
        dispatcher.execute(action,	CallbackUtils.defaultCallback(
                new AbstractCallback<DeclarationListResult>() {
            @Override
            public void onSuccess(
                    DeclarationListResult result) {
                    getView().setDeclarationTemplateRows(result.getDeclarations());
            }
        }, this).addCallback(
                new ManualRevealCallback<DeclarationListResult>(
                DeclarationTemplateListPresenter.this)));
    }
}
