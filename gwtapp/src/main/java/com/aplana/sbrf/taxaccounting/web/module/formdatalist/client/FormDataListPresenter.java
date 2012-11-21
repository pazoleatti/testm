package com.aplana.sbrf.taxaccounting.web.module.formdatalist.client;

import com.aplana.sbrf.taxaccounting.model.FilterData;
import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.web.main.api.client.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.RevealContentTypeHolder;
import com.aplana.sbrf.taxaccounting.web.module.formdata.client.FormDataPresenter;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.client.filter.FilterPresenter;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.client.filter.FilterReadyEvent;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.shared.GetFormDataList;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.shared.GetFormDataListResult;
import com.google.gwt.cell.client.ActionCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.user.cellview.client.Column;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.annotations.ProxyEvent;
import com.gwtplatform.mvp.client.proxy.*;

import java.util.List;

public class FormDataListPresenter extends
		Presenter<FormDataListPresenter.MyView, FormDataListPresenter.MyProxy>
		implements FormDataListUiHandlers, FilterReadyEvent.MyHandler {
	/**
	 * {@link com.aplana.sbrf.taxaccounting.web.module.formdatalist.client.FormDataListPresenter}
	 * 's proxy.
	 */
	@ProxyCodeSplit
	@NameToken(nameToken)
	public interface MyProxy extends ProxyPlace<FormDataListPresenter>, Place {
	}

	/**
	 * {@link com.aplana.sbrf.taxaccounting.web.module.formdatalist.client.FormDataListPresenter}
	 * 's view.
	 */
	public interface MyView extends View, HasUiHandlers<FormDataListUiHandlers> {
		void setFormDataList(List<FormData> records);

		public <C> Column<FormData, C> addTableColumn(Cell<C> cell,
				String headerText, final ValueGetter<C> getter,
				FieldUpdater<FormData, C> fieldUpdater);
	}

	public static final String nameToken = "!formDataList";

	private final PlaceManager placeManager;
	private final DispatchAsync dispatcher;

	private final FilterPresenter filterPresenter;
	static final Object TYPE_filterPresenter = new Object();

	@Inject
	public FormDataListPresenter(EventBus eventBus, MyView view, MyProxy proxy,
			PlaceManager placeManager, DispatchAsync dispatcher,
			FilterPresenter filterPresenter) {
		super(eventBus, view, proxy);
		this.placeManager = placeManager;
		this.dispatcher = dispatcher;
		this.filterPresenter = filterPresenter;
		getView().setUiHandlers(this);
	}

	@Override
	protected void onBind() {
		super.onBind();
		// TODO: довольно коряво, проблема в том, у ActionCell нет методов для
		// переопределения делегатов
		// нужно как-то передалать.
		// Также, нужно разобраться, нужно ли здесь делать registerHandler???
		ActionCell<FormData> openFormDataCell = new ActionCell<FormData>(
				"Открыть", new ActionCell.Delegate<FormData>() {
					@Override
					public void execute(FormData formData) {
						String formDataId = formData.getId().toString();
						placeManager.revealPlace(new PlaceRequest(
								FormDataPresenter.NAME_TOKEN).with(
								FormDataPresenter.FORM_DATA_ID, formDataId));

					}
				});

		getView().addTableColumn(openFormDataCell, "Action",
				new ValueGetter<FormData>() {
					@Override
					public FormData getValue(FormData contact) {
						return contact;
					}
				}, null);
	}

	/* (non-Javadoc)
	 * @see com.gwtplatform.mvp.client.Presenter#useManualReveal()
	 */
	@Override
	public boolean useManualReveal() {
		return true;
	}

	/* (non-Javadoc)
	 * @see com.gwtplatform.mvp.client.Presenter#prepareFromRequest(com.gwtplatform.mvp.client.proxy.PlaceRequest)
	 */
	@Override
	public void prepareFromRequest(PlaceRequest request) {
		super.prepareFromRequest(request);
		filterPresenter.initFilter();
	}

	@Override
	protected void onReveal() {
		super.onReveal();
		setInSlot(TYPE_filterPresenter, filterPresenter);
	}

	@Override
	protected void onHide() {
		super.onHide();
		clearSlot(TYPE_filterPresenter);
	}

	@Override
	protected void revealInParent() {
		RevealContentEvent.fire(this, RevealContentTypeHolder.getMainContent(),
				this);
	}

	/**
	 * Применение фильтра, обновление списка форм
	 * 
	 * @param filterData
	 */
	private void loadFormDataList(FilterData filterData) {
		GetFormDataList action = new GetFormDataList();
		action.setFilterData(filterData);

		dispatcher.execute(action,
				new AbstractCallback<GetFormDataListResult>() {
					@Override
					public void onSuccess(GetFormDataListResult result) {
						getView().setFormDataList(result.getRecords());
						if (!isVisible()) {
							// Вручную вызывается onReveal
							getProxy().manualReveal(FormDataListPresenter.this);
						}
					}
				});
	}

	@Override
	public void onApplyFilter() {
		FilterData filterData = filterPresenter.getFilterData();
		loadFormDataList(filterData);
	}

	/* (non-Javadoc)
	 * @see com.aplana.sbrf.taxaccounting.web.module.formdatalist.client.filter.FilterReadyEvent.MyHandler#onFilterReady(com.aplana.sbrf.taxaccounting.web.module.formdatalist.client.filter.FilterReadyEvent)
	 */
	@ProxyEvent
	@Override
	public void onFilterReady(FilterReadyEvent event) {
		if (event.getSource() == filterPresenter) {
			FilterData filterData = filterPresenter.getFilterData();
			loadFormDataList(filterData);
		}
	}

	@Override
	public void onCreateClicked() {

	}

}
