package com.aplana.sbrf.taxaccounting.web.module.declarationlist.client.download;

import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.gwt.client.dialog.DialogHandler;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogAddEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogCleanEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogShowEvent;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.client.DeclarationDataTokens;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared.*;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.HasPopupSlot;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Диалог создания декларации
 */
public class DeclarationDownloadReportsPresenter extends PresenterWidget<DeclarationDownloadReportsPresenter.MyView> implements DeclarationDownloadReportsUiHandlers {

    public interface MyView extends PopupView, HasUiHandlers<DeclarationDownloadReportsUiHandlers> {

        void setAcceptableDeclarationTypes(List<DeclarationType> declarationType);
        void setAcceptableReportPeriods(List<ReportPeriod> reportPeriods, ReportPeriod reportPeriod);
        Integer getDefaultReportPeriodId();
        void setAcceptableDepartments(List<Department> departments, Set<Integer> departmentsIds, Integer departmentsId);

        void setSelectedDeclarationType(Integer id);
        void setSelectedReportPeriod(List<Integer> periodIds);
        void setSelectedDepartment(List<Integer> departmentIds);
        void setCorrectionDate(List<DepartmentReportPeriod> departmentReportPeriods);

        Integer getSelectedDeclarationType();
        List<Integer> getSelectedReportPeriod();
        List<Integer> getSelectedDepartment();
        Date getCorrectionDate();
        boolean isCorrection();

        void setTaxType(TaxType taxType);

        void init();

        void updateEnabled();
    }

    private DispatchAsync dispatcher;
    private PlaceManager placeManager;

    private TaxType taxType;

    @Inject
    public DeclarationDownloadReportsPresenter(final EventBus eventBus, final MyView view,
                                               DispatchAsync dispatcher, PlaceManager placeManager) {
        super(eventBus, view);
        this.dispatcher = dispatcher;
        this.placeManager = placeManager;
        getView().setUiHandlers(this);
    }

    @Override
    protected void onHide() {
        clearValues();
        getView().hide();
    }

    @Override
    public void onContinue() {
        final DeclarationDataFilter filter = new DeclarationDataFilter();
        filter.setDeclarationTypeIds(Arrays.asList(getView().getSelectedDeclarationType().longValue()));
        filter.setDepartmentIds(getView().getSelectedDepartment());
        filter.setReportPeriodIds(getView().getSelectedReportPeriod());

        if(isFilterDataCorrect(filter)){
            LogCleanEvent.fire(this);
            LogShowEvent.fire(this, false);
            onCreateForms(false);
        }
    }

    private void onCreateForms(final boolean force) {
        CreateReportsDeclarationAction action = new CreateReportsDeclarationAction();
        action.setDeclarationTypeId(getView().getSelectedDeclarationType().intValue());
        action.setDepartmentId(getView().getSelectedDepartment().iterator().next());
        action.setReportPeriodId(getView().getSelectedReportPeriod().iterator().next());
        action.setTaxType(taxType);
        action.setCorrectionDate(getView().getCorrectionDate());
        dispatcher.execute(action, CallbackUtils
                .defaultCallback(new AbstractCallback<CreateReportsDeclarationResult>() {
                    @Override
                    public void onSuccess(CreateReportsDeclarationResult result) {
                        LogCleanEvent.fire(DeclarationDownloadReportsPresenter.this);
                        LogShowEvent.fire(DeclarationDownloadReportsPresenter.this, false);
                        if (!result.isStatus()) {
                            Dialog.confirmMessage(result.getRestartMsg(), new DialogHandler() {
                                @Override
                                public void yes() {
                                    onCreateForms(true);
                                }
                            });
                        } else {
                            onHide();
                            LogAddEvent.fire(DeclarationDownloadReportsPresenter.this, result.getUuid());
                        }
                    }
                }, DeclarationDownloadReportsPresenter.this));
    }

    @Override
    public void onDepartmentChange() {
        if (getView().getSelectedDepartment().isEmpty() || getView().getSelectedReportPeriod().isEmpty()) {
            return;
        }
        GetDeclarationTypeAction action = new GetDeclarationTypeAction();
        action.setTaxType(taxType);
        action.setDepartmentId(getView().getSelectedDepartment().get(0));
        action.setDeclarationFormKind(DeclarationFormKind.REPORTS);
        action.setReportPeriod(getView().getSelectedReportPeriod().get(0));

        dispatcher.execute(action, CallbackUtils.defaultCallback(new AbstractCallback<GetDeclarationTypeResult>() {
            @Override
            public void onSuccess(GetDeclarationTypeResult result) {
                getView().setAcceptableDeclarationTypes(result.getDeclarationTypes());
                getView().updateEnabled();
            }
        }, this) );
    }

    private boolean isFilterDataCorrect(DeclarationDataFilter filter){
        if ((filter.getReportPeriodIds() == null || filter.getReportPeriodIds().isEmpty())
                || (filter.getDepartmentIds() == null || filter.getDepartmentIds().isEmpty())
                || (filter.getDeclarationTypeIds() == null)
        ){
            String title = "Выгрузка отчетности";
            String msg = "Заполнены не все параметры отчетности";
            Dialog.errorMessage(title, msg);
            return false;
        }
        return true;
    }

    private void clearValues(){
        getView().setSelectedDeclarationType(null);
        getView().setSelectedReportPeriod(null);
        getView().setSelectedDepartment(null);
    }

    public void initAndShowDialog(final DeclarationDataFilter dataFilter, final HasPopupSlot popupSlot){
        this.taxType = dataFilter.getTaxType();
        getView().setTaxType(this.taxType);
        GetReportPeriodsAction action = new GetReportPeriodsAction();
        action.setTaxType(dataFilter.getTaxType());
        action.setReportPeriodId(getView().getDefaultReportPeriodId());
        action.setDownloadReports(true);
        getView().init();
        dispatcher.execute(action, CallbackUtils.defaultCallback(new AbstractCallback<GetReportPeriodsResult>() {
            @Override
            public void onSuccess(GetReportPeriodsResult result) {
                getView().setAcceptableReportPeriods(result.getReportPeriods(), result.getDefaultReportPeriod());
                onReportPeriodChange();
                popupSlot.addToPopupSlot(DeclarationDownloadReportsPresenter.this);
            }
        }, this));
    }

    @Override
    public TaxType getTaxType() {
        return taxType;
    }

    @Override
    public void onReportPeriodChange() {
        if (getView().getSelectedReportPeriod().isEmpty()) {
            getView().updateEnabled();
            return;
        }
        GetDeclarationDepartmentsAction action = new GetDeclarationDepartmentsAction();
        action.setTaxType(taxType);
        action.setReportPeriodId(getView().getSelectedReportPeriod().get(0));
        action.setReports(true);
        dispatcher.execute(action, CallbackUtils.defaultCallback(new AbstractCallback<GetDeclarationDepartmentsResult>() {
            @Override
            public void onSuccess(GetDeclarationDepartmentsResult result) {
                getView().setAcceptableDepartments(result.getDepartments(), result.getDepartmentIds(), result.getDefaultDepartmentId());
                getView().setCorrectionDate(result.getDepartmentReportPeriods());
                getView().updateEnabled();
                onDepartmentChange();
            }
        }, this) );
    }
}
