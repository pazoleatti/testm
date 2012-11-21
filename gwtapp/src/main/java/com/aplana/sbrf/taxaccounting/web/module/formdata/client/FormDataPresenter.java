package com.aplana.sbrf.taxaccounting.web.module.formdata.client;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.service.FormDataAccessService;
import com.aplana.sbrf.taxaccounting.web.main.api.client.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.RevealContentTypeHolder;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.AccessFlags;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.GetFormData;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.GetFormDataResult;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.SaveFormDataAction;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.SaveFormDataResult;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.client.FormDataListPresenter;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.view.client.SingleSelectionModel;
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
		Button getAddRowButton();
		Button getRemoveRowButton();
		Button getManualInputButton();
		Button getPrintButton();
		Button getOriginalVersionButton();
		DataGrid<DataRow> getFormDataTable();
		void loadFormData(FormData formData, AccessFlags flags);
		void reloadFormData(FormData formData, AccessFlags flags);
		void reloadRows();
		FormData getFormData();
		void setLogMessages(List<LogEntry> logEntries);
		void reset();
		Boolean isReadOnly();
//		void setReadOnly(Boolean readOnly);
		void activateEditMode();
		void activateReadOnlyMode();
		
//		void loadFormData(FormData formData, AccessFlags flags);
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
//				AccessFlags flags = result.getAccessFlags();
//				Window.alert(flags.getCanCreate() +" " + flags.getCanRead() + " " + flags.getCanEdit());
				getView().loadFormData(result.getFormData(), result.getAccessFlags());
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

		final MyView view = getView();
		
		// Save data button
		registerHandler(view.getSaveButton().addClickHandler(new ClickHandler() {
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
						view.loadFormData(savedFormData, null);
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
		
		registerHandler(view.getAddRowButton().addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				FormData formData = view.getFormData();
				formData.appendDataRow(null);
				view.reloadRows();
			}
			
		}));
		
		registerHandler(view.getRemoveRowButton().addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				FormData formData = view.getFormData();
				// TODO need rework
				formData.getDataRows().remove(((SingleSelectionModel<DataRow>)view.getFormDataTable().getSelectionModel()).getSelectedObject());
				view.reloadRows();
			}
		}));
		
		registerHandler(view.getManualInputButton().addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				view.activateEditMode();
			}
			
		}));
		
		registerHandler(view.getPrintButton().addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				Window.alert("В разработке");
//				view.activateReadOnlyMode();
			}
			
		}));
		
		registerHandler(view.getOriginalVersionButton().addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
//				Window.alert("В разработке");
				view.activateReadOnlyMode();
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
