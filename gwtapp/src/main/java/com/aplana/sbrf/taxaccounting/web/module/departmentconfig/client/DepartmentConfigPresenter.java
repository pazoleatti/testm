package com.aplana.sbrf.taxaccounting.web.module.departmentconfig.client;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.web.module.departmentconfig.shared.DepartmentCombined;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.web.main.api.client.RevealContentTypeHolder;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.MessageEvent;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.shared.GetDeclarationResult;
import com.aplana.sbrf.taxaccounting.web.module.departmentconfig.shared.*;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.*;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Presenter для формы настроек подразделений
 *
 * @author Dmitriy Levykin
 */
public class DepartmentConfigPresenter extends Presenter<DepartmentConfigPresenter.MyView, DepartmentConfigPresenter.MyProxy> implements DepartmentConfigUiHandlers {

    @ProxyCodeSplit
    @NameToken(DepartmentConfigTokens.departamentConfig)
    public interface MyProxy extends ProxyPlace<DepartmentConfigPresenter>, Place {
    }

    public interface MyView extends View, HasUiHandlers<DepartmentConfigUiHandlers> {
        void updateVisibility(boolean isUnp);

        void setDepartments(List<Department> departments, Set<Integer> availableDepartment);

        void setDepartment(Department department);

        void setDepartmentCombined(DepartmentCombined combinedDepartmentParam);

        void setTaxTypes(List<TaxType> types);
    }

    private final DispatchAsync dispatcher;

    @Inject
    public DepartmentConfigPresenter(final EventBus eventBus, final MyView view, final MyProxy proxy, DispatchAsync dispatcher, PlaceManager placeManager) {
        super(eventBus, view, proxy, RevealContentTypeHolder.getMainContent());
        this.dispatcher = dispatcher;
        getView().setUiHandlers(this);
    }

    @Override
    public void save(DepartmentCombined combinedDepartmentParam) {
        if (combinedDepartmentParam == null) {
            return;
        }

        SaveDepartmentCombinedAction action = new SaveDepartmentCombinedAction();
        action.setDepartmentCombined(combinedDepartmentParam);
        dispatcher.execute(action, CallbackUtils
                .defaultCallback(new AbstractCallback<SaveDepartmentCombinedResult>() {
                    @Override
                    public void onSuccess(SaveDepartmentCombinedResult result) {
                        MessageEvent.fire(DepartmentConfigPresenter.this, "Параметры подразделения сохранены");
                    }
                }, this));
    }

    @Override
    public void updateDepartment(Integer departmentId) {
        GetDepartmentCombinedAction action = new GetDepartmentCombinedAction();
        action.setDepartmentId(departmentId);
        dispatcher.execute(action, CallbackUtils
                .defaultCallback(new AbstractCallback<GetDepartmentCombinedResult>() {
                    @Override
                    public void onSuccess(GetDepartmentCombinedResult result) {
                        getView().setDepartmentCombined(result.getDepartmentCombined());
                    }
                }, this));
    }

    @Override
    public boolean useManualReveal() {
        return true;
    }

    @Override
    public void prepareFromRequest(PlaceRequest request) {
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

                                getView().updateVisibility(result.getControlUNP());

                                // Список подразделений для справочника
                                getView().setDepartments(result.getDepartments(), result.getAvailableDepartments());
                                // Текущее подразделение пользователя
                                getView().setDepartment(result.getDepartment());
                                // Доступные типы налогов
                                getView().setTaxTypes(Arrays.asList(TaxType.INCOME, TaxType.TRANSPORT));

                            }
                        }, this).addCallback(new ManualRevealCallback<GetOpenDataResult>(this)));
    }

    // TODO Unlock. Реализовать механизм блокировок.
}