package com.aplana.sbrf.taxaccounting.web.module.declarationlist.client.creation;

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
public class DeclarationCreationPresenter extends PresenterWidget<DeclarationCreationPresenter.MyView> implements DeclarationCreationUiHandlers {

    public interface MyView extends PopupView, HasUiHandlers<DeclarationCreationUiHandlers> {

        void setAcceptableDeclarationTypes(List<DeclarationType> declarationType);
        void setAcceptableReportPeriods(List<ReportPeriod> reportPeriods, ReportPeriod reportPeriod);
        Integer getDefaultReportPeriodId();
        void setAcceptableDepartments(List<Department> departments, Set<Integer> departmentsIds, Integer departmentsId);

        void setSelectedDeclarationType(Integer id);
        void setSelectedReportPeriod(List<Integer> periodIds);
        void setSelectedDepartment(List<Integer> departmentIds);
        void setCorrectionDate(String correctionDate, DeclarationFormKind declarationFormKind);

        Integer getSelectedDeclarationType();
        List<Integer> getSelectedReportPeriod();
        List<Integer> getSelectedDepartment();
        void setTaxType(TaxType taxType);

        void init();

        void updateEnabled();
    }

    private DispatchAsync dispatcher;
    private PlaceManager placeManager;

    private TaxType taxType;
    private DeclarationFormKind declarationFormKind;

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
        filter.setDeclarationTypeIds(Arrays.asList(getView().getSelectedDeclarationType().longValue()));
        filter.setDepartmentIds(getView().getSelectedDepartment());
        filter.setReportPeriodIds(getView().getSelectedReportPeriod());
        if(isFilterDataCorrect(filter)){
            LogCleanEvent.fire(this);
            LogShowEvent.fire(this, false);
            if (declarationFormKind.equals(DeclarationFormKind.REPORTS)) {
                // создание отчетности
                onCreateForms(filter, false);
            } else {
                CreateDeclaration command = new CreateDeclaration();
                command.setDeclarationTypeId(filter.getDeclarationTypeIds().get(0).intValue());
                command.setDepartmentId(filter.getDepartmentIds().iterator().next());
                command.setReportPeriodId(filter.getReportPeriodIds().iterator().next());
                command.setTaxType(taxType);
                dispatcher.execute(command, CallbackUtils
                        .defaultCallback(new AbstractCallback<CreateDeclarationResult>() {
                            @Override
                            public void onSuccess(CreateDeclarationResult result) {
                                if (result.getDeclarationId() == null) {
                                    LogAddEvent.fire(DeclarationCreationPresenter.this, result.getUuid());
                                    String title = (declarationFormKind.equals(DeclarationFormKind.REPORTS) ? "Создание отчетности" : "Создание налоговой формы");
                                    String msg = (declarationFormKind.equals(DeclarationFormKind.REPORTS) ? "Отчетности не созданы" : "Налоговоя форма не создана");
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
    }

    private void onCreateForms(final DeclarationDataFilter filter, final boolean force) {
        CreateFormsDeclarationAction action = new CreateFormsDeclarationAction();
        action.setDeclarationTypeId(filter.getDeclarationTypeIds().get(0).intValue());
        action.setDepartmentId(filter.getDepartmentIds().iterator().next());
        action.setReportPeriodId(filter.getReportPeriodIds().iterator().next());
        action.setTaxType(taxType);
        dispatcher.execute(action, CallbackUtils
                .defaultCallback(new AbstractCallback<CreateFormsDeclarationResult>() {
                    @Override
                    public void onSuccess(CreateFormsDeclarationResult result) {
                        LogCleanEvent.fire(DeclarationCreationPresenter.this);
                        LogShowEvent.fire(DeclarationCreationPresenter.this, false);
                        if (!result.isStatus()) {
                            Dialog.confirmMessage(result.getRestartMsg(), new DialogHandler() {
                                @Override
                                public void yes() {
                                    onCreateForms(filter, true);
                                }
                            });
                        } else {
                            onHide();
                            LogAddEvent.fire(DeclarationCreationPresenter.this, result.getUuid());
                        }
                    }
                }, DeclarationCreationPresenter.this));
    }

    @Override
    public void onDepartmentChange() {
        if (getView().getSelectedDepartment().isEmpty() || getView().getSelectedReportPeriod().isEmpty()) {
            return;
        }
        GetDeclarationTypeAction action = new GetDeclarationTypeAction();
        action.setTaxType(taxType);
        action.setDeclarationFormKind(declarationFormKind);
        action.setDepartmentId(getView().getSelectedDepartment().get(0));
        action.setReportPeriod(getView().getSelectedReportPeriod().get(0));

        dispatcher.execute(action, CallbackUtils.defaultCallback(new AbstractCallback<GetDeclarationTypeResult>() {
            @Override
            public void onSuccess(GetDeclarationTypeResult result) {
                getView().setAcceptableDeclarationTypes(result.getDeclarationTypes());
                if (result.getCorrectionDate() != null) {
                    getView().setCorrectionDate(DateTimeFormat.getFormat("dd.MM.yyyy").format(result.getCorrectionDate()), declarationFormKind);
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
                || (filter.getDeclarationTypeIds() == null)
        ){
            String title = (declarationFormKind.equals(DeclarationFormKind.REPORTS) ? "Создание отчетности" : "Создание налоговой формы");
            String msg = (declarationFormKind.equals(DeclarationFormKind.REPORTS) ? "Заполнены не все параметры отчетности" : "Заполнены не все параметры налоговой формы");
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

    public void initAndShowDialog(final DeclarationDataFilter dataFilter, DeclarationFormKind declarationFormKind, final HasPopupSlot popupSlot){
        this.taxType = dataFilter.getTaxType();
        this.declarationFormKind = declarationFormKind;
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

    @Override
    public DeclarationFormKind getDeclarationFormKind() {
        return declarationFormKind;
    }
}
