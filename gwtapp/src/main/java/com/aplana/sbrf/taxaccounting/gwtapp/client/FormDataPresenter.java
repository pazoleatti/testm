package com.aplana.sbrf.taxaccounting.gwtapp.client;

import com.aplana.sbrf.taxaccounting.gwtapp.shared.GetFormData;
import com.aplana.sbrf.taxaccounting.gwtapp.shared.GetFormDataResult;
import com.aplana.sbrf.taxaccounting.gwtapp.shared.SaveDataAction;
import com.aplana.sbrf.taxaccounting.gwtapp.shared.SaveDataResult;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.FormData;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
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
import com.gwtplatform.mvp.client.proxy.RevealRootContentEvent;

public class FormDataPresenter extends
Presenter<FormDataPresenter.MyView, FormDataPresenter.MyProxy> {

    /**
     * Our data
     */
    private FormData formData;

    /**
	 * {@link com.aplana.sbrf.taxaccounting.gwtapp.client.FormDataPresenter}'s proxy.
	 */
	@ProxyCodeSplit
	@NameToken(NAME_TOKEN)
	public interface MyProxy extends Proxy<FormDataPresenter>, Place {
	}

	/**
	 * {@link com.aplana.sbrf.taxaccounting.gwtapp.client.FormDataPresenter}'s view.
	 */
	public interface MyView extends View {
		Button getCancelButton();
        Button getSaveButton();
		DataGrid<DataRow> getFormDataTable();
		void loadFormData(FormData formData);
	}

	public static final String NAME_TOKEN = "formData";

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
		dispatcher.execute(action, new AsyncCallback<GetFormDataResult>() {
			@Override
			public void onFailure(Throwable caught) {
				caught.printStackTrace();
			}

			@Override
			public void onSuccess(GetFormDataResult result) {
                formData = result.getFormData();
                getView().loadFormData(formData);
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
        registerHandler(getView().getSaveButton().addClickHandler(
                new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        SaveDataAction action = new SaveDataAction();
                        action.setFormData(formData);
                        dispatcher.execute(action, new AsyncCallbackAdapter<SaveDataResult>(){
                            @Override
                            public void onSuccess(SaveDataResult result) {
                                Window.alert("Всё сохранено!");
                            }

                            @Override
                            public void onFailure(Throwable throwable) {
                                Window.alert("Fail!");
                            }
                        });
                    }
                }
        ));
	}

	@Override
	protected void onReset() {
		super.onReset();
	}

	@Override
	protected void revealInParent() {
		RevealRootContentEvent.fire(this, this);
	}
}
