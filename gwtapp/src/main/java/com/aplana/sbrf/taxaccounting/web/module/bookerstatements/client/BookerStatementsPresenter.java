package com.aplana.sbrf.taxaccounting.web.module.bookerstatements.client;

import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.gwt.client.dialog.DialogHandler;
import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.web.main.api.client.RevealContentTypeHolder;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.MessageEvent;
import com.aplana.sbrf.taxaccounting.web.module.bookerstatements.shared.*;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.RefBookColumn;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.RefBookDataRow;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.view.client.AbstractDataProvider;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;
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
    private boolean searchEnabled = false;

    private final TableDataProvider dataProvider = new TableDataProvider();

    public interface MyView extends View, HasUiHandlers<BookerStatementsUiHandlers> {

        /**
         * Инициализация.
         * Требуется для обнуления параметров загрузки после перехода по главному меню
         */
        void init();

        /**
         * Данные справочника "Подразделения"
         *
         * @param departments         Список подразделений дерева справочника
         * @param availableDepartment Список подразделений, которые доступны для выбора
         */
        void setDepartments(List<Department> departments, Set<Integer> availableDepartment);

        /**
         * Установка выбранного подразделения
         *
         * @param department
         */
        void setDepartment(Department department);

        /**
         * Установка доступных налоговых периодов
         *
         * @param reportPeriods
         */
        void setReportPeriods(List<ReportPeriod> reportPeriods);

        /**
         * Установка списка достуных видов бухгалтерской отчётности
         *
         * @param bookerReportTypes
         */
        void setBookerReportTypes(Map<String, String> bookerReportTypes);

        /**
         * Получает выбранное подразделение
         *
         * @return null в случае если ничего не выбранно
         */
        Pair<Integer, String> getDepartment();

        /**
         * Получает отчетный период
         *
         * @return null в случае если ничего не выбранно
         */
        Pair<Integer, String> getReportPeriod();

        /**
         * Получает тип
         *
         * @return null в случае если ничего не выбранно
         */
        Pair<Integer, String> getType();

        void addAccImportValueChangeHandler(ValueChangeHandler<String> valueChangeHandler);

        void setTableData(int start, int totalCount, List<RefBookDataRow> dataRows);

        int getPageSize();

        void assignDataProvider(int pageSize, AbstractDataProvider<RefBookDataRow> dataProvider);

        Date getReportPeriodEndDate();

        void updateTable();
        void setTableColumns(final List<RefBookColumn> columns);
    }

    @Inject
    public BookerStatementsPresenter(final EventBus eventBus, final MyView view, final MyProxy proxy,
                                     DispatchAsync dispatcher, PlaceManager placeManager) {
        super(eventBus, view, proxy, RevealContentTypeHolder.getMainContent());
        this.dispatcher = dispatcher;
        getView().setUiHandlers(this);
        getView().assignDataProvider(getView().getPageSize(), dataProvider);
    }

    @Override
    public void importData(String uuid) {
        if (isFilterFilled()) {
            ImportAction importAction = new ImportAction();
            importAction.setUuid(uuid);
            importAction.setDepartmentId(getView().getDepartment().getFirst());
            importAction.setReportPeriodId(getView().getReportPeriod().getFirst());
            importAction.setTypeId(getView().getType().getFirst());
            dispatcher.execute(importAction, CallbackUtils.defaultCallback(
                    new AbstractCallback<ImportResult>() {
                        @Override
                        public void onSuccess(ImportResult importResult) {
                            MessageEvent.fire(BookerStatementsPresenter.this, "Загрузка бух отчетности выполнена успешно");
                        }
                    }, this));
        }
    }

    @Override
    public void onSearch() {
        searchEnabled = true;
        getView().updateTable();
    }

    @Override
    public void onDelete() {
        if (isFilterFilled()) {
            GetBookerStatementsAction action = new GetBookerStatementsAction();
            action.setDepartmentId(getView().getDepartment().getFirst());
            action.setStatementsKind(getView().getType().getFirst());
            action.setVersion(getView().getReportPeriodEndDate());
            action.setNeedOnlyIds(true);
            dispatcher.execute(action, CallbackUtils.defaultCallback(new AbstractCallback<GetBookerStatementsResult>() {
                @Override
                public void onSuccess(final GetBookerStatementsResult result) {
                    if (result.getUniqueRecordIds() != null && !result.getUniqueRecordIds().isEmpty()) {
                        String departmentName = getView().getDepartment().getSecond().toString();
                        if (departmentName.toCharArray()[departmentName.length()-1] != '"'){
                            departmentName = departmentName + "\"";
                        }
                        Dialog.confirmMessage("Вы уверены, что хотите удалить данные бухгалтерской отчётности \"" +
                                getView().getType().getSecond() +"\"" +
                                " для подразделения \"" + departmentName +
                                " в периоде \"" + getView().getReportPeriod().getSecond() + "\"?",
                                new DialogHandler() {
                                    @Override
                                    public void yes() {
                                        DeleteBookerStatementsAction action = new DeleteBookerStatementsAction();
                                        action.setStatementsKind(getView().getType().getFirst());
                                        action.setUniqueRecordIds(result.getUniqueRecordIds());
                                        dispatcher.execute(action, CallbackUtils
                                                .defaultCallback(new AbstractCallback<DeleteBookerStatementsResult>() {
                                                    @Override
                                                    public void onSuccess(DeleteBookerStatementsResult result) {
                                                        Dialog.infoMessage("Удаление бух отчетности выполнено успешно.");
                                                    }
                                                }, BookerStatementsPresenter.this)
                                        );
                                        Dialog.hideMessage();
                                    }

                                    @Override
                                    public void no() {
                                        Dialog.hideMessage();
                                    }

                                    @Override
                                    public void close() {
                                        Dialog.hideMessage();
                                    }
                                });
                    } else {
                        Dialog.errorMessage("Данные бухгалтерской отчётности (форма " + getView().getType().getSecond() +
                                ") для подразделения " + getView().getDepartment().getSecond() +
                                " в периоде " + getView().getReportPeriod().getSecond() + " не существуют!");
                    }
                }
            }, BookerStatementsPresenter.this));
        }
    }

    /**
     * Проверка заполненности данных фильтра
     */
    private boolean isFilterFilled() {
        if (getView().getReportPeriod() == null) {
            Dialog.errorMessage("Не задано значение отчетного периода!");
            return false;
        }
        if (getView().getDepartment() == null) {
            Dialog.errorMessage("Не задано подразделение!");
            return false;
        }
        if (getView().getType() == null) {
            Dialog.errorMessage("Не задан вид бухгалтерской отчетности!");
            return false;
        }
        return true;
    }

    @Override
    public boolean useManualReveal() {
        return true;
    }

    @Override
    public void prepareFromRequest(PlaceRequest request) {
        super.prepareFromRequest(request);
        getView().init();

        dispatcher.execute(new GetBSOpenDataAction(),
                CallbackUtils.defaultCallback(
                        new AbstractCallback<GetBSOpenDataResult>() {
                            @Override
                            public void onSuccess(GetBSOpenDataResult result) {
                                if (result == null || result.getControlUNP() == null) {
                                    getProxy().manualRevealFailed();
                                    return;
                                }

                                // Список подразделений для справочника
                                getView().setDepartments(result.getDepartments(), result.getAvailableDepartments());
                                // Текущее подразделение пользователя
                                getView().setDepartment(result.getDepartment());
                                // Список достуных видов бухгалтерской отчётности
                                getView().setBookerReportTypes(new HashMap<String, String>() {{
                                    put("101", "Форма 101");
                                    put("102", "Форма 102");
                                }});
                                getView().setReportPeriods(result.getReportPeriods());
                            }
                        }, this).addCallback(new ManualRevealCallback<GetBSOpenDataAction>(this)));
    }

    @Override
    protected void onBind() {
        super.onBind();
        ValueChangeHandler<String> accImportValueChangeHandler = new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                ImportAction importAction = new ImportAction();
                importAction.setUuid(event.getValue());
                importAction.setDepartmentId(getView().getDepartment().getFirst());
                importAction.setReportPeriodId(getView().getReportPeriod().getFirst());
                importAction.setTypeId(getView().getType().getFirst());
                dispatcher.execute(importAction, CallbackUtils.defaultCallback(
                        new AbstractCallback<ImportResult>() {
                            @Override
                            public void onSuccess(ImportResult importResult) {
                                Dialog.infoMessage("Загрузка бух отчетности выполнена успешно.");
                            }
                        }, BookerStatementsPresenter.this));
            }
        };
        getView().addAccImportValueChangeHandler(accImportValueChangeHandler);
    }



    private class TableDataProvider extends AsyncDataProvider<RefBookDataRow> {
        @Override
        protected void onRangeChanged(HasData<RefBookDataRow> display) {
            if (searchEnabled && isFilterFilled()) {
                final Range range = display.getVisibleRange();
                GetBookerStatementsAction action = new GetBookerStatementsAction();
                action.setDepartmentId(getView().getDepartment().getFirst());
                action.setStatementsKind(getView().getType().getFirst());
                action.setVersion(getView().getReportPeriodEndDate());
                action.setPagingParams(new PagingParams(range.getStart() + 1, range.getLength()));
                action.setNeedOnlyIds(false);
                dispatcher.execute(action, CallbackUtils.defaultCallback(new AbstractCallback<GetBookerStatementsResult>() {
                    @Override
                    public void onSuccess(GetBookerStatementsResult result) {
                        if (result.getTotalCount() != 0) {
                            getView().setTableColumns(result.getColumns());
                            getView().setTableData(range.getStart(),
                                    result.getTotalCount(), result.getDataRows());
                        } else {
                            Dialog.errorMessage("Невозможно отобразить бухгалтерскую отчетность", "Для выбранного подразделения, в указанном периоде отсутствуют данные по бухгалтерской отчётности вида: <вид бухгалтерской отчётности>!");
                        }
                    }
                }, BookerStatementsPresenter.this));
            }
        }
    }
}