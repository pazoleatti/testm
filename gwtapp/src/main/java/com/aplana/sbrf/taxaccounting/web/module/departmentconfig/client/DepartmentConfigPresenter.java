package com.aplana.sbrf.taxaccounting.web.module.departmentconfig.client;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.TaxPeriod;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.web.main.api.client.RevealContentTypeHolder;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.MessageEvent;
import com.aplana.sbrf.taxaccounting.web.module.audit.shared.GetTaxPeriodAction;
import com.aplana.sbrf.taxaccounting.web.module.audit.shared.GetTaxPeriodResult;
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

import java.util.Arrays;
import java.util.List;
import java.util.Set;

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
    }

    @Inject
    public DepartmentConfigPresenter(final EventBus eventBus, final MyView view, final MyProxy proxy,
                                     DispatchAsync dispatcher, PlaceManager placeManager) {
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
                    }
                }, this));
    }

    @Override
    public void reloadTaxPeriods(TaxType taxType) {
        if (taxType == null) {
            return;
        }

        GetTaxPeriodAction action = new GetTaxPeriodAction();
        action.setTaxType(taxType);
        dispatcher.execute(action, CallbackUtils
                .defaultCallback(new AbstractCallback<GetTaxPeriodResult>() {
                    @Override
                    public void onSuccess(GetTaxPeriodResult result) {
                        getView().setTaxPeriods(result.getTaxPeriods());
                        getView().reloadDepartmentParams();
                    }
                }, this));
    }



    @Override
    public void onTaxPeriodSelected(TaxPeriod taxPeriod, Integer departmentId) {
        if (taxPeriod == null || departmentId == null) {
            return;
        }

        GetReportPeriods action = new GetReportPeriods();
        action.setTaxPeriod(taxPeriod);
        action.setDepartamentId(departmentId);
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

        dispatcher.execute(new GetOpenDataAction(),
                CallbackUtils.defaultCallback(
                        new AbstractCallback<GetOpenDataResult>() {
                            @Override
                            public void onSuccess(GetOpenDataResult result) {
                                if (result == null || result.getControlUNP() == null) {
                                    getProxy().manualRevealFailed();
                                    return;
                                }

                                getView().setUnpFlag(result.getControlUNP());

                                // Список подразделений для справочника
                                getView().setDepartments(result.getDepartments(), result.getAvailableDepartments());
                                // Текущее подразделение пользователя
                                getView().setDepartment(result.getDepartment());
                                // Доступные типы налогов
                                getView().setTaxTypes(Arrays.asList(TaxType.INCOME, TaxType.TRANSPORT, TaxType.DEAL));

                            }
                        }, this).addCallback(new ManualRevealCallback<GetOpenDataResult>(this)));
    }

    // TODO Unlock. Реализовать механизм блокировок.
}