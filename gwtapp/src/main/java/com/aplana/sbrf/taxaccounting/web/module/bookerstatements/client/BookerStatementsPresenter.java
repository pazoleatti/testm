package com.aplana.sbrf.taxaccounting.web.module.bookerstatements.client;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.web.main.api.client.RevealContentTypeHolder;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.module.bookerstatements.client.create.CreateBookerStatementsPresenter;
import com.aplana.sbrf.taxaccounting.web.module.bookerstatements.shared.*;
import com.aplana.sbrf.taxaccounting.web.module.bookerstatementsdata.client.BookerStatementsDataTokens;
import com.aplana.sbrf.taxaccounting.web.module.formdata.client.FormDataPresenter;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.History;
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
 * Presenter для Формы фильтрации бухгалтерской отчётности
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

    private final TableDataProvider dataProvider = new TableDataProvider();

    private boolean isFilter = false;

    private final CreateBookerStatementsPresenter dialogPresenter;

    private BookerStatementsFilter filter;

    private Map<Integer, String> lstHistory = new HashMap<Integer, String>();

    public interface MyView extends View, HasUiHandlers<BookerStatementsUiHandlers> {

        /**
         * Инициализация значений элементов фильтра.
         */
        void initFilter();

        /**
         * Данные справочника "Подразделения"
         *
         * @param departments         Список подразделений дерева справочника
         * @param availableDepartment Список подразделений, которые доступны для выбора
         */
        void setDepartments(List<Department> departments, Set<Integer> availableDepartment);

        /**
         * Установка выбранных подразделении
         *
         * @param departments
         */
        void setDepartment(List<Integer> departments);

        /**
         * Установка доступных налоговых периодов
         *
         * @param reportPeriods
         */
        void setReportPeriods(List<ReportPeriod> reportPeriods);

        void setReportPeriod(List<Integer> reportPeriods);

        /**
         * Установка списка достуных видов бухгалтерской отчётности
         *
         * @param bookerReportTypes
         */
        void setBookerReportTypes(List<BookerStatementsType> bookerReportTypes);

        void setBookerReportType(BookerStatementsType bookerReportType);
        /**
         * Получает выбранные подразделения
         */
        List<Integer> getDepartments();

        /**
         * Получает выбранные отчетные периоды
         */
        List<Integer> getReportPeriods();

        /**
         * Получает тип
         *
         * @return null в случае если ничего не выбранно
         */
        BookerStatementsType getType();

        void setTableData(int start, int totalCount, List<BookerStatementsSearchResultItem> dataRows, Map<Integer, String> departmentFullNames);

        int getPageSize();

        void assignDataProvider(int pageSize, AbstractDataProvider<BookerStatementsSearchResultItem> dataProvider);

        void updateTable();

        BookerStatementsSearchOrdering getSearchOrdering();

        boolean isAscSorting();
    }

    @Inject
    public BookerStatementsPresenter(final EventBus eventBus, final MyView view, final MyProxy proxy,
                                     DispatchAsync dispatcher, PlaceManager placeManager, CreateBookerStatementsPresenter dialogPresenter) {
        super(eventBus, view, proxy, RevealContentTypeHolder.getMainContent());
        this.dispatcher = dispatcher;
        this.dialogPresenter = dialogPresenter;
        getView().setUiHandlers(this);
        getView().assignDataProvider(getView().getPageSize(), dataProvider);
        History.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                lstHistory.put(0, lstHistory.get(1));
                lstHistory.put(1, event.getValue());
            }
        });
    }

    @Override
    public void onSearch() {
        BookerStatementsFilter filter = new BookerStatementsFilter();
        filter.setBookerStatementsType(getView().getType());
        filter.setDepartmentIds(getView().getDepartments());
        filter.setReportPeriodIds(getView().getReportPeriods());

        getView().updateTable();
    }

    @Override
    public boolean useManualReveal() {
        return true;
    }

    @Override
    public void prepareFromRequest(PlaceRequest request) {
        super.prepareFromRequest(request);
        String url = BookerStatementsDataTokens.bookerStatements + ";";
        if ((lstHistory.get(0) == null || !lstHistory.get(0).startsWith(url)) &&
                (lstHistory.get(1) == null || !lstHistory.get(1).startsWith(url))) {
            filter = null;
        }
        dispatcher.execute(new GetBSOpenListAction(),
                CallbackUtils.defaultCallback(
                        new AbstractCallback<GetBSOpenListResult>() {
                            @Override
                            public void onSuccess(GetBSOpenListResult result) {
                                if (result == null || result.getControlUNP() == null) {
                                    getProxy().manualRevealFailed();
                                    return;
                                }

                                getView().initFilter();

                                // Список подразделений для справочника
                                getView().setDepartments(result.getDepartments(), result.getAvailableDepartments());
                                // Список достуных видов бухгалтерской отчётности
                                getView().setBookerReportTypes(result.getBookerReportTypes());
                                getView().setReportPeriods(result.getReportPeriods());

                                if (filter == null) {
                                    filter = new BookerStatementsFilter();
                                    // Текущее подразделение пользователя
                                    getView().setDepartment(Arrays.asList(result.getDepartment().getId()));
                                    getView().setBookerReportType(BookerStatementsType.INCOME101);
                                } else {
                                    getView().setDepartment(filter.getDepartmentIds());
                                    getView().setReportPeriod(filter.getReportPeriodIds());
                                    getView().setBookerReportType(filter.getBookerStatementsType());
                                }
                                onSearch();
                            }
                        }, this).addCallback(new ManualRevealCallback<GetBSOpenListAction>(this)));
    }

    private class TableDataProvider extends AsyncDataProvider<BookerStatementsSearchResultItem> {
        @Override
        protected void onRangeChanged(HasData<BookerStatementsSearchResultItem> display) {
            final Range range = display.getVisibleRange();

            if (filter == null) {
                return;
            }
            filter.setDepartmentIds(getView().getDepartments());
            filter.setReportPeriodIds(getView().getReportPeriods());
            filter.setBookerStatementsType(getView().getType());
            filter.setStartIndex(range.getStart());
            filter.setCountOfRecords(range.getLength());
            filter.setAscSorting(getView().isAscSorting());
            filter.setSearchOrdering(getView().getSearchOrdering());

            GetBookerStatementsListAction action = new GetBookerStatementsListAction();
            action.setFilter(filter);
            dispatcher.execute(action, CallbackUtils.defaultCallback(new AbstractCallback<GetBookerStatementsListResult>() {
                @Override
                public void onSuccess(GetBookerStatementsListResult result) {
                    getView().setTableData(range.getStart(),
                            result.getTotalCount(), result.getDataRows(), result.getDepartmentFullNames());
                }
            }, BookerStatementsPresenter.this));
        }
    }

    @Override
    public void onSortingChanged(){
        getView().updateTable();
    }


    @Override
    public void onCreateClicked() {
        dialogPresenter.initAndShowDialog(this);
    }
}