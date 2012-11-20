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

	private FormTemplate formTemplate;

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
				if (formTemplate.getCreateScript() == null) {
					formTemplate.setCreateScript(new Script());
				}

				final MyView view = getView();
				view.getScriptEditor().flush();

				UpdateFormAction action = new UpdateFormAction();
				action.setForm(formTemplate);
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

		registerHandler(getView().getCreateScriptButton().addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent clickEvent) {
				Script script = new Script();
				script.setName("Новый");
				formTemplate.getCalcScripts().add(script);
				script.setOrder(formTemplate.getCalcScripts().size());
				bindFormTemplate();
			}
		}));

		registerHandler(getView().getDeleteScriptButton().addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent clickEvent) {
				formTemplate.getCalcScripts().remove(getSelectedScript());
				bindFormTemplate();
			}
		}));
	}

	/**
	 * Bind script to form
	 */
	private void bindScript() {
		ScriptEditor scriptEditor = getView().getScriptEditor();
		scriptEditor.flush();
		scriptEditor.setValue(getSelectedScript());
	}

	private Script getSelectedScript() {
		Script script = null;

		ListBox slb = getView().getScriptListBox();
		int selInd = slb.getSelectedIndex();
		if (selInd >= 0) {
			String str = slb.getValue(selInd);
			if (str.equals("create")) {
				if (formTemplate.getCreateScript() == null) {
					formTemplate.setCreateScript(new Script());
				}
				script = formTemplate.getCreateScript();
			} else {
				int scrInd = Integer.valueOf(str);
				script = formTemplate.getCalcScripts().get(scrInd);
			}
		}
		return script;
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
					formTemplate = result.getForm();

					bindFormTemplate();
				}
			});
		}
	}

	private void bindFormTemplate() {
		ListBox lb = getView().getScriptListBox();
		lb.clear();

		lb.addItem("Скрипт создания", "create");

		int i = 0;
		for (Script script : formTemplate.getCalcScripts()) {
			lb.addItem(script.getName(), String.valueOf(i++));
		}

		lb.setSelectedIndex(0);
		bindScript();
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

		Button getCreateScriptButton();

		Button getDeleteScriptButton();
	}
}
