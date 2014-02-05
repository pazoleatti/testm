package com.aplana.sbrf.taxaccounting.web.module.departmentconfig.client;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.web.main.api.client.RevealContentTypeHolder;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.MessageEvent;
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

import java.util.*;

/**
 * Presenter для формы настроек подразделений
 *
 * @author Dmitriy Levykin
 */
public class DepartmentConfigPresenter extends Presenter<DepartmentConfigPresenter.MyView,
        DepartmentConfigPresenter.MyProxy> implements DepartmentConfigUiHandlers {

    @ProxyCodeSplit
    @NameToken(DepartmentConfigTokens.departamentConfig)
    public interface MyProxy extends ProxyPlace<DepartmentConfigPresenter>, Place {
    }

    private final DispatchAsync dispatcher;

    private Department userDepartment;

    public interface MyView extends View, HasUiHandlers<DepartmentConfigUiHandlers> {
        /**
         * Данные справочника "Подразделения"
         * @param departments Список подразделений дерева справочника
         * @param availableDepartment Список подразделений, которые доступны для выбора
         */
        void setDepartments(List<Department> departments, Set<Integer> availableDepartment);

        /**
         * Установка выбранного подразделения
         * @param department
         */
        void setDepartment(Department department);

        /**
         * Установка доступных отчетных периодов
         * @param reportPeriods
         */
        void setReportPeriods(List<ReportPeriod> reportPeriods);

        /**
         * Установка выбранного отчетного периода
         * @param reportPeriodId
         */
        void setReportPeriod(Integer reportPeriodId);

        /**
         * Установка параметров подразделения
         * @param combinedDepartmentParam
         */
        void setDepartmentCombined(DepartmentCombined combinedDepartmentParam);

        /**
         * Устанавливаем всем справочникам на форме "ограничивающий период"
         * @param reportPeriodId идентификатор отчетного периода
         */
        void resetRefBookWidgetsDatePeriod(Integer reportPeriodId);

        /**
         * Установка типа налога
         * @param type
         */
        void setTaxType(TaxType type);

        /**
         * Перезагрузка параметров подразделения
         */
        void reloadDepartmentParams();

        /**
         * Очистка формы
         */
        void clear();

        /**
         * Обновление дерева подразделений
         */
        void reloadDepartments();

        /**
         * Установка разыменованных значений для справочников
         */
        void setDereferenceValue(Map<Long, String> rbTextValues);

        /**
         * Признак открытости выбранного отчетного периода
         * @param reportPeriodActive
         */
        void setReportPeriodActive(boolean reportPeriodActive);

        TaxType getTaxType();
    }

    @Inject
    public DepartmentConfigPresenter(final EventBus eventBus, final MyView view, final MyProxy proxy,
                                     DispatchAsync dispatcher, PlaceManager placeManager) {
        super(eventBus, view, proxy, RevealContentTypeHolder.getMainContent());
        this.dispatcher = dispatcher;
        getView().setUiHandlers(this);
    }

    @Override
    public void save(DepartmentCombined combinedDepartmentParam, Integer period) {
        if (combinedDepartmentParam == null || period == null) {
            return;
        }

        SaveDepartmentCombinedAction action = new SaveDepartmentCombinedAction();
        action.setDepartmentCombined(combinedDepartmentParam);
        action.setReportPeriodId(period);
        action.setTaxType(getView().getTaxType());
        dispatcher.execute(action, CallbackUtils
                .defaultCallback(new AbstractCallback<SaveDepartmentCombinedResult>() {
                    @Override
                    public void onSuccess(SaveDepartmentCombinedResult result) {
                        MessageEvent.fire(DepartmentConfigPresenter.this, "Параметры подразделения сохранены");
                    }
                }, this));
    }

    @Override
    public void clear() {
        getView().clear();
    }

    @Override
    public void reloadDepartmentParams(Integer departmentId, TaxType taxType, Integer reportPeriodId) {
        if (departmentId == null || taxType == null || reportPeriodId == null) {
            getView().clear();
            return;
        }

        GetDepartmentCombinedAction action = new GetDepartmentCombinedAction();
        action.setDepartmentId(departmentId);
        action.setTaxType(taxType);
        action.setReportPeriodId(reportPeriodId);
        dispatcher.execute(action, CallbackUtils
                .defaultCallback(new AbstractCallback<GetDepartmentCombinedResult>() {
                    @Override
                    public void onSuccess(GetDepartmentCombinedResult result) {
                        getView().setDepartmentCombined(result.getDepartmentCombined());
                        getView().setDereferenceValue(result.getRbTextValues());
                        getView().setReportPeriodActive(result.isReportPeriodActive());
                        result.getRbTextValues();
                    }
                }, this));
    }

    @Override
    public void reloadDepartments(TaxType taxType, final Integer currentDepartmentId) {
        GetDepartmentTreeDataAction action = new GetDepartmentTreeDataAction();
        action.setTaxType(taxType);

        dispatcher.execute(action,
                CallbackUtils.defaultCallback(
                        new AbstractCallback<GetDepartmentTreeDataResult>() {
                            @Override
                            public void onSuccess(GetDepartmentTreeDataResult result) {
                                // Дерево подразделений
                                if (result.getAvailableDepartments() != null && result.getDepartments() != null) {
                                    getView().setDepartments(result.getDepartments(), result.getAvailableDepartments());
                                }
                                else {
                                    getView().setDepartments(new ArrayList<Department>(), new HashSet<Integer>());
                                }

                                if (result.getAvailableDepartments() != null
                                        && result.getAvailableDepartments().contains(currentDepartmentId)) {
                                    // Выбирается подразделение выбранное ранее
                                    for (Department dep : result.getDepartments()) {
                                        if (dep.getId() == currentDepartmentId) {
                                            getView().setDepartment(dep);
                                            break;
                                        }
                                    }
                                } else {
                                    // Выбирается подразделение пользователя
                                    getView().setDepartment(userDepartment);
                                }
                                // Список отчетных периодов
                                getView().setReportPeriods(result.getReportPeriods());

                                // По-умолчанию последний
                                if (result.getReportPeriods() != null && !result.getReportPeriods().isEmpty()) {
                                    getView().setReportPeriod(result.getReportPeriods().get(result.getReportPeriods().size()-1).getId());
                                }
                            }
                        }, this).addCallback(new ManualRevealCallback<GetDepartmentTreeDataAction>(this)));
    }

    @Override
    public boolean useManualReveal() {
        return true;
    }

    @Override
    public void prepareFromRequest(PlaceRequest request) {
        super.prepareFromRequest(request);

        String value = request.getParameter("nType", "");
        TaxType nType = (value != null && !"".equals(value) ? TaxType.valueOf(value) : null);
        getView().setTaxType(nType);

        dispatcher.execute(new GetUserDepartmentAction(),
                CallbackUtils.defaultCallback(
                        new AbstractCallback<GetUserDepartmentResult>() {
                            @Override
                            public void onSuccess(GetUserDepartmentResult result) {
                                if (result == null) {
                                    getProxy().manualRevealFailed();
                                    return;
                                }
                                // Текущее подразделение пользователя
                                userDepartment = result.getDepartment();
                            }
                        }, this).addCallback(new ManualRevealCallback<GetUserDepartmentAction>(this)));
    }
}