package com.aplana.sbrf.taxaccounting.web.module.admin.client;

import com.aplana.sbrf.taxaccounting.model.FormTemplate;
import com.aplana.sbrf.taxaccounting.model.Script;
import com.aplana.sbrf.taxaccounting.web.main.api.client.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.RevealContentTypeHolder;
import com.aplana.sbrf.taxaccounting.web.module.admin.shared.*;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
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

	private FormTemplate formDescriptor;

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
				for (FormTemplate form : result.getForms()) {
					listBox.addItem(form.getType().getName(), form.getId().toString());
				}
			}
		});

		registerHandler(getView().getFormListBox().addChangeHandler(
				new ChangeHandler() {
					@Override
					public void onChange(ChangeEvent changeEvent) {
						loadForm();
					}
				}
		));

		registerHandler(getView().getCancelButton().addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent clickEvent) {
				loadForm();
			}
		}));

		registerHandler(getView().getSaveButton().addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent clickEvent) {
				if (formDescriptor.getCreateScript() == null) {
					formDescriptor.setCreateScript(new Script());
				}

				final MyView view = getView();
				view.getScriptEditor().flush();

				UpdateFormAction action = new UpdateFormAction();
				action.setForm(formDescriptor);
				dispatcher.execute(action, new AbstractCallback<UpdateFormResult>() {
					@Override
					public void onSuccess(UpdateFormResult updateFormResult) {
						Window.alert("Форма сохранена.");
					}
				});
			}
		}));

		registerHandler(getView().getScriptListBox().addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent changeEvent) {
				bindScript();
			}
		}));
	}

	/**
	 * Bind script to form
	 */
	private void bindScript() {
		ListBox slb = getView().getScriptListBox();
		int selInd = slb.getSelectedIndex();
		if(selInd>=0){
			String str = slb.getValue(selInd);
			ScriptEditor scriptEditor = getView().getScriptEditor();
			scriptEditor.flush();

			if(str.equals("create")){
				if(formDescriptor.getCreateScript()==null){
					formDescriptor.setCreateScript(new Script());
				}
				scriptEditor.setValue(formDescriptor.getCreateScript());
			} else {
				int scrInd = Integer.valueOf(str);
				scriptEditor.setValue(formDescriptor.getCalcScripts().get(scrInd));
			}
		}
	}

	/**
	 * loadFormDescriptor and update fields
	 */
	private void loadForm() {
		GetFormAction action = new GetFormAction();
		final ListBox listBox = getView().getFormListBox();
		final int selectedIndex = listBox.getSelectedIndex();
		if (selectedIndex >= 0) {
			action.setId(Integer.valueOf(listBox.getValue(selectedIndex)));
			dispatcher.execute(action, new AbstractCallback<GetFormResult>() {
				@Override
				public void onSuccess(GetFormResult result) {
					formDescriptor = result.getForm();

					ListBox lb = getView().getScriptListBox();
					lb.clear();

					lb.addItem("Скрипт создания", "create");

					int i = 0;
					for(Script script:formDescriptor.getCalcScripts()){
						lb.addItem(script.getName(), String.valueOf(i++));
					}

					lb.setSelectedIndex(0);
					bindScript();
				}
			});
		}
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

		public Button getSaveButton();

		public Button getCancelButton();

		public ScriptEditor getScriptEditor();

		ListBox getScriptListBox();
	}
}
