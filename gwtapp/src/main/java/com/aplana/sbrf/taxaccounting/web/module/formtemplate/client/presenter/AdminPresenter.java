package com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.presenter;

import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.model.TemplateFilter;
import com.aplana.sbrf.taxaccounting.web.main.api.client.RevealContentTypeHolder;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogAddEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogCleanEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogShowEvent;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.AdminConstants;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.editform.EditFormPresenter;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.event.FormTemplateMainEvent;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.event.UpdateTableEvent;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.filter.FilterFormTemplatePresenter;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.filter.FilterFormTemplateReadyEvent;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.filter.FormTemplateApplyEvent;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.shared.*;
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
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;

import java.util.HashMap;
import java.util.List;

/**
 * Presenter для страницы администрирования. Выполняет следующие действия:
 * <ol>
 * <li>Загружает список шаблонов форм</li>
 * <li>Переходит к редактированию выбранного шаблона формы</li>
 * </ol>
 * 
 * @author Vitalii Samolovskikh
 */
public class AdminPresenter
        extends Presenter<AdminPresenter.MyView, AdminPresenter.MyProxy>
        implements FormTemplateApplyEvent.FormDataApplyHandler, FilterFormTemplateReadyEvent.MyHandler,
        AdminUIHandlers, UpdateTableEvent.MyHandler {

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
	public interface MyView extends View, HasUiHandlers<AdminUIHandlers> {
		void setFormTemplateTable(List<FormTypeTemplate> formTypeTemplates);
        FormTypeTemplate getSelectedElement();
	}

	private final DispatchAsync dispatcher;
    protected final FilterFormTemplatePresenter filterPresenter;
    protected final EditFormPresenter editFormPresenter;
    public static final Object TYPE_filterPresenter = new Object();
    public static final Object TYPE_editPresenter = new Object();

    private TemplateFilter previousFilter;

    private HashMap<Integer, String> lstHistory = new HashMap<Integer, String>();

    @Inject
	public AdminPresenter(EventBus eventBus, MyView view, MyProxy proxy, DispatchAsync dispatcher,
                          FilterFormTemplatePresenter filterPresenter, EditFormPresenter editFormPresenter) {
		super(eventBus, view, proxy, RevealContentTypeHolder.getMainContent());
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
        LogCleanEvent.fire(this);
        LogShowEvent.fire(this, false);
        String url = AdminConstants.NameTokens.formTemplateVersionList + ";" + AdminConstants.NameTokens.formTypeId;
        if ((lstHistory.get(0) == null || !lstHistory.get(0).startsWith(url)) &&
                (lstHistory.get(1) == null || !lstHistory.get(1).startsWith(url))) {
            previousFilter = null;
        }
        filterPresenter.initFilter(previousFilter);
	}

    @Override
    protected void onBind() {
        super.onBind();
        addRegisteredHandler(FilterFormTemplateReadyEvent.getType(), this);
        addRegisteredHandler(FormTemplateApplyEvent.getType(), this);
        addRegisteredHandler(UpdateTableEvent.getType(), this);
    }

    @Override
    protected void onReveal() {
        super.onReveal();
        setInSlot(TYPE_filterPresenter, filterPresenter);
        setInSlot(TYPE_editPresenter, editFormPresenter);
    }

    @Override
    protected void onHide() {
        super.onHide();
        clearSlot(TYPE_filterPresenter);
        clearSlot(TYPE_editPresenter);
    }

    @Override
    public void onClickFind(FormTemplateApplyEvent event) {
        updateFormData();
    }

    @Override
    public void onFilterReady(FilterFormTemplateReadyEvent event) {
        if (event.isSuccess())
            updateFormData();

    }

    public void updateFormData() {
        previousFilter =  filterPresenter.getFilterData();
        GetFormTemplateListAction action = new GetFormTemplateListAction();
        action.setFilter(filterPresenter.getFilterData());
        dispatcher.execute(
                action,
                CallbackUtils.defaultCallback(
                        new AbstractCallback<GetFormTemplateListResult>() {
                            @Override
                            public void onSuccess(
                                    GetFormTemplateListResult result) {
                                getView().setFormTemplateTable(
                                        result.getFormTypeTemplates());
                            }
                        }, this).addCallback(
                        new ManualRevealCallback<GetFormTemplateListResult>(
                                AdminPresenter.this)));
    }

    @Override
    public void onCreateClicked() {
        TaxType taxType = filterPresenter.getFilterData().getTaxType();
        if (taxType == null){
            Dialog.infoMessage("Выберите вид налога");
            return;
        }
        FormTemplateMainEvent.fire(this, taxType);
    }

    @Override
    public void onDeleteClick() {
        FormTypeTemplate formTypeTemplate = getView().getSelectedElement();
        if (formTypeTemplate == null)
            return;
        DeleteFormTypeAction action = new DeleteFormTypeAction();
        action.setFormTypeId(formTypeTemplate.getFormTypeId());
        dispatcher.execute(action, CallbackUtils.defaultCallback(new AbstractCallback<DeleteFormTypeResult>() {
            @Override
            public void onSuccess(DeleteFormTypeResult result) {
                LogAddEvent.fire(AdminPresenter.this, result.getUuid());
                updateFormData();
            }
        }, this));
    }

    @Override
    public void onSelectionChanged(FormTypeTemplate selectedItem) {
        if (selectedItem != null) {
            editFormPresenter.setFormTypeId(selectedItem.getFormTypeId());
            editFormPresenter.setFormTypeName(selectedItem.getFormTypeName());
            editFormPresenter.setFormTypeCode(selectedItem.getFormTypeCode());
        } else {
            editFormPresenter.setFormTypeId(0);
            editFormPresenter.setFormTypeCode("");
            editFormPresenter.setFormTypeName("");

        }
    }

    @Override
    public void onUpdateTable(UpdateTableEvent event) {
        updateFormData();
        if (event.getUuid() != null)
            LogAddEvent.fire(this, event.getUuid());
    }
}
