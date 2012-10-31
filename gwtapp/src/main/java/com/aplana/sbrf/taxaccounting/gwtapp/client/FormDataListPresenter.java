package com.aplana.sbrf.taxaccounting.gwtapp.client;

import java.util.List;

import com.aplana.sbrf.taxaccounting.gwtapp.shared.GetFormDataList;
import com.aplana.sbrf.taxaccounting.gwtapp.shared.GetFormDataListResult;
import com.aplana.sbrf.taxaccounting.model.FormData;
import com.google.gwt.cell.client.ActionCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.proxy.Place;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.Proxy;
import com.gwtplatform.mvp.client.proxy.RevealRootContentEvent;

public class FormDataListPresenter extends Presenter<FormDataListPresenter.MyView, FormDataListPresenter.MyProxy> {
	/**
	 * {@link com.aplana.sbrf.taxaccounting.gwtapp.client.FormDataListPresenter}'s proxy.
	 */
	@ProxyStandard
	@NameToken(nameToken)
	public interface MyProxy extends Proxy<FormDataListPresenter>, Place {
	}

	/**
	 * {@link com.aplana.sbrf.taxaccounting.gwtapp.client.FormDataListPresenter}'s view.
	 */
	public interface MyView extends View {
		void setFormDataList(List<FormData> records);
		public <C> Column<FormData, C> addTableColumn(Cell<C> cell, String headerText, final ValueGetter<C> getter, FieldUpdater<FormData, C> fieldUpdater);
	}

	public static final String nameToken = "formDataList";

	private final PlaceManager placeManager;
	private final DispatchAsync dispatcher;


	@Inject
	public FormDataListPresenter(EventBus eventBus, MyView view, MyProxy proxy, PlaceManager placeManager, DispatchAsync dispatcher) {
		super(eventBus, view, proxy);
		this.placeManager = placeManager;
		this.dispatcher = dispatcher;
	}

	@Override
	protected void onBind() {
		super.onBind();
		// TODO: довольно коряво, проблема в том, у ActionCell нет методов для переопределения делегатов
		// нужно как-то передалать.
		// Также, нужно разобраться, нужно ли здесь делать registerHandler???
		ActionCell<FormData> openFormDataCell = new ActionCell<FormData>(
			"Открыть", 
			new ActionCell.Delegate<FormData>() {
				@Override
				public void execute(FormData formData) {
					String formDataId = formData.getId().toString();
					placeManager.revealPlace(
						new PlaceRequest(FormDataPresenter.NAME_TOKEN).
						with(FormDataPresenter.FORM_DATA_ID, formDataId)
					);
					
				}
			}
		);
		
		getView().addTableColumn(
			openFormDataCell,
			"Action",
			new ValueGetter<FormData>() {
				@Override
				public FormData getValue(FormData contact) {
					return contact;
				}
			},
			null
		);
	}

	@Override
	protected void onReset() {
		super.onReset();
		loadFormDataList();
	}

	@Override
	protected void revealInParent() {
		RevealRootContentEvent.fire(this, this);
	}

	protected void loadFormDataList() {
		dispatcher.execute(new GetFormDataList(), new AsyncCallback<GetFormDataListResult>() {
			@Override
			public void onFailure(Throwable caught) {
				caught.printStackTrace();
			}

			@Override
			public void onSuccess(GetFormDataListResult result) {
				getView().setFormDataList(result.getRecords());	
			}
		});
	}
}
