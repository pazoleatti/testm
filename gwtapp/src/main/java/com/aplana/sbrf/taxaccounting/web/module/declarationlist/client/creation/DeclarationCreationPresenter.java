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
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.HasPopupSlot;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;

import java.util.List;
import java.util.Set;

/**
 * Диалог создания декларации
 */
public class DeclarationCreationPresenter extends PresenterWidget<DeclarationCreationPresenter.MyView> implements DeclarationCreationUiHandlers {

	public interface MyView extends PopupView, HasUiHandlers<DeclarationCreationUiHandlers> {
		void setAcceptableDeclarationTypes(List<DeclarationType> declarationType);
		void setAcceptableReportPeriods(List<ReportPeriod> reportPeriods);
		void setAcceptableDepartments(List<Department> departments, Set<Integer> departmentsIds);
		
		void setSelectedDeclarationType(Integer id);
		void setSelectedReportPeriod(List<Integer> periodIds);
		void setSelectedDepartment(List<Integer> departmentIds);
		void setSelectedTaxOrganCode(List<Long> taxOrganCode);
		void setSelectedTaxOrganKpp(List<Long> taxOrganKpp);

		Integer getSelectedDeclarationType();
		List<Integer> getSelectedReportPeriod();
		List<Integer> getSelectedDepartment();
        void setTaxType(TaxType taxType);

        void setTaxOrganFilter(String filter);
        String getTaxOrganCode();
        String getTaxOrganKpp();
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
			CheckExistenceDeclaration checkCommand = new CheckExistenceDeclaration();
			checkCommand.setDeclarationTypeId(filter.getDeclarationTypeId());
			checkCommand.setDepartmentId(filter.getDepartmentIds().iterator().next());
			checkCommand.setReportPeriodId(filter.getReportPeriodIds().iterator().next());
            checkCommand.setTaxOrganCode(filter.getTaxOrganCode());
            checkCommand.setTaxOrganKpp(filter.getTaxOrganKpp());
            checkCommand.setTaxType(taxType);
			dispatcher.execute(checkCommand, CallbackUtils
					.defaultCallback(new AbstractCallback<CheckExistenceDeclarationResult>() {
						@Override
						public void onSuccess(final CheckExistenceDeclarationResult checkResult) {
                            if (checkResult.getStatus() != CheckExistenceDeclarationResult.DeclarationStatus.NOT_EXIST) {
                                LogAddEvent.fire(DeclarationCreationPresenter.this, checkResult.getUuid());
                                Dialog.warningMessage("Создание декларации", "Декларация не создана");
                            } else {
								CreateDeclaration command = new CreateDeclaration();
								command.setDeclarationTypeId(filter.getDeclarationTypeId());
                                // TODO Передать отчетный период подразделения http://jira.aplana.com/browse/SBRFACCTAX-8840
                                command.setDepartmentReportPeriodId(-1);

                                command.setTaxOrganCode(filter.getTaxOrganCode());
                                command.setTaxOrganKpp(filter.getTaxOrganKpp());
                                command.setTaxType(taxType);
								dispatcher.execute(command, CallbackUtils
										.defaultCallback(new AbstractCallback<CreateDeclarationResult>() {
											@Override
											public void onSuccess(CreateDeclarationResult result) {
                                                onHide();
                                                placeManager
                                                        .revealPlace(new PlaceRequest.Builder().nameToken(DeclarationDataTokens.declarationData)
                                                                .with(DeclarationDataTokens.declarationId, String.valueOf(result.getDeclarationId())).build());
                                                LogAddEvent.fire(DeclarationCreationPresenter.this, result.getUuid());
											}
										}, DeclarationCreationPresenter.this));
							}
						}
					}, DeclarationCreationPresenter.this));
		}
	}

	@Override
	public void onDepartmentChange() {
		if (getView().getSelectedDepartment().isEmpty() || getView().getSelectedReportPeriod().isEmpty()) {
            getView().setTaxOrganFilter(null);
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
                getView().setTaxOrganFilter(result.getFilter());
			}
		}, this) );
	}

	private boolean isFilterDataCorrect(DeclarationDataFilter filter){
        if ((filter.getReportPeriodIds() == null || filter.getReportPeriodIds().isEmpty())
                        || (filter.getDepartmentIds() == null || filter.getDepartmentIds().isEmpty())
                        || (filter.getDeclarationTypeId() == null)
                        || (taxType.equals(TaxType.PROPERTY)
                                && ((filter.getTaxOrganCode() == null || filter.getTaxOrganCode().isEmpty())
                                        || (filter.getTaxOrganKpp() == null || filter.getTaxOrganKpp().isEmpty()))
                            )){
            Dialog.errorMessage("Создание декларации", "Заполнены не все параметры декларации");
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
        GetDeclarationFilterData action = new GetDeclarationFilterData();
        action.setTaxType(dataFilter.getTaxType());
	    this.taxType = dataFilter.getTaxType();
        getView().setTaxType(this.taxType);
        dispatcher.execute(action, CallbackUtils.defaultCallback(new AbstractCallback<GetDeclarationFilterDataResult>() {
            @Override
            public void onSuccess(GetDeclarationFilterDataResult result) {
                getView().setAcceptableReportPeriods(result.getPeriodsForCreation());
                popupSlot.addToPopupSlot(DeclarationCreationPresenter.this);
            }
        }, this) );
    }

    @Override
    public TaxType getTaxType() {
        return taxType;
    }

    @Override
    public void onReportPeriodChange() {
        if (getView().getSelectedReportPeriod().isEmpty()) {
            return;
        }
        GetDeclarationDepartmentsAction action = new GetDeclarationDepartmentsAction();
        action.setTaxType(taxType);
        action.setReportPeriodId(getView().getSelectedReportPeriod().get(0));
        dispatcher.execute(action, CallbackUtils.defaultCallback(new AbstractCallback<GetDeclarationDepartmentsResult>() {
            @Override
            public void onSuccess(GetDeclarationDepartmentsResult result) {
                getView().setAcceptableDepartments(result.getDepartments(), result.getDepartmentIds());
            }
        }, this) );
    }
}
