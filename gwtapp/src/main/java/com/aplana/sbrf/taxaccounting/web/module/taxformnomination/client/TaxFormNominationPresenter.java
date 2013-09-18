package com.aplana.sbrf.taxaccounting.web.module.taxformnomination.client;

import java.util.List;
import java.util.Set;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.FormType;
import com.aplana.sbrf.taxaccounting.model.FormTypeKind;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.web.main.api.client.RevealContentTypeHolder;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.module.taxformnomination.shared.GetOpenDataAction;
import com.aplana.sbrf.taxaccounting.web.module.taxformnomination.shared.GetOpenDataResult;
import com.aplana.sbrf.taxaccounting.web.module.taxformnomination.shared.GetTableDataAction;
import com.aplana.sbrf.taxaccounting.web.module.taxformnomination.shared.GetTableDataResult;
import com.aplana.sbrf.taxaccounting.web.module.taxformnomination.shared.GetTaxFormTypesAction;
import com.aplana.sbrf.taxaccounting.web.module.taxformnomination.shared.GetTaxFormTypesResult;
import com.aplana.sbrf.taxaccounting.web.module.taxformnomination.shared.SaveAction;
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

/**
 * Презентер для формы "Назначение форм и деклараций"
 *
 * @author Stanislav Yasinskiy
 */
public class TaxFormNominationPresenter
        extends Presenter<TaxFormNominationPresenter.MyView, TaxFormNominationPresenter.MyProxy>
        implements TaxFormNominationUiHandlers {

    @ProxyCodeSplit
    @NameToken(TaxFormNominationToken.taxFormNomination)
    public interface MyProxy extends ProxyPlace<TaxFormNominationPresenter>, Place {
    }

    public interface MyView extends View, HasUiHandlers<TaxFormNominationUiHandlers> {
        // загрузка подразделений
        void setDepartments(List<Department> departments, Set<Integer> availableDepartment);

        // Инициализация
        void init(Boolean isForm);


        // установка данных
        void setTaxFormKind(List<FormType> formTypes);

        void setTableData(List<FormTypeKind> departmentFormTypes);

        // получение данных
        boolean isForm();

        Long departmentId();

        Integer getTypeId();

        Integer getFormId();

        TaxType getTaxType();


    }

    private final DispatchAsync dispatcher;

    @Inject
    public TaxFormNominationPresenter(final EventBus eventBus, final MyView view, final MyProxy proxy, DispatchAsync dispatcher) {
        super(eventBus, view, proxy, RevealContentTypeHolder.getMainContent());
        this.dispatcher = dispatcher;
        getView().setUiHandlers(this);
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
                                getView().init(Boolean.valueOf(request.getParameter("isForm", "")));

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
     * Перезагрузка таблицы
     */
    @Override
    public void getTableData() {
        GetTableDataAction action = new GetTableDataAction();
        action.setDepoId(getView().departmentId());
        action.setTaxType(getView().getTaxType().getCode());
        action.setForm(getView().isForm());
        dispatcher.execute(action, CallbackUtils
                .defaultCallback(new AbstractCallback<GetTableDataResult>() {
                    @Override
                    public void onSuccess(GetTableDataResult result) {
                        getView().setTableData(result.getTableData());
                    }
                }, this));
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
        action.setDepartmentId(getView().departmentId());
        action.setTypeId(getView().getTypeId());
        action.setFormId(getView().getFormId());
        action.setTaxType(getView().getTaxType().getCode());
        action.setForm(getView().isForm());
        dispatcher.execute(action, CallbackUtils
                .defaultCallback(new AbstractCallback<GetTableDataResult>() {
                    @Override
                    public void onSuccess(GetTableDataResult result) {
                        if (result.getTableData() != null)
                            getView().setTableData(result.getTableData());
                    }
                }, this));
    }
}
