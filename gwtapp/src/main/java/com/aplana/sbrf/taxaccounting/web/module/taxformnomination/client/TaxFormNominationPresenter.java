package com.aplana.sbrf.taxaccounting.web.module.taxformnomination.client;

import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.web.main.api.client.RevealContentTypeHolder;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogAddEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogCleanEvent;
import com.aplana.sbrf.taxaccounting.web.module.taxformnomination.client.declarationDestinationsDialog.DeclarationDestinationsPresenter;
import com.aplana.sbrf.taxaccounting.web.module.taxformnomination.client.event.UpdateTable;
import com.aplana.sbrf.taxaccounting.web.module.taxformnomination.client.formDestinationsDialog.FormDestinationsPresenter;
import com.aplana.sbrf.taxaccounting.web.module.taxformnomination.shared.*;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.ManualRevealCallback;
import com.gwtplatform.mvp.client.proxy.Place;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;

import java.util.List;
import java.util.Set;

/**
 * Презентер для формы "Назначение форм и деклараций"
 *
 * @author Stanislav Yasinskiy
 */
public class TaxFormNominationPresenter
        extends Presenter<TaxFormNominationPresenter.MyView, TaxFormNominationPresenter.MyProxy>
        implements TaxFormNominationUiHandlers, UpdateTable.UpdateTableHandler  {

	@ProxyCodeSplit
    @NameToken(TaxFormNominationToken.taxFormNomination)
    public interface MyProxy extends ProxyPlace<TaxFormNominationPresenter>, Place {
    }

    public interface MyView extends View, HasUiHandlers<TaxFormNominationUiHandlers> {
        // загрузка подразделений
        void setDepartments(List<Department> departments, Set<Integer> availableDepartment);

        // Инициализация
        void init(TaxType nType);


        // установка данных
        void setTaxFormKind(List<FormType> formTypes);

        // установка данные в таблицу отображающую данные вкладки "Назначение деклараций"
        void setDataToFormTable(List<FormTypeKind> departmentFormTypes);
        // установка данные в таблицу отображающую данные вкладки "Назначение налоговых форм"
        void setDataToDeclarationTable(List<FormTypeKind> departmentFormTypes);

        // получение данных
        boolean isForm();

        //Long departmentId();

        Integer getTypeId();

        Integer getFormId();

        TaxType getTaxType();

        List<Integer> getDepartments();

	    List<FormTypeKind> getSelectedItemsOnDeclarationGrid();
        List<FormTypeKind> getSelectedItemsOnFormGrid();
    }

    protected final FormDestinationsPresenter formDestinationsPresenter;
    protected final DeclarationDestinationsPresenter declarationDestinationsPresenter;
    private final DispatchAsync dispatcher;

    @Inject
    public TaxFormNominationPresenter(final EventBus eventBus, final MyView view, final MyProxy proxy, DispatchAsync dispatcher, FormDestinationsPresenter formDestinationsPresenter, DeclarationDestinationsPresenter declarationDestinationsPresenter) {
        super(eventBus, view, proxy, RevealContentTypeHolder.getMainContent());
        this.dispatcher = dispatcher;
        this.formDestinationsPresenter = formDestinationsPresenter;
        this.declarationDestinationsPresenter = declarationDestinationsPresenter;
        getView().setUiHandlers(this);
    }

    @Override
    protected void onBind() {
        addRegisteredHandler(UpdateTable.getType(), this);
        super.onBind();
    }

    @Override
    public boolean useManualReveal() {
        return true;
    }

    @Override
    public void prepareFromRequest(final PlaceRequest request) {
        super.prepareFromRequest(request);

        dispatcher.execute(new GetOpenDataAction(),
                CallbackUtils.defaultCallback(
                        new AbstractCallback<GetOpenDataResult>() {
                            @Override
                            public void onSuccess(GetOpenDataResult result) {
                                if (result == null || result.getControlUNP() == null) {
                                    getProxy().manualRevealFailed();
                                    return;
                                }
                                getView().setDepartments(result.getDepartments(), result.getAvailableDepartments());
                                String value = request.getParameter("nType", "");
                                TaxType nType = (value != null && !"".equals(value) ? TaxType.valueOf(value) : null);
                                getView().init(nType);
                                formDestinationsPresenter.initForm(getView().getTaxType());

                            }
                        }, this).addCallback(new ManualRevealCallback<GetOpenDataResult>(this)));
    }

    // TODO Unlock. Реализовать механизм блокировок.

    /**
     * Перезагруска бокса "Вид налоговой формы"/"Вид декларации"
     */
    @Override
    public void getTaxFormKind() {
        GetTaxFormTypesAction action = new GetTaxFormTypesAction();
        action.setTaxType(getView().getTaxType());
        action.setForm(getView().isForm());

        dispatcher.execute(action, CallbackUtils
                .defaultCallback(new AbstractCallback<GetTaxFormTypesResult>() {
                    @Override
                    public void onSuccess(GetTaxFormTypesResult result) {
                        getView().setTaxFormKind(result.getFormTypeList());
                    }
                }, this));
    }

    /**
     * Перезагрузка таблицы, отображающий данные для "Назначение деклараций"
     */
    @Override
    public void reloadFormTableData() {
        dispatcher.execute(getTableDataAction(), CallbackUtils
                .defaultCallback(new AbstractCallback<GetTableDataResult>() {
                    @Override
                    public void onSuccess(GetTableDataResult result) {
                        getView().setDataToFormTable(result.getTableData());
                    }
                }, this));
    }

    @Override
    public void reloadDeclarationTableData(){
        dispatcher.execute(getTableDataAction(), CallbackUtils
		        .defaultCallback(new AbstractCallback<GetTableDataResult>() {
                    @Override
                    public void onSuccess(GetTableDataResult result) {
                        getView().setDataToDeclarationTable(result.getTableData());
                    }
                }, this));
    }

     private GetTableDataAction getTableDataAction(){
         GetTableDataAction action = new GetTableDataAction();
         action.setDepartmentsIds(getView().getDepartments());
         action.setTaxType(getView().getTaxType().getCode());
         action.setForm(getView().isForm());

         return action;
     }

    /**
     * Добавление, удаление зависимостей
     *
     * @param ids список id на удаление или null если нажата кнопка "Назначить"
     */
    @Override
    public void save(Set<Long> ids) {
        SaveAction action = new SaveAction();
        action.setIds(ids);
        // ?? action.setDepartmentsIds(getView().getDepartments());
        action.setTypeId(getView().getTypeId());
        action.setFormId(getView().getFormId());
        action.setTaxType(getView().getTaxType().getCode());
        action.setForm(getView().isForm());
        dispatcher.execute(action, CallbackUtils
                .defaultCallback(new AbstractCallback<GetTableDataResult>() {
                    @Override
                    public void onSuccess(GetTableDataResult result) {
                        if (result.getTableData() != null)
                            getView().setDataToFormTable(result.getTableData());
                        // ??
                    }
                }, this));
    }

    @Override
    public void onClickOpenFormDestinations() {
        formDestinationsPresenter.initAndShowDialog(this);
    }

    @Override
    public void onClickEditFormDestinations(Set<Integer> selectedDepartments, Set<FormDataKind> selectedKinds, Set<Integer> selectedTypes) {
        formDestinationsPresenter.initAndShowEditDialog(this, selectedDepartments, selectedKinds, selectedTypes);
    }

    @Override
    public void onClickOpenDeclarationDestinations() {
	    declarationDestinationsPresenter.initAndShowDialog(this, getView().getTaxType());
    }

	@Override
	public void onClickDeclarationCancelAnchor() {
		DeleteDeclarationSourcesAction action = new DeleteDeclarationSourcesAction();
		action.setKind(getView().getSelectedItemsOnDeclarationGrid());
		dispatcher.execute(action,
				CallbackUtils.defaultCallback(
						new AbstractCallback<DeleteDeclarationSourcesResult>() {
							@Override
							public void onSuccess(DeleteDeclarationSourcesResult result) {
								reloadDeclarationTableData();
								if ((result.getUuid() != null ) && !result.getUuid().isEmpty()) {
									Dialog.errorMessage("Ошибка", "Невозможно снять назначение декларации, т. к. назначение декларации является приемником данных");
									LogAddEvent.fire(TaxFormNominationPresenter.this, result.getUuid());
								}

							}
						}, this));
	}

	@Override
	public void onUpdateTable(UpdateTable event) {
        if (getView().isForm()){
            reloadFormTableData();
        } else {
            reloadDeclarationTableData();
        }
	}

    @Override
    public void onClickFormCancelAnchor(){
        DeleteFormsSourseAction action = new DeleteFormsSourseAction();
        action.setKind(getView().getSelectedItemsOnFormGrid());
        LogCleanEvent.fire(this);
        dispatcher.execute(action,
                CallbackUtils.defaultCallback(
                        new AbstractCallback<DeleteFormsSourceResult>() {
                            @Override
                            public void onSuccess(DeleteFormsSourceResult result) {
                                LogAddEvent.fire(TaxFormNominationPresenter.this, result.getUuid());
                                reloadFormTableData();
                            }
                        }, this));
    }
}
