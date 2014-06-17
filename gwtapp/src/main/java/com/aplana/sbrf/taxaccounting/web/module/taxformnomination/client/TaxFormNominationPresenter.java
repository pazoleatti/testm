package com.aplana.sbrf.taxaccounting.web.module.taxformnomination.client;

import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.web.main.api.client.RevealContentTypeHolder;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogAddEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogCleanEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogShowEvent;
import com.aplana.sbrf.taxaccounting.web.module.taxformnomination.client.declarationDestinationsDialog.DeclarationDestinationsPresenter;
import com.aplana.sbrf.taxaccounting.web.module.taxformnomination.client.event.UpdateTable;
import com.aplana.sbrf.taxaccounting.web.module.taxformnomination.client.formDestinationsDialog.FormDestinationsPresenter;
import com.aplana.sbrf.taxaccounting.web.module.taxformnomination.shared.*;
import com.aplana.sbrf.taxaccounting.web.module.taxformnomination.shared.model.TaxNominationColumnEnum;
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
import java.util.Map;
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

        void setDepartments(List<Integer> department);

        // Инициализация
        void init(TaxType nType, boolean isForm);

        // установка данные в таблицу отображающую данные вкладки "Назначение деклараций"
        void setDataToFormTable(int start, int totalCount, List<FormTypeKind> departmentFormTypes, Map<Integer, String> departmentFullNames);
        // установка данные в таблицу отображающую данные вкладки "Назначение налоговых форм"
        void setDataToDeclarationTable(int start, int totalCount, List<FormTypeKind> departmentFormTypes, Map<Integer, String> departmentFullNames);

        // получение данных
        boolean isForm();

        List<Integer> getDepartments();

	    List<FormTypeKind> getSelectedItemsOnDeclarationGrid();
        List<FormTypeKind> getSelectedItemsOnFormGrid();

        /**
         * Обновление достпности кнопок редактировать/отменить назначение
         */
        void updateButtonsEnabled();

        /**
         * Обновление страницы
         */
        void onReveal();

        void clearFilter();
    }

    private TaxType taxType;

    protected final FormDestinationsPresenter formDestinationsPresenter;
    protected final DeclarationDestinationsPresenter declarationDestinationsPresenter;
    private final DispatchAsync dispatcher;

    @Inject
    public TaxFormNominationPresenter(final EventBus eventBus, final MyView view,
                                      final MyProxy proxy, DispatchAsync dispatcher,
                                      FormDestinationsPresenter formDestinationsPresenter,
                                      DeclarationDestinationsPresenter declarationDestinationsPresenter) {
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
        LogCleanEvent.fire(this);
        LogShowEvent.fire(this, false);

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
                                TaxFormNominationPresenter.this.taxType = nType;
                                boolean isForm = Boolean.valueOf(request.getParameter("isForm", ""));
                                getView().init(nType, isForm);
                                formDestinationsPresenter.initForm(nType);

                            }
                        }, this).addCallback(new ManualRevealCallback<GetOpenDataResult>(this)));
    }

    // TODO Unlock. Реализовать механизм блокировок.

    /**
     * Перезагрузка таблицы, отображающий данные для "Назначение деклараций"
     */
    @Override
    public void reloadFormTableData() {
        dispatcher.execute(getTableDataAction(), CallbackUtils
                .defaultCallback(new AbstractCallback<GetTableDataResult>() {
                    @Override
                    public void onSuccess(GetTableDataResult result) {
                        getView().setDataToFormTable(0, result.getTotalCount(), result.getTableData(), result.getDepartmentFullNames());
                        getView().updateButtonsEnabled();
                    }
                }, this));
    }

    @Override
    public void reloadDeclarationTableData(){
        dispatcher.execute(getTableDataAction(), CallbackUtils
		        .defaultCallback(new AbstractCallback<GetTableDataResult>() {
                    @Override
                    public void onSuccess(GetTableDataResult result) {
                        getView().setDataToDeclarationTable(0, result.getTotalCount(), result.getTableData(), result.getDepartmentFullNames());
                        getView().updateButtonsEnabled();
                    }
                }, this));
    }

     private GetTableDataAction getTableDataAction(){
         GetTableDataAction action = new GetTableDataAction();
         action.setDepartmentsIds(getView().getDepartments());
         action.setTaxType(taxType.getCode());
         action.setForm(getView().isForm());

         return action;
     }

    @Override
    public void onClickOpenFormDestinations() {
        formDestinationsPresenter.initAndShowDialog(this, taxType);
    }

    @Override
    public void onClickEditFormDestinations(List<FormTypeKind> formTypeKinds) {
        formDestinationsPresenter.initAndShowEditDialog(this, formTypeKinds);
    }

    @Override
    public void onClickOpenDeclarationDestinations() {
	    declarationDestinationsPresenter.initAndShowDialog(this, taxType);
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
                                    if (result.isExistDeclaration()) {
                                        Dialog.errorMessage("Невозможно отменить назначение, т.к. созданы экземпляры декларации");
                                    } else {
                                        Dialog.errorMessage("Невозможно снять назначение декларации, т.к. назначение декларации является приемником данных");
                                        // TODO удаление связей
                                    }
									LogAddEvent.fire(TaxFormNominationPresenter.this, result.getUuid());
								}

							}
						}, this));
	}

	@Override
	public void onUpdateTable(UpdateTable event) {
        if (getView().isForm()){
            if (event.getDepartments() != null){
                getView().setDepartments(event.getDepartments());
            }
            reloadFormTableData();
        } else {
	        getView().setDepartments(event.getDepartments());
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
                                if (result.getUuid() != null){
                                    if (result.isExistFormData()) {
                                        Dialog.errorMessage("Ошибка", "Невозможно отменить назначение, т. к. созданы экземпляры налоговых форм");
                                    } else {
                                        Dialog.errorMessage("Ошибка", "Невозможно снять назначение налоговой формы, т. к. назначение является приемником данных / назначение является источником данных");
                                        // TODO удаление источников-приемников
                                    }
                                }
                                reloadFormTableData();
                            }
                        }, this));
    }

    @Override
    protected void onReveal() {
        super.onReveal();
        getView().onReveal();
    }

    @Override
    protected void onHide() {
        super.onHide();
        getView().clearFilter();
    }

    @Override
    public void onFormRangeChange(final int start, int length, TaxNominationColumnEnum sort, boolean asc) {
        GetTableDataAction action = getTableDataAction();
        action.setCount(length);
        action.setStartIndex(start);
        action.setSortColumn(sort);
        action.setAsc(asc);

        dispatcher.execute(action, CallbackUtils
                .defaultCallback(new AbstractCallback<GetTableDataResult>() {
                    @Override
                    public void onSuccess(GetTableDataResult result) {
                        getView().setDataToFormTable(start, result.getTotalCount(), result.getTableData(), result.getDepartmentFullNames());
                        getView().updateButtonsEnabled();
                    }
                }, this));
    }


    @Override
    public void onDeclarationRangeChange(final int start, int length, TaxNominationColumnEnum sort, boolean asc) {
        GetTableDataAction action = getTableDataAction();
        action.setCount(length);
        action.setStartIndex(start);
        action.setSortColumn(sort);
        action.setAsc(asc);

        dispatcher.execute(action, CallbackUtils
                .defaultCallback(new AbstractCallback<GetTableDataResult>() {
                    @Override
                    public void onSuccess(GetTableDataResult result) {
                        getView().setDataToDeclarationTable(start, result.getTotalCount(), result.getTableData(), result.getDepartmentFullNames());
                        getView().updateButtonsEnabled();
                    }
                }, this));
    }
}
