package com.aplana.sbrf.taxaccounting.web.module.declarationlist.client.creation;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.MessageEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogAddEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogCleanEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogShowEvent;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.client.DeclarationDataTokens;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared.*;
import com.google.gwt.user.client.Window;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
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
		void setDeclarationFilter(DeclarationDataFilter filter);
		void setDeclarationFilterValues(DeclarationDataFilterAvailableValues filterValues);
		void setReportPeriods(List<ReportPeriod> reportPeriods);
		void setDepartments(List<Department> departments, Set<Integer> departmentsIds);
		void setSelectedReportPeriod(Integer reportPeriodId);
		DeclarationDataFilter updateAndGetDeclarationFilter();
	}

	private DispatchAsync dispatcher;
	private PlaceManager placeManager;

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
		getView().hide();
	}
	
	public void setDeclarationFilter(DeclarationDataFilter filter) {
		getView().setDeclarationFilter(filter);
	}

	public void setFilterValues(DeclarationDataFilterAvailableValues filterValues) {
		getView().setDeclarationFilterValues(filterValues);
	}

	public void setDepartments(List<Department> departments, Set<Integer> departmentsIds) {
		getView().setDepartments(departments, departmentsIds);
	}
	
	public void setReportPeriods(List<ReportPeriod> reportPeriods) {
		getView().setReportPeriods(reportPeriods);
	}

	@Override
	public void onContinue() {
		final DeclarationDataFilter filter = getView().updateAndGetDeclarationFilter();
		if(isFilterDataCorrect(filter)){
			LogCleanEvent.fire(this);
			LogShowEvent.fire(this, false);
			CheckExistenceDeclaration checkCommand = new CheckExistenceDeclaration();
			checkCommand.setDeclarationTypeId(filter.getDeclarationTypeId());
			checkCommand.setDepartmentId(filter.getDepartmentIds().iterator().next());
			checkCommand.setReportPeriodId(filter.getReportPeriodIds().iterator().next());
			dispatcher.execute(checkCommand, CallbackUtils
					.defaultCallback(new AbstractCallback<CheckExistenceDeclarationResult>() {
						@Override
						public void onSuccess(final CheckExistenceDeclarationResult checkResult) {
							if (checkResult.getStatus() == CheckExistenceDeclarationResult.DeclarationStatus.EXIST_CREATED) {
								if (Window.confirm("Декларация с указанными параметрами уже существует. Переформировать?")) {
									RefreshDeclaration refreshDeclarationCommand = new RefreshDeclaration();
									refreshDeclarationCommand.setDeclarationDataId(checkResult.getDeclarationDataId());
									dispatcher.execute(refreshDeclarationCommand, CallbackUtils
											.defaultCallback(new AbstractCallback<RefreshDeclarationResult>() {
												@Override
												public void onSuccess(RefreshDeclarationResult result) {
                                                    onHide();
                                                    placeManager
                                                            .revealPlace(new PlaceRequest.Builder().nameToken(DeclarationDataTokens.declarationData)
                                                                    .with(DeclarationDataTokens.declarationId,
                                                                            String.valueOf(checkResult.getDeclarationDataId())).build());
                                                    LogAddEvent.fire(DeclarationCreationPresenter.this, result.getLogEntries());
												}
											}, DeclarationCreationPresenter.this));
								}
							} else if (checkResult.getStatus() == CheckExistenceDeclarationResult.DeclarationStatus.EXIST_ACCEPTED) {
								MessageEvent.fire(DeclarationCreationPresenter.this, "Переформирование невозможно, так как декларация уже принята.");
							} else {
								CreateDeclaration command = new CreateDeclaration();
								command.setDeclarationTypeId(filter.getDeclarationTypeId());
								command.setDepartmentId(filter.getDepartmentIds().iterator().next());
								command.setReportPeriodId(filter.getReportPeriodIds().iterator().next());
								dispatcher.execute(command, CallbackUtils
										.defaultCallback(new AbstractCallback<CreateDeclarationResult>() {
											@Override
											public void onSuccess(CreateDeclarationResult result) {
                                                onHide();
                                                placeManager
                                                        .revealPlace(new PlaceRequest.Builder().nameToken(DeclarationDataTokens.declarationData)
                                                                .with(DeclarationDataTokens.declarationId, String.valueOf(result.getDeclarationId())).build());
                                                LogAddEvent.fire(DeclarationCreationPresenter.this, result.getLogEntries());
											}
										}, DeclarationCreationPresenter.this));
							}
						}
					}, DeclarationCreationPresenter.this));
		}
	}


	private boolean isFilterDataCorrect(DeclarationDataFilter filter){
		if(filter.getDeclarationTypeId() == null){
			MessageEvent.fire(this, "Для создания декларации необходимо выбрать вид декларации");
			return false;
		}
		if(filter.getReportPeriodIds() == null || filter.getReportPeriodIds().isEmpty()){
			MessageEvent.fire(this, "Для создания декларации необходимо выбрать один отчетный период");
			return false;
		}
		if(filter.getDepartmentIds() == null || filter.getDepartmentIds().isEmpty()){
			MessageEvent.fire(this, "Для создания декларации необходимо выбрать одно подразделение");
			return false;
		}
		return true;
	}
}
