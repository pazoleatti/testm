package com.aplana.sbrf.taxaccounting.web.module.bookerstatements.client;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.TaxPeriod;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.web.main.api.client.RevealContentTypeHolder;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared.GetReportPeriods;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared.GetReportPeriodsResult;
import com.aplana.sbrf.taxaccounting.web.module.departmentconfig.shared.GetOpenDataAction;
import com.aplana.sbrf.taxaccounting.web.module.departmentconfig.shared.GetOpenDataResult;
import com.aplana.sbrf.taxaccounting.web.module.departmentconfig.shared.GetTaxPeriodWDAction;
import com.aplana.sbrf.taxaccounting.web.module.departmentconfig.shared.GetTaxPeriodWDResult;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Presenter для формы "Загрузка бухгалтерской отчётности"
 *
 * @author Dmitriy Levykin
 */
public class BookerStatementsPresenter extends Presenter<BookerStatementsPresenter.MyView,
        BookerStatementsPresenter.MyProxy> implements BookerStatementsUiHandlers {

    @ProxyCodeSplit
    @NameToken(BookerStatementsTokens.bookerStatements)
    public interface MyProxy extends ProxyPlace<BookerStatementsPresenter>, Place {
    }

    private final DispatchAsync dispatcher;

    public interface MyView extends View, HasUiHandlers<BookerStatementsUiHandlers> {
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
         * Обновление списка налоговых периодов
         */
        void reloadTaxPeriods();

        /**
         * Установка выбранного отчетного периода
         * @param reportPeriod
         */
        void setReportPeriod(ReportPeriod reportPeriod);

        /**
         * Установка списка достуных видов бухгалтерской отчётности
         * @param bookerReportTypes
         */
        void setBookerReportTypes(Map<String, String> bookerReportTypes);
    }

    @Inject
    public BookerStatementsPresenter(final EventBus eventBus, final MyView view, final MyProxy proxy,
                                     DispatchAsync dispatcher, PlaceManager placeManager) {
        super(eventBus, view, proxy, RevealContentTypeHolder.getMainContent());
        this.dispatcher = dispatcher;
        getView().setUiHandlers(this);
    }

    @Override
    public boolean useManualReveal() {
        return true;
    }

    @Override
    public void reloadTaxPeriods(Integer departmentId) {
        if (departmentId == null) {
            return;
        }

        GetTaxPeriodWDAction action = new GetTaxPeriodWDAction();
        action.setDepartmentId(departmentId);
        action.setTaxType(TaxType.INCOME);
        dispatcher.execute(action, CallbackUtils
                .defaultCallback(new AbstractCallback<GetTaxPeriodWDResult>() {
                    @Override
                    public void onSuccess(GetTaxPeriodWDResult result) {
                        getView().setTaxPeriods(result.getTaxPeriods());
                        // getView().setReportPeriod(result.getLastReportPeriod());
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
                                // Список достуных видов бухгалтерской отчётности
                                getView().setBookerReportTypes(new HashMap<String, String>() {{
                                    put("101", "Форма 101");
                                    put("102", "Форма 102");
                                }});
                            }
                        }, this).addCallback(new ManualRevealCallback<GetOpenDataResult>(this)));
    }
}