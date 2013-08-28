package com.aplana.sbrf.taxaccounting.web.module.departmentconfig.client;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.TaxPeriod;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.web.main.api.client.RevealContentTypeHolder;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.MessageEvent;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared.GetReportPeriods;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared.GetReportPeriodsResult;
import com.aplana.sbrf.taxaccounting.web.module.departmentconfig.shared.*;
import com.aplana.sbrf.taxaccounting.web.widget.reportperiodpicker.ReportPeriodSelectHandler;
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

    public interface MyView extends View, HasUiHandlers<DepartmentConfigUiHandlers>, ReportPeriodSelectHandler {
        /**
         * Флаг роли (Контролер/Контролер УНП)
         * @param isUnp
         */
        void setUnpFlag(boolean isUnp);

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
         * Установка доступных налоговых периодов
         * @param taxPeriods
         */
        void setTaxPeriods(List<TaxPeriod> taxPeriods);

        /**
         * Установка доступных налоговых периодов
         * @param reportPeriods
         */
        void setReportPeriods(List<ReportPeriod> reportPeriods);

        /**
         * Установка параметров подразделения
         * @param combinedDepartmentParam
         */
        void setDepartmentCombined(DepartmentCombined combinedDepartmentParam);

        /**
         * Установка доступных типов налога
         * @param types
         */
        void setTaxTypes(List<TaxType> types);

        /**
         * Перезагрузка параметров подразделения
         */
        void reloadDepartmentParams();

        /**
         * Очистка формы
         */
        void clear();

        /**
         * Обновление списка налоговых периодов
         */
        void reloadTaxPeriods();

        /**
         * Обновление дерева подразделений
         */
        void reloadDepartments();

        /**
         * Установка выбранного отчетного периода
         * @param reportPeriod
         */
        void setReportPeriod(ReportPeriod reportPeriod);

        /**
         * Установка разыменованных значений для справочников
         */
        void setDereferenceValue(Map<Long, String> rbTextValues);

        /**
         * Признак открытости выбранного отчетного периода
         * @param reportPeriodActive
         */
        void setReportPeriodActive(boolean reportPeriodActive);
    }

    @Inject
    public DepartmentConfigPresenter(final EventBus eventBus, final MyView view, final MyProxy proxy,
                                     DispatchAsync dispatcher, PlaceManager placeManager) {
        super(eventBus, view, proxy, RevealContentTypeHolder.getMainContent());
        this.dispatcher = dispatcher;
        getView().setUiHandlers(this);
    }

    @Override
    public void save(DepartmentCombined combinedDepartmentParam, ReportPeriod period) {
        if (combinedDepartmentParam == null || period == null) {
            return;
        }

        SaveDepartmentCombinedAction action = new SaveDepartmentCombinedAction();
        action.setDepartmentCombined(combinedDepartmentParam);
        action.setPeriod(period);
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
    public void reloadTaxPeriods(TaxType taxType, Integer departmentId) {
        if (taxType == null || departmentId == null) {
            return;
        }

        GetTaxPeriodWDAction action = new GetTaxPeriodWDAction();
        action.setDepartmentId(departmentId);
        action.setTaxType(taxType);
        dispatcher.execute(action, CallbackUtils
                .defaultCallback(new AbstractCallback<GetTaxPeriodWDResult>() {
                    @Override
                    public void onSuccess(GetTaxPeriodWDResult result) {
                        getView().setTaxPeriods(result.getTaxPeriods());
                        getView().setReportPeriod(result.getLastReportPeriod());
                        getView().reloadDepartmentParams();
                    }
                }, this));
    }

    @Override
    public void reloadDepartments(TaxType taxType) {
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
                                // Выбирается подразделение пользователя
                                getView().setDepartment(userDepartment);
                                // Обновление налоговых периодов
                                getView().reloadTaxPeriods();
                            }
                        }, this).addCallback(new ManualRevealCallback<GetDepartmentTreeDataAction>(this)));
    }

    @Override
    public void onTaxPeriodSelected(TaxPeriod taxPeriod, Integer departmentId) {
        if (taxPeriod == null || departmentId == null) {
            return;
        }

        GetReportPeriods action = new GetReportPeriods();
        action.setTaxPeriod(taxPeriod);
        action.setDepartmentId(departmentId);
        dispatcher.execute(action, CallbackUtils
                .defaultCallback(new AbstractCallback<GetReportPeriodsResult>() {
                    @Override
                    public void onSuccess(GetReportPeriodsResult result) {
                        getView().setReportPeriods(result.getReportPeriods());
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

        dispatcher.execute(new GetUserDepartmentAction(),
                CallbackUtils.defaultCallback(
                        new AbstractCallback<GetUserDepartmentResult>() {
                            @Override
                            public void onSuccess(GetUserDepartmentResult result) {
                                if (result == null || result.getControlUNP() == null) {
                                    getProxy().manualRevealFailed();
                                    return;
                                }
                                // Признак УНП
                                getView().setUnpFlag(result.getControlUNP());
                                // Текущее подразделение пользователя
                                userDepartment = result.getDepartment();
                                // Доступные типы налогов
                                getView().setTaxTypes(Arrays.asList(TaxType.INCOME, TaxType.TRANSPORT, TaxType.DEAL));
                            }
                        }, this).addCallback(new ManualRevealCallback<GetUserDepartmentAction>(this)));
    }

    // TODO Unlock. Реализовать механизм блокировок.
}