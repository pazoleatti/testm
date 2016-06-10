package com.aplana.sbrf.taxaccounting.web.module.declarationlist.client.creation;

import com.aplana.gwt.client.dialog.Dialog;
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

import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Диалог создания декларации
 */
public class DeclarationCreationPresenter extends PresenterWidget<DeclarationCreationPresenter.MyView> implements DeclarationCreationUiHandlers {

    public interface MyView extends PopupView, HasUiHandlers<DeclarationCreationUiHandlers> {

        void setAcceptableDeclarationTypes(List<DeclarationType> declarationType);
        void setAcceptableReportPeriods(List<ReportPeriod> reportPeriods, ReportPeriod reportPeriod);
        Integer getDefaultReportPeriodId();
        void setAcceptableDepartments(List<Department> departments, Set<Integer> departmentsIds, Integer departmentsId);

        void setSelectedDeclarationType(Integer id);
        void setSelectedReportPeriod(List<Integer> periodIds);
        void setSelectedDepartment(List<Integer> departmentIds);
        void setSelectedTaxOrganCode(String taxOrganCode);
        void setSelectedTaxOrganKpp(String taxOrganKpp);
        void setCorrectionDate(String correctionDate, TaxType taxType);

        Integer getSelectedDeclarationType();
        List<Integer> getSelectedReportPeriod();
        List<Integer> getSelectedDepartment();
        void setTaxType(TaxType taxType);

        String getTaxOrganCode();
        String getTaxOrganKpp();

        void init();

        void initRefBooks(Date version, String filter, TaxType taxType);
        void updateEnabled();
    }

    private DispatchAsync dispatcher;
    private PlaceManager placeManager;

    private TaxType taxType;

    @Inject
    public DeclarationCreationPresenter(final EventBus eventBus, final MyView view,
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
        filter.setDeclarationTypeId(getView().getSelectedDeclarationType());
        filter.setDepartmentIds(getView().getSelectedDepartment());
        filter.setReportPeriodIds(getView().getSelectedReportPeriod());
        filter.setTaxOrganCode(getView().getTaxOrganCode());
        filter.setTaxOrganKpp(getView().getTaxOrganKpp());
        if(isFilterDataCorrect(filter)){
            LogCleanEvent.fire(this);
            LogShowEvent.fire(this, false);
            CreateDeclaration command = new CreateDeclaration();
            command.setDeclarationTypeId(filter.getDeclarationTypeId());
            command.setDepartmentId(filter.getDepartmentIds().iterator().next());
            command.setReportPeriodId(filter.getReportPeriodIds().iterator().next());
            command.setTaxOrganCode(filter.getTaxOrganCode());
            command.setTaxOrganKpp(filter.getTaxOrganKpp());
            command.setTaxType(taxType);
            dispatcher.execute(command, CallbackUtils
                    .defaultCallback(new AbstractCallback<CreateDeclarationResult>() {
                        @Override
                        public void onSuccess(CreateDeclarationResult result) {
                            if (result.getDeclarationId() == null) {
                                LogAddEvent.fire(DeclarationCreationPresenter.this, result.getUuid());
                                String title = (taxType.equals(TaxType.DEAL) ? "Создание уведомления" : "Создание декларации");
                                String msg = (taxType.equals(TaxType.DEAL) ? "Уведомление не создано" : "Декларация не создана");
                                Dialog.infoMessage(title, msg);
                            } else {
                                onHide();
                                placeManager
                                        .revealPlace(new PlaceRequest.Builder().nameToken(DeclarationDataTokens.declarationData)
                                                .with(DeclarationDataTokens.declarationId, String.valueOf(result.getDeclarationId())).build());
                                LogAddEvent.fire(DeclarationCreationPresenter.this, result.getUuid());
                            }
                        }
                    }, DeclarationCreationPresenter.this));
        }
    }

    @Override
    public void onDepartmentChange() {
        if (getView().getSelectedDepartment().isEmpty() || getView().getSelectedReportPeriod().isEmpty()) {
            return;
        }
        GetDeclarationTypeAction action = new GetDeclarationTypeAction();
        action.setTaxType(taxType);

        action.setDepartmentId(getView().getSelectedDepartment().get(0));
        action.setReportPeriod(getView().getSelectedReportPeriod().get(0));

        dispatcher.execute(action, CallbackUtils.defaultCallback(new AbstractCallback<GetDeclarationTypeResult>() {
            @Override
            public void onSuccess(GetDeclarationTypeResult result) {
                getView().setAcceptableDeclarationTypes(result.getDeclarationTypes());
                if (taxType == TaxType.PROPERTY || taxType == TaxType.TRANSPORT || taxType == TaxType.INCOME) {
                    getView().initRefBooks(result.getVersion(), result.getFilter(), taxType);
                }
                if (result.getCorrectionDate() != null) {
                    getView().setCorrectionDate(DateTimeFormat.getFormat("dd.MM.yyyy").format(result.getCorrectionDate()), result.getTaxType());
                } else {
                    getView().setCorrectionDate(null, null);
                }
                getView().updateEnabled();
            }
        }, this) );
    }

    private boolean isFilterDataCorrect(DeclarationDataFilter filter){
        if ((filter.getReportPeriodIds() == null || filter.getReportPeriodIds().isEmpty())
                || (filter.getDepartmentIds() == null || filter.getDepartmentIds().isEmpty())
                || (filter.getDeclarationTypeId() == null)
                || ((taxType.equals(TaxType.PROPERTY) || taxType.equals(TaxType.TRANSPORT))
                && (filter.getTaxOrganCode() == null || filter.getTaxOrganCode().isEmpty()))
                || ((taxType.equals(TaxType.PROPERTY) || taxType.equals(TaxType.TRANSPORT) || taxType.equals(TaxType.INCOME))
                && (filter.getTaxOrganKpp() == null || filter.getTaxOrganKpp().isEmpty()))
        ){
            String title = (taxType.equals(TaxType.DEAL) ? "Создание уведомления" : "Создание декларации");
            String msg = (taxType.equals(TaxType.DEAL) ? "Заполнены не все параметры уведомления" : "Заполнены не все параметры декларации");
            Dialog.errorMessage(title, msg);
            return false;
        }
        return true;
    }

    private void clearValues(){
        getView().setSelectedDeclarationType(null);
        getView().setSelectedReportPeriod(null);
        getView().setSelectedDepartment(null);
        getView().setSelectedTaxOrganCode(null);
        getView().setSelectedTaxOrganKpp(null);
    }

    public void initAndShowDialog(final DeclarationDataFilter dataFilter, final HasPopupSlot popupSlot){
        this.taxType = dataFilter.getTaxType();
        getView().setTaxType(this.taxType);
        GetReportPeriodsAction action = new GetReportPeriodsAction();
        action.setTaxType(dataFilter.getTaxType());
        action.setReportPeriodId(getView().getDefaultReportPeriodId());
        getView().init();
        dispatcher.execute(action, CallbackUtils.defaultCallback(new AbstractCallback<GetReportPeriodsResult>() {
            @Override
            public void onSuccess(GetReportPeriodsResult result) {
                getView().setAcceptableReportPeriods(result.getReportPeriods(), result.getDefaultReportPeriod());
                onReportPeriodChange();
                popupSlot.addToPopupSlot(DeclarationCreationPresenter.this);
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
        dispatcher.execute(action, CallbackUtils.defaultCallback(new AbstractCallback<GetDeclarationDepartmentsResult>() {
            @Override
            public void onSuccess(GetDeclarationDepartmentsResult result) {
                getView().setAcceptableDepartments(result.getDepartments(), result.getDepartmentIds(), result.getDefaultDepartmentId());
                getView().updateEnabled();
                onDepartmentChange();
            }
        }, this) );
    }
}
