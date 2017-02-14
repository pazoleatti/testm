package com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.client;

import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.sbrf.taxaccounting.model.TemplateFilter;
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
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.History;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @Title("Шаблоны налоговых форм")
	@ProxyCodeSplit
	@NameToken(DeclarationTemplateTokens.declarationTemplateList)
	public interface MyProxy extends ProxyPlace<DeclarationTemplateListPresenter> {
	}

    private Map<Integer, String> lstHistory = new HashMap<Integer, String>();

	/**
	 * Интерфейс декларации, т.е. представления. Такой, каким видит его Presenter.
	 */
	public interface MyView extends View, HasUiHandlers<DeclarationTemplateListUiHandlers> {
		void setDeclarationTypeTemplateRows(List<DeclarationTypeTemplate> result, Integer selectedId);
        DeclarationTypeTemplate getSelectedElement();
        Integer getSelectedElementId();
    }

	private final DispatchAsync dispatcher;
    protected final FilterDeclarationTemplatePresenter filterPresenter;
    protected final EditFormPresenter editFormPresenter;
    public static final Object OBJECT = new Object();
    public static final Object OBJECT_EDIT_FORM = new Object();
    private Integer selectedItemId;

    @Inject
	public DeclarationTemplateListPresenter(EventBus eventBus, MyView view, MyProxy proxy, PlaceManager placeManager,
                                            DispatchAsync dispatcher, FilterDeclarationTemplatePresenter filterPresenter,
                                            EditFormPresenter editFormPresenter) {
		super(eventBus, view, proxy, RevealContentTypeHolder.getMainContent());
        this.placeManager = placeManager;
        this.dispatcher = dispatcher;
        this.filterPresenter = filterPresenter;
        this.editFormPresenter = editFormPresenter;
        History.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                lstHistory.put(0, lstHistory.get(1));
                lstHistory.put(1, event.getValue());
            }
        });
        getView().setUiHandlers(this);
	}

    private TemplateFilter previousFilter;
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
        String url = DeclarationTemplateTokens.declarationVersionList + ";" + DeclarationTemplateTokens.declarationType;
        if ((lstHistory.get(0) == null || !lstHistory.get(0).startsWith(url)) &&
                (lstHistory.get(1) == null || !lstHistory.get(1).startsWith(url))) {
            previousFilter = null;
        }
        LogCleanEvent.fire(this);
        LogShowEvent.fire(this, false);
        filterPresenter.initFilter(previousFilter);
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
        selectedItemId = getView().getSelectedElementId();
    }

    @Override
    public void onClickFind(DeclarationTemplateApplyEvent event) {
        updateDeclarationData();
    }

    @Override
    public void onFilterReady(FilterDeclarationTemplateReadyEvent event) {
        updateDeclarationData(selectedItemId);
    }

    public void updateDeclarationData() {
        updateDeclarationData(null);
    }

    public void updateDeclarationData(final Integer selectedId) {
        previousFilter =  filterPresenter.getFilterData();
        DeclarationListAction action = new DeclarationListAction();
        action.setFilter(filterPresenter.getFilterData());
        dispatcher.execute(action,	CallbackUtils.defaultCallback(
                new AbstractCallback<DeclarationListResult>() {
            @Override
            public void onSuccess(DeclarationListResult result) {
                    getView().setDeclarationTypeTemplateRows(result.getTypeTemplates(),selectedId);
            }
        }, this).addCallback(new ManualRevealCallback<DeclarationListResult>(DeclarationTemplateListPresenter.this)));
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
                LogAddEvent.fire(DeclarationTemplateListPresenter.this, result.getLogEntriesUuid());
                placeManager.revealPlace(new PlaceRequest.Builder().nameToken(DeclarationTemplateTokens.declarationTemplateList).build());
            }
        }, this).addCallback(new ManualRevealCallback<DTDeleteResult>(DeclarationTemplateListPresenter.this)));
    }

    @Override
    public void onUpdateTable(UpdateTableEvent event) {
        updateDeclarationData(getView().getSelectedElementId());
        if (event.getUuid() != null)
            LogAddEvent.fire(this, event.getUuid());
    }

    @Override
    public void onSelectionChanged(DeclarationTypeTemplate selectedItem) {
        if (selectedItem != null) {
            editFormPresenter.setDeclarationTypeId(selectedItem.getTypeId());
            editFormPresenter.setDeclarationTypeData(selectedItem.getTypeId());
        }
    }
}
