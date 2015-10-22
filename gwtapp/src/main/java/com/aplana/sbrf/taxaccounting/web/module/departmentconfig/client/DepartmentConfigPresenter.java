package com.aplana.sbrf.taxaccounting.web.module.departmentconfig.client;

import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.gwt.client.dialog.DialogHandler;
import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.web.main.api.client.RevealContentTypeHolder;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogAddEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogCleanEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogShowEvent;
import com.aplana.sbrf.taxaccounting.web.module.departmentconfig.shared.*;
import com.aplana.sbrf.taxaccounting.web.module.departmentconfigproperty.shared.AddLogAction;
import com.aplana.sbrf.taxaccounting.web.module.departmentconfigproperty.shared.AddLogResult;
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

    private static final String EDIT_FOUND_TEXT = "В периоде %s найдены экземпляры налоговых форм/деклараций, " +
            "которые используют предыдущие значения формы настроек подразделения. Подтверждаете изменение настроек подразделения?";
    private static final String EDIT_FOUND_TEXT_D = "В периоде %s найдены экземпляры форм/уведомлений, " +
            "которые используют предыдущие значения формы настроек подразделения. Подтверждаете изменение настроек подразделения?";

    private final DispatchAsync dispatcher;

    private Department userDepartment;
    private boolean isControlUnp;

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

        /**
         * Получить идентификатор текущего периода
         * @return
         */
        Integer getCurrentReportPeriodId();

        /**
         * Получить идентификатор текущего подразделения
         * @return
         */
        Integer getCurrentDepartmentId();

        void removeResizeHandler();

        void update();

        /**
         * Устанавливаем всем справочникам на форме "ограничивающий период"
         * @param startDate
         * @param endDate
         */
        void setRefBookPeriod(Date startDate, Date endDate);
    }

    @Inject
    public DepartmentConfigPresenter(final EventBus eventBus, final MyView view, final MyProxy proxy,
                                     DispatchAsync dispatcher, PlaceManager placeManager) {
        super(eventBus, view, proxy, RevealContentTypeHolder.getMainContent());
        this.dispatcher = dispatcher;
        getView().setUiHandlers(this);
    }

    @Override
    public void save(final DepartmentCombined combinedDepartmentParam, final Integer period, final Integer department) {
        final String[] uuid = {""};

        if (combinedDepartmentParam == null || department == null || period == null) {
            return;
        }

        LogCleanEvent.fire(DepartmentConfigPresenter.this);

        final GetCheckDeclarationAction action = new GetCheckDeclarationAction();
        action.setReportPeriodId(period);
        action.setDepartment(department);
        action.setTaxType(getView().getTaxType());
        dispatcher.execute(action,
                CallbackUtils.defaultCallback(
                        new AbstractCallback<GetCheckDeclarationResult>() {
                            void save(String uuid) {
                                SaveDepartmentCombinedAction action = new SaveDepartmentCombinedAction();
                                action.setDepartmentCombined(combinedDepartmentParam);
                                action.setReportPeriodId(period);
                                action.setTaxType(getView().getTaxType());
                                action.setDepartment(department);
                                action.setOldUUID(uuid);
                                dispatcher.execute(action, CallbackUtils
                                        .defaultCallback(new AbstractCallback<SaveDepartmentCombinedResult>() {
                                            @Override
                                            public void onSuccess(SaveDepartmentCombinedResult result) {
                                                LogAddEvent.fire(DepartmentConfigPresenter.this, result.getUuid());
                                                if (!result.isHasError()) {
                                                    getView().reloadDepartmentParams();
                                                }
                                            }
                                        }, DepartmentConfigPresenter.this));
                            }

                            @Override
                            public void onSuccess(final GetCheckDeclarationResult result) {
                                isControlUnp = result.isControlUnp();
                                uuid[0] = result.getUuid();
                                if (uuid[0] != null) {
                                    LogAddEvent.fire(DepartmentConfigPresenter.this, uuid[0]);
                                }
                                if (result.isDeclarationFormFound()) {
                                    Dialog.confirmMessage(((getView().getTaxType().equals(TaxType.DEAL) ? EDIT_FOUND_TEXT_D : EDIT_FOUND_TEXT)).replace("%s", result.getReportPeriodName()),
                                            new DialogHandler() {
                                                @Override
                                                public void yes() {
                                                    super.yes();

                                                    AddLogAction addLogAction = new AddLogAction();
                                                    addLogAction.setOldUUID(uuid[0]);
                                                    addLogAction.setMessages(Arrays.asList(new LogEntry(LogLevel.WARNING,
                                                            "Для актуализации данных в найденных экземплярах налоговых форм/деклараций их необходимо рассчитать/обновить")));
                                                    dispatcher.execute(addLogAction, CallbackUtils
                                                            .defaultCallback(new AbstractCallback<AddLogResult>() {
                                                                @Override
                                                                public void onSuccess(AddLogResult result) {
                                                                    save(result.getUuid());
                                                                }
                                                            }, DepartmentConfigPresenter.this));
                                                }
                                            });
                                } else {
                                    save(uuid[0]);
                                }
                            }
                        }, this));
    }

    @Override
    public void delete(final DepartmentCombined combinedDepartmentParam, final Integer period, final Integer department) {
        if (combinedDepartmentParam == null || department == null || period == null) {
            return;
        }
        LogCleanEvent.fire(DepartmentConfigPresenter.this);
        final String[] uuid = {""};

        final GetCheckDeclarationAction action = new GetCheckDeclarationAction();
        action.setReportPeriodId(period);
        action.setDepartment(department);
        action.setTaxType(getView().getTaxType());
        action.setFatal(true);
        dispatcher.execute(action,
                CallbackUtils.defaultCallback(
                        new AbstractCallback<GetCheckDeclarationResult>() {
                            void delete() {
                                CheckSettingExistAction settingExistAction = new CheckSettingExistAction();
                                settingExistAction.setDepartmentId(department.longValue());
                                settingExistAction.setReportPeriodId(period);
                                settingExistAction.setTaxType(getView().getTaxType());
                                dispatcher.execute(settingExistAction, CallbackUtils
                                        .defaultCallback(new AbstractCallback<CheckSettingExistResult>() {
                                            @Override
                                            public void onSuccess(CheckSettingExistResult result) {
                                                if (result.isSettingsExist()) {
                                                    Dialog.confirmMessage("Подтверждение операции", "Настройки подразделения будут удалены, начиная с указанного периода. Продолжить?", new DialogHandler() {
                                                        @Override
                                                        public void yes() {
                                                            super.yes();
                                                            LogCleanEvent.fire(DepartmentConfigPresenter.this);

                                                            DeleteDepartmentCombinedAction action = new DeleteDepartmentCombinedAction();
                                                            action.setDepartmentCombined(combinedDepartmentParam);
                                                            action.setReportPeriodId(period);
                                                            action.setTaxType(getView().getTaxType());
                                                            action.setDepartment(department);
                                                            action.setOldUUID(uuid[0]);
                                                            dispatcher.execute(action, CallbackUtils
                                                                    .defaultCallback(new AbstractCallback<DeleteDepartmentCombinedResult>() {
                                                                        @Override
                                                                        public void onSuccess(DeleteDepartmentCombinedResult result) {
                                                                            LogAddEvent.fire(DepartmentConfigPresenter.this, result.getUuid());
                                                                            getView().reloadDepartmentParams();
                                                                        }
                                                                    }, DepartmentConfigPresenter.this));
                                                            getView().update();
                                                        }
                                                    });
                                                } else {
                                                    Dialog.errorMessage("Удаление настроек не выполнено", "Удаление настроек выбранного " +
                                                            "подразделения и периода не может быть выполнено, т.к. данная версия настроек не создана");
                                                }
                                            }
                                        }, DepartmentConfigPresenter.this));
                            }

                            @Override
                            public void onSuccess(final GetCheckDeclarationResult result) {
                                isControlUnp = result.isControlUnp();
                                delete();
                            }
                        }, this));
    }

    @Override
    public void edit(Integer period, Integer department) {
        if (department == null || period == null) {
            return;
        }
        getView().setEditMode(true);
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

                                reloadDepartmentParams(getView().getCurrentDepartmentId(), getView().getTaxType(), getView().getCurrentReportPeriodId());
                            }
                        }, this).addCallback(new ManualRevealCallback<GetDepartmentTreeDataAction>(this)));
    }

    @Override
    public boolean isControlUnp() {
        return isControlUnp;
    }

    @Override
    public void getRefBookPeriod(Integer currentReportPeriodId, Integer currentDepartmentId) {
        GetRefBookPeriodAction action = new GetRefBookPeriodAction();
        action.setDepartmentId(currentDepartmentId);
        action.setReportPeriodId(currentReportPeriodId);
        action.setTaxType(getView().getTaxType());
        dispatcher.execute(action,
                CallbackUtils.defaultCallback(
                        new AbstractCallback<GetRefBookPeriodResult>() {
                            @Override
                            public void onSuccess(GetRefBookPeriodResult result) {
                                getView().setRefBookPeriod(result.getStartDate(), result.getEndDate());
                            }
                        }, this).addCallback(new ManualRevealCallback<GetUserDepartmentAction>(this)));
    }

    @Override
    public boolean useManualReveal() {
        return true;
    }

    @Override
    public void prepareFromRequest(PlaceRequest request) {
        super.prepareFromRequest(request);
        LogCleanEvent.fire(this);
        LogShowEvent.fire(this, false);

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

    @Override
    protected void onHide() {
        super.onHide();
        getView().removeResizeHandler();
    }
}