package com.aplana.sbrf.taxaccounting.web.module.departmentconfig.client;

import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.gwt.client.dialog.DialogHandler;
import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.web.main.api.client.RevealContentTypeHolder;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.MessageEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogAddEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogCleanEvent;
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

    private static final String SAVE_FOUND_TEXT = "В Системе созданы формы/декларации использующие старую версию Настроек. Для вступления изменений в силу каждую налоговую форму/декларацию нужно обновить вручную.";
    private static final String SAVE_FOUND_TEXT_D = "В Системе созданы формы/уведомления использующие старую версию Настроек. Для вступления изменений в силу каждую форму/уведомление нужно обновить вручную.";

    private static final String EDIT_FOUND_TEXT = "Настройки используются для налоговых форм/деклараций. Желаете внести изменения в Настройки?";
    private static final String EDIT_FOUND_TEXT_D = "Настройки используются для форм/уведомлений. Желаете внести изменения в Настройки?";

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
         * Исходное состояние формы
         */
        void init();

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

        /**
         * Изменение режима редактирования формы
         */
        void setEditMode(boolean isEditMode);

        /**
         * Обновление видимости дял кнопки "Редактировать"
         */
        void updateVisibleEditButton();
    }

    @Inject
    public DepartmentConfigPresenter(final EventBus eventBus, final MyView view, final MyProxy proxy,
                                     DispatchAsync dispatcher, PlaceManager placeManager) {
        super(eventBus, view, proxy, RevealContentTypeHolder.getMainContent());
        this.dispatcher = dispatcher;
        getView().setUiHandlers(this);
    }

    @Override
    public void save(DepartmentCombined combinedDepartmentParam, Integer period, Integer department) {
        if (combinedDepartmentParam == null || department == null || period == null) {
            return;
        }

        LogCleanEvent.fire(DepartmentConfigPresenter.this);

        SaveDepartmentCombinedAction action = new SaveDepartmentCombinedAction();
        action.setDepartmentCombined(combinedDepartmentParam);
        action.setReportPeriodId(period);
        action.setTaxType(getView().getTaxType());
        action.setDepartment(department);
        dispatcher.execute(action, CallbackUtils
                .defaultCallback(new AbstractCallback<SaveDepartmentCombinedResult>() {
                    @Override
                    public void onSuccess(SaveDepartmentCombinedResult result) {
                        LogAddEvent.fire(DepartmentConfigPresenter.this, result.getUuid());
                        if (!result.isHasError()) {
                            //MessageEvent.fire(DepartmentConfigPresenter.this, "Параметры подразделения сохранены");
                            if (result.isDeclarationFormFound()) {
                                Dialog.confirmMessage(getView().getTaxType().equals(TaxType.DEAL) ? SAVE_FOUND_TEXT_D : SAVE_FOUND_TEXT);
                            }
                            getView().reloadDepartmentParams();
                        }
                    }
                }, this));
    }

    @Override
    public void edit(Integer period, Integer department) {
        if (department == null || period == null) {
            return;
        }

        GetCheckDeclarationAction action = new GetCheckDeclarationAction();
        action.setReportPeriodId(period);
        action.setDepartment(department);
        action.setTaxType(getView().getTaxType());
        dispatcher.execute(action,
                CallbackUtils.defaultCallback(
                        new AbstractCallback<GetCheckDeclarationResult>() {
                            @Override
                            public void onSuccess(GetCheckDeclarationResult result) {
                                if (result.getUuid() != null) {
                                    LogAddEvent.fire(DepartmentConfigPresenter.this, result.getUuid());
                                }
                                if (result.isDeclarationFormFound()) {
                                    Dialog.confirmMessage(getView().getTaxType().equals(TaxType.DEAL) ? EDIT_FOUND_TEXT_D : EDIT_FOUND_TEXT,
                                            new DialogHandler() {
                                                @Override
                                                public void yes() {
                                                    super.yes();
                                                    getView().setEditMode(true);
                                                }
                                            });
                                } else {
                                    getView().setEditMode(true);
                                }
                            }
                        }, this));
    }

    @Override
    public void clear() {
        getView().clear();
    }

    @Override
    public void reloadDepartmentParams(Integer departmentId, TaxType taxType, Integer reportPeriodId) {
        LogCleanEvent.fire(DepartmentConfigPresenter.this);

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
                        getView().updateVisibleEditButton();
                        result.getRbTextValues();
                        if (result.getUuid() != null) {
                            LogAddEvent.fire(DepartmentConfigPresenter.this, result.getUuid());
                        }
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
                                getView().setReportPeriods(result.getReportPeriods() == null
                                        ? new ArrayList<ReportPeriod>(0) : result.getReportPeriods());

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
        getView().init();

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