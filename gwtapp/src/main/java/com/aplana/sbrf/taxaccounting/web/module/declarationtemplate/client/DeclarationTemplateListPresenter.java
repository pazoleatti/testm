package com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.client;

import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.sbrf.taxaccounting.web.main.api.client.RevealContentTypeHolder;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogAddEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogCleanEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogShowEvent;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.client.editform.EditFormPresenter;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.client.event.DTCreateNewTypeEvent;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.client.event.UpdateTableEvent;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.client.filter.DeclarationTemplateApplyEvent;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.client.filter.FilterDeclarationTemplatePresenter;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.client.filter.FilterDeclarationTemplateReadyEvent;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.shared.*;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.annotations.Title;
import com.gwtplatform.mvp.client.proxy.ManualRevealCallback;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;

import java.util.List;

/**
 * Presenter для страницы администрирования деклараций. Выполняет следующие действия:
 * <ol>
 *     <li>Загружает список деклараций</li>
 *     <li>Переходит к редактированию выбранной декларации</li>
 * </ol>
 */
public class DeclarationTemplateListPresenter
        extends Presenter<DeclarationTemplateListPresenter.MyView, DeclarationTemplateListPresenter.MyProxy>
        implements DeclarationTemplateApplyEvent.MyHandler, FilterDeclarationTemplateReadyEvent.MyHandler,
        DeclarationTemplateListUiHandlers, UpdateTableEvent.MyHandler {

    private final PlaceManager placeManager;

    @Title("Шаблоны деклараций")
	@ProxyCodeSplit
	@NameToken(DeclarationTemplateTokens.declarationTemplateList)
	public interface MyProxy extends ProxyPlace<DeclarationTemplateListPresenter> {
	}

	/**
	 * Интерфейс декларации, т.е. представления. Такой, каким видит его Presenter.
	 */
	public interface MyView extends View, HasUiHandlers<DeclarationTemplateListUiHandlers> {
		void setDeclarationTypeTemplateRows(List<DeclarationTypeTemplate> result);
        DeclarationTypeTemplate getSelectedElement();
	}

	private final DispatchAsync dispatcher;
    protected final FilterDeclarationTemplatePresenter filterPresenter;
    protected final EditFormPresenter editFormPresenter;
    public static final Object OBJECT = new Object();
    public static final Object OBJECT_EDIT_FORM = new Object();

    @Inject
	public DeclarationTemplateListPresenter(EventBus eventBus, MyView view, MyProxy proxy, PlaceManager placeManager,
                                            DispatchAsync dispatcher, FilterDeclarationTemplatePresenter filterPresenter,
                                            EditFormPresenter editFormPresenter) {
		super(eventBus, view, proxy, RevealContentTypeHolder.getMainContent());
        this.placeManager = placeManager;
        this.dispatcher = dispatcher;
        this.filterPresenter = filterPresenter;
        this.editFormPresenter = editFormPresenter;
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
        LogCleanEvent.fire(this);
        LogShowEvent.fire(this, false);
        filterPresenter.initFilter();
	}

	@Override
	public boolean useManualReveal() {
		return true;
	}


    @Override
    protected void onBind() {
        addRegisteredHandler(FilterDeclarationTemplateReadyEvent.getType(), this);
        addRegisteredHandler(DeclarationTemplateApplyEvent.getType(), this);
        addRegisteredHandler(UpdateTableEvent.getType(), this);
        super.onBind();
    }

    @Override
    protected void onReveal() {
        super.onReveal();
        setInSlot(OBJECT, filterPresenter);
        setInSlot(OBJECT_EDIT_FORM, editFormPresenter);
    }

    @Override
    protected void onHide() {
        super.onHide();
        clearSlot(OBJECT);
        clearSlot(OBJECT_EDIT_FORM);
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
                    getView().setDeclarationTypeTemplateRows(result.getTypeTemplates());
            }
        }, this).addCallback(
                new ManualRevealCallback<DeclarationListResult>(
                DeclarationTemplateListPresenter.this)));
    }

    @Override
    public void onCreateClicked() {
        if (filterPresenter.getFilterData().getTaxType() == null){
            Dialog.infoMessage("Выберите вид налога");
            return;
        }
        DTCreateNewTypeEvent.fire(this, filterPresenter.getFilterData().getTaxType());
    }

    @Override
    public void onDeleteClicked() {
        DeclarationTypeTemplate declarationTypeTemplate = getView().getSelectedElement();
        if (declarationTypeTemplate == null)
            return;
        DTDeleteAction action = new DTDeleteAction();
        action.setDtTypeId(declarationTypeTemplate.getTypeId());
        dispatcher.execute(action, CallbackUtils.defaultCallback(new AbstractCallback<DTDeleteResult>() {
            @Override
            public void onSuccess(DTDeleteResult result) {
                if (result.getLogEntriesUuid() != null)
                    LogAddEvent.fire(DeclarationTemplateListPresenter.this, result.getLogEntriesUuid());
                placeManager.revealPlace(new PlaceRequest.Builder().nameToken(DeclarationTemplateTokens.declarationTemplateList).build());
            }
        }, this).addCallback(new ManualRevealCallback<DTDeleteResult>(DeclarationTemplateListPresenter.this)));
    }

    @Override
    public void onUpdateTable(UpdateTableEvent event) {
        updateDeclarationData();
    }

    @Override
    public void onSelectionChanged(DeclarationTypeTemplate selectedItem) {
        if (selectedItem != null) {
            editFormPresenter.setDeclarationTypeId(selectedItem.getTypeId());
            editFormPresenter.setDeclarationTypeName(selectedItem.getTypeName());
        }

    }
}
