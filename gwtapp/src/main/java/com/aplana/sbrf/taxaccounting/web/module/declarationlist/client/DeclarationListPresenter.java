package com.aplana.sbrf.taxaccounting.web.module.declarationlist.client;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.*;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.*;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.*;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.client.*;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.client.filter.*;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared.*;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.google.gwt.user.client.Window;
import com.google.gwt.view.client.*;
import com.google.inject.*;
import com.google.web.bindery.event.shared.*;
import com.gwtplatform.dispatch.shared.*;
import com.gwtplatform.mvp.client.annotations.*;
import com.gwtplatform.mvp.client.proxy.*;

import java.util.*;

public class DeclarationListPresenter extends
		DeclarationListPresenterBase<DeclarationListPresenter.MyProxy> implements
		DeclarationListUiHandlers, DeclarationFilterReadyEvent.MyHandler{

	private static final int PAGE_SIZE = 20;
	private static boolean IS_FIRST_TIME = false;
	private final TableDataProvider dataProvider = new TableDataProvider();

	@ProxyCodeSplit
	@NameToken(DeclarationListNameTokens.DECLARATION_LIST)
	public interface MyProxy extends ProxyPlace<DeclarationListPresenter>, Place {
	}

	@Inject
	public DeclarationListPresenter(EventBus eventBus, DeclarationListPresenterBase.MyView view, MyProxy proxy,
	                         PlaceManager placeManager, DispatchAsync dispatcher,
	                         DeclarationFilterPresenter filterPresenter) {
		super(eventBus, view, proxy, placeManager, dispatcher, filterPresenter);
		getView().setUiHandlers(this);
		getView().assignDataProvider(PAGE_SIZE, dataProvider);
	}

	@Override
	public void prepareFromRequest(PlaceRequest request) {
		try {
			LogCleanEvent.fire(this);
			LogShowEvent.fire(this, false);
			super.prepareFromRequest(request);
			TaxType taxType = TaxType.valueOf(request.getParameter("nType", ""));
			filterPresenter.initFilter(taxType);
			getView().updateTitle(taxType.getName());
		} catch (Exception e) {
			ErrorEvent.fire(this, "Не удалось открыть список деклараций", e);
		}
	}

	/**
	 * Применение фильтра, обновление списка деклараций
	 */
	private void loadDeclarationsList() {
		refreshTable();
		updateTitle(filterPresenter.getCurrentTaxType());
		// Вручную вызывается onReveal. Вызываем его всегда,
		// даже когда
		// презентер в состоянии visible, т.к. нам необходима
		// его разблокировка.
		// Почему GWTP вызывает блокировку даже если страница
		// уже видна - непонятно.
		getProxy().manualReveal(DeclarationListPresenter.this);
	}

	@Override
	public void onApplyFilter() {
		loadDeclarationsList();
		filterPresenter.updateSavedFilterData(filterPresenter.getFilterData());
	}

	@Override
	public void onCreateClicked() {
		final DeclarationDataFilter filter = filterPresenter.getFilterData();
		if(isFilterDataCorrect(filter)){
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
											placeManager
													.revealPlace(new PlaceRequest(DeclarationDataTokens.declarationData)
															.with(DeclarationDataTokens.declarationId,
																	String.valueOf(checkResult.getDeclarationDataId()))
													);
										}
									}));
							}
						}else if(checkResult.getStatus() == CheckExistenceDeclarationResult.DeclarationStatus.EXIST_ACCEPTED) {
							MessageEvent.fire(DeclarationListPresenter.this, "Переформирование невозможно, так как декларация уже принята.");
						} else {
							CreateDeclaration command = new CreateDeclaration();
							command.setDeclarationTypeId(filter.getDeclarationTypeId());
							command.setDepartmentId(filter.getDepartmentIds().iterator().next());
							command.setReportPeriodId(filter.getReportPeriodIds().iterator().next());
							dispatcher.execute(command, CallbackUtils
									.defaultCallback(new AbstractCallback<CreateDeclarationResult>() {
										@Override
										public void onSuccess(CreateDeclarationResult result) {
											placeManager
													.revealPlace(new PlaceRequest(DeclarationDataTokens.declarationData)
															.with(DeclarationDataTokens.declarationId, String.valueOf(result.getDeclarationId()))
													);
										}
							}));
						}
					}
				}));
		}

	}


	@ProxyEvent
	@Override
	public void onFilterReady(DeclarationFilterReadyEvent event) {
		if (event.getSource() == filterPresenter) {
			loadDeclarationsList();
		}
	}

	public void refreshTable() {
		dataProvider.update();
	}

	@Override
	public void onSortingChanged(){
		refreshTable();
	}

	private boolean isFilterDataCorrect(DeclarationDataFilter filter){
		if(filter.getDeclarationTypeId() == null){
			MessageEvent.fire(DeclarationListPresenter.this, "Для создания декларации необходимо выбрать вид декларации");
			return false;
		}
		if(filter.getReportPeriodIds().isEmpty() || filter.getReportPeriodIds().size() > 1){
			MessageEvent.fire(DeclarationListPresenter.this, "Для создания декларации необходимо выбрать один отчетный период");
			return false;
		}
		if(filter.getDepartmentIds().isEmpty() || filter.getDepartmentIds().size() > 1){
			MessageEvent.fire(DeclarationListPresenter.this, "Для создания декларации необходимо выбрать одно подразделение");
			return false;
		}
		return true;
	}

	private void updateTitle(TaxType taxType){
		String description = "";
		if(taxType.getName().equals(TaxType.VAT.getName())){
			description = "Деклараци по НДС";
		}  else if (taxType.getName().equals(TaxType.PROPERTY.getName())){
			description = "Деклараци по налогу на имущество";
		}  else if (taxType.getName().equals(TaxType.TRANSPORT.getName())){
			description = "Деклараци по транспортному налогу";
		}  else if (taxType.getName().equals(TaxType.INCOME.getName())){
			description = "Деклараци по налогу на прибыль";
		}
		TitleUpdateEvent.fire(this, "Список деклараций", description);
	}

	private class TableDataProvider extends AsyncDataProvider<DeclarationDataSearchResultItem> {

		private int ZERO_RECORDS_COUNT = 0;

		public void update() {
			for (HasData<DeclarationDataSearchResultItem> display: getDataDisplays()) {
				onRangeChanged(display);
			}
		}

		@Override
		protected void onRangeChanged(HasData<DeclarationDataSearchResultItem> display) {
			final Range range = display.getVisibleRange();
			GetDeclarationList requestData = createRequestData(range);
			dispatcher.execute(requestData, CallbackUtils
					.defaultCallback(new AbstractCallback<GetDeclarationListResult>() {
						@Override
						public void onSuccess(GetDeclarationListResult result) {
							if(result == null || result.getTotalCountOfRecords() == ZERO_RECORDS_COUNT){
								getView().setDeclarationsList(range.getStart(), ZERO_RECORDS_COUNT,
										new ArrayList<DeclarationDataSearchResultItem>());
							} else {
								handleResponse(result, range);
							}
						}
					}));
		}

		private GetDeclarationList createRequestData(Range range) {
			if(IS_FIRST_TIME){
				DeclarationDataFilter filter = filterPresenter.getFilterData();
				filter.setCountOfRecords(PAGE_SIZE);
				filter.setStartIndex(range.getStart());
				filter.setAscSorting(getView().isAscSorting());
				filter.setSearchOrdering(getView().getSearchOrdering());

				GetDeclarationList request = new GetDeclarationList();
				request.setDeclarationFilter(filter);
				return request;
			}
			IS_FIRST_TIME = true;
			return (new GetDeclarationList());
		}

		private void handleResponse(GetDeclarationListResult response, Range range) {
			getView().setDeclarationsList(range.getStart(), response.getTotalCountOfRecords(), response.getRecords());
		}
	}
}
