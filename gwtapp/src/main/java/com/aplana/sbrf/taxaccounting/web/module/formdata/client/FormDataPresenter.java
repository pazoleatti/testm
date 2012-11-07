package com.aplana.sbrf.taxaccounting.web.module.formdata.client;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.aplana.sbrf.taxaccounting.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.web.main.api.client.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.RevealContentTypeHolder;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.GetFormData;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.GetFormDataResult;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.SaveFormDataAction;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.SaveFormDataResult;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.client.FormDataListPresenter;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.client.ui.Button;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.Place;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.Proxy;
import com.gwtplatform.mvp.client.proxy.RevealContentEvent;

public class FormDataPresenter extends Presenter<FormDataPresenter.MyView, FormDataPresenter.MyProxy> {
	private Logger logger = Logger.getLogger(getClass().getName());

	/**
	 * {@link com.aplana.sbrf.taxaccounting.web.module.formdata.client.FormDataPresenter}'s proxy.
	 */
	@ProxyCodeSplit
	@NameToken(NAME_TOKEN)
	public interface MyProxy extends Proxy<FormDataPresenter>, Place {
	}

	/**
	 * {@link com.aplana.sbrf.taxaccounting.web.module.formdata.client.FormDataPresenter}'s view.
	 */
	public interface MyView extends View {
		Button getCancelButton();
		Button getSaveButton();
		DataGrid<DataRow> getFormDataTable();
		void loadFormData(FormData formData);
		FormData getFormData();
		void setLogMessages(List<LogEntry> logEntries);
		void reset();
	}

	public static final String NAME_TOKEN = "!formData";

	public static final String FORM_DATA_ID = "formDataId";

	private final DispatchAsync dispatcher;
	private final PlaceManager placeManager;

	@Inject
	public FormDataPresenter(EventBus eventBus, MyView view, MyProxy proxy,
			PlaceManager placeManager, DispatchAsync dispatcher) {
		super(eventBus, view, proxy);
		this.placeManager = placeManager;
		this.dispatcher = dispatcher;
	}

	@Override
	public void prepareFromRequest(PlaceRequest request) {
		super.prepareFromRequest(request);
		long formDataId = Long.parseLong(request.getParameter(FORM_DATA_ID, "none"));
		GetFormData action = new GetFormData();
		action.setFormDataId(formDataId);
		dispatcher.execute(action, new AbstractCallback<GetFormDataResult>() {
			@Override
			public void onSuccess(GetFormDataResult result) {
				getView().loadFormData(result.getFormData());
			}
		});
	}

	@Override
	protected void onBind() {
		super.onBind();

		registerHandler(getView().getCancelButton().addClickHandler(
			new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					placeManager.revealPlace(new PlaceRequest(FormDataListPresenter.nameToken));
				}
			}
		));

		// Save data button
		registerHandler(getView().getSaveButton().addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				SaveFormDataAction action = new SaveFormDataAction();
				final MyView view = getView();
				action.setFormData(view.getFormData());
				dispatcher.execute(action, new AbstractCallback<SaveFormDataResult>(){
					@Override
					public void onSuccess(SaveFormDataResult result) {
						FormData savedFormData = result.getFormData();
						view.reset();
						view.loadFormData(savedFormData);
						view.setLogMessages(result.getLogEntries());						
					}
						
					@Override
					public void onFailure(Throwable throwable) {
						logger.log(Level.SEVERE, "Failed to save formData object", throwable);
						super.onFailure(throwable);
					}							
				});
			}
		}));
}

@Override
protected void onReset() {
	super.onReset();
	getView().reset();
}

@Override
protected void revealInParent() {
	RevealContentEvent.fire(this, RevealContentTypeHolder.getMainContent(), this);
}
}
