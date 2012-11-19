package com.aplana.sbrf.taxaccounting.web.module.admin.client;

import com.aplana.sbrf.taxaccounting.model.Form;
import com.aplana.sbrf.taxaccounting.web.main.api.client.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.RevealContentTypeHolder;
import com.aplana.sbrf.taxaccounting.web.module.admin.shared.FormListAction;
import com.aplana.sbrf.taxaccounting.web.module.admin.shared.FormListResult;
import com.aplana.sbrf.taxaccounting.web.module.admin.shared.GetFormAction;
import com.aplana.sbrf.taxaccounting.web.module.admin.shared.GetFormResult;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.Place;
import com.gwtplatform.mvp.client.proxy.Proxy;
import com.gwtplatform.mvp.client.proxy.RevealContentEvent;

/**
 * @author Vitalii Samolovskikh
 */
public class AdminPresenter extends Presenter<AdminPresenter.MyView, AdminPresenter.MyProxy> {
    private final DispatchAsync dispatcher;

    private Form formDescriptor;

    @Inject
    public AdminPresenter(EventBus eventBus, MyView view, MyProxy proxy, DispatchAsync dispatcher) {
        super(eventBus, view, proxy);
        this.dispatcher = dispatcher;
    }

    @Override
    protected void onBind() {
        super.onBind();

        dispatcher.execute(new FormListAction(), new AbstractCallback<FormListResult>() {
            @Override
            public void onSuccess(FormListResult result) {
                final ListBox listBox = getView().getFormListBox();
                listBox.clear();
                for(Form form:result.getForms()){
                    listBox.addItem(form.getType().getName(), form.getId().toString());
                }
            }
        });

        registerHandler(getView().getFormListBox().addChangeHandler(
                new ChangeHandler() {
                    @Override
                    public void onChange(ChangeEvent changeEvent) {
                        GetFormAction action = new GetFormAction();
                        final ListBox listBox = getView().getFormListBox();
                        action.setId(Integer.valueOf(listBox.getValue(listBox.getSelectedIndex())));
                        dispatcher.execute(action, new AbstractCallback<GetFormResult>() {
                            @Override
                            public void onSuccess(GetFormResult result) {
                                formDescriptor = result.getForm();
                                getView().getCreateScriptBody().setValue(formDescriptor.getCreateScript().getBody());
                            }
                        });
                    }
                }
        ));
    }

    @Override
    protected void revealInParent() {
        RevealContentEvent.fire(this, RevealContentTypeHolder.getMainContent(), this);
    }

    /**
     * {@link com.aplana.sbrf.taxaccounting.web.module.formdata.client.FormDataPresenter}'s proxy.
     */
    @ProxyCodeSplit
    @NameToken(AdminNameTokens.adminPage)
    public interface MyProxy extends Proxy<AdminPresenter>, Place {
    }

    /**
     * {@link com.aplana.sbrf.taxaccounting.web.module.formdata.client.FormDataPresenter}'s view.
     */
    public interface MyView extends View {
        public ListBox getFormListBox();
        public TextArea getCreateScriptBody();
    }
}
