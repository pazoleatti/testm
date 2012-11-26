package com.aplana.sbrf.taxaccounting.web.module.admin.client;

import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
import com.aplana.sbrf.taxaccounting.model.FormTemplate;
import com.aplana.sbrf.taxaccounting.model.Script;
import com.aplana.sbrf.taxaccounting.web.main.api.client.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.RevealContentTypeHolder;
import com.aplana.sbrf.taxaccounting.web.module.admin.shared.GetFormAction;
import com.aplana.sbrf.taxaccounting.web.module.admin.shared.GetFormResult;
import com.aplana.sbrf.taxaccounting.web.module.admin.shared.UpdateFormAction;
import com.aplana.sbrf.taxaccounting.web.module.admin.shared.UpdateFormResult;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.ListBox;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.Place;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.Proxy;
import com.gwtplatform.mvp.client.proxy.RevealContentEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Vitalii Samolovskikh
 */
public class FormTemplatePresenter extends Presenter<FormTemplatePresenter.MyView, FormTemplatePresenter.MyProxy>
		implements FormTemplateUiHandlers {
	public static final String PARAM_FORM_TEMPLATE_ID = "formTemplateId";

	private final DispatchAsync dispatcher;

	private int formId;
	private FormTemplate formTemplate;

	@Inject
	public FormTemplatePresenter(EventBus eventBus, MyView view, MyProxy proxy, DispatchAsync dispatcher) {
		super(eventBus, view, proxy);
		this.dispatcher = dispatcher;
		getView().setUiHandlers(this);
	}

	@Override
	public void prepareFromRequest(PlaceRequest request) {
		super.prepareFromRequest(request);

		ListBox elb = getView().getEventListBox();
		elb.clear();
		for(FormDataEvent event:FormDataEvent.values()){
			elb.addItem(event.getTitle(), String.valueOf(event.getCode()));
		}

		formId = Integer.valueOf(request.getParameter(PARAM_FORM_TEMPLATE_ID, "0"));
		load();
	}

	@Override
	public void selectEvent() {
		ListBox slb = getView().getEventScriptListBox();
		slb.clear();

		ListBox flb = getView().getFreeScriptListBox();
		flb.clear();

		FormDataEvent event = getSelectedEvent();

		if(event!=null){
			List<Script> scripts = formTemplate.getScriptsByEvent(event);
			if(scripts!=null){
				for(Script script:scripts){
					slb.addItem(script.getName(), String.valueOf(script.getId()));
				}
			}

			List<Script> freeScripts = formTemplate.getScripts();
			if(freeScripts!=null){
				freeScripts = new ArrayList<Script>(freeScripts);

				if(scripts!=null){
					for(Iterator<Script> i=freeScripts.iterator();i.hasNext();){
						Script script = i.next();
						if(scripts.contains(script)){
							i.remove();
						}
					}
				}

				for(Script script:freeScripts){
					flb.addItem(script.getName(), String.valueOf(script.getId()));
				}
			}
		}
	}

	private FormDataEvent getSelectedEvent() {
		FormDataEvent event = null;
		ListBox elb = getView().getEventListBox();
		int selectedIndex = elb.getSelectedIndex();
		if(selectedIndex>=0){
			int code = Integer.valueOf(elb.getValue(selectedIndex));
			event = FormDataEvent.getByCode(code);
		}
		return event;
	}

	@Override
	public void addScriptToEvent() {
		ListBox flb = getView().getFreeScriptListBox();

		Script script = getSelectedScriptById(flb);

		if(script!=null){
			formTemplate.addEventScript(getSelectedEvent(), script);
			selectEvent();
		}
	}

	private Script getSelectedScriptById(ListBox lb) {
		Script script = null;
		int ind = lb.getSelectedIndex();
		if(ind>=0){
			int id = Integer.valueOf(lb.getValue(ind));
			for(Script sc: formTemplate.getScripts()){
				if(sc.getId()==id){
					script = sc;
					break;
				}
			}
		}
		return script;
	}

	@Override
	public void removeScriptFromEvent() {
		ListBox elb = getView().getEventScriptListBox();

		Script script = getSelectedScriptById(elb);

		if(script!=null){
			formTemplate.removeEventScript(getSelectedEvent(), script);
			selectEvent();
		}
	}

	@Override
	public void selectScript() {
		bindScript();
	}

	@Override
	public void createScript() {
		Script script = new Script();
		script.setName("Новый");
		formTemplate.addScript(script);
		bindFormTemplate();
	}

	@Override
	public void deleteScript() {
		formTemplate.removeScript(getSelectedScript());
		bindFormTemplate();
	}

	/**
	 * load form template and update fields
	 */
	@Override
	public void load() {
		GetFormAction action = new GetFormAction();
		action.setId(formId);
		dispatcher.execute(action, new AbstractCallback<GetFormResult>() {
			@Override
			public void onSuccess(GetFormResult result) {
				formTemplate = result.getForm();
				bindFormTemplate();
			}
		});
	}

	@Override
	public void save() {
		getView().getScriptEditor().flush();
		UpdateFormAction action = new UpdateFormAction();
		action.setForm(formTemplate);
		dispatcher.execute(action, new AbstractCallback<UpdateFormResult>() {
			@Override
			public void onSuccess(UpdateFormResult updateFormResult) {
				Window.alert("Форма сохранена.");
			}
		});
	}

	private void bindFormTemplate() {
		ListBox lb = getView().getScriptListBox();
		lb.clear();
		int i = 0;
		for (Script script : formTemplate.getScripts()) {
			lb.addItem(script.getName(), String.valueOf(i++));
		}
		lb.setSelectedIndex(0);
		bindScript();
	}

	/**
	 * Bind script to form
	 */
	private void bindScript() {
		getView().getScriptEditor().flush();
		getView().getScriptEditor().setValue(getSelectedScript());
	}

	private Script getSelectedScript() {
		ListBox slb = getView().getScriptListBox();
		Script script = null;
		int selInd = slb.getSelectedIndex();
		if (selInd >= 0) {
			String str = slb.getValue(selInd);
			int scrInd = Integer.valueOf(str);
			script = formTemplate.getScripts().get(scrInd);
		}
		return script;
	}

	public interface MyView extends View, HasUiHandlers<FormTemplateUiHandlers> {
		public ScriptEditor getScriptEditor();
		public ListBox getScriptListBox();

		public ListBox getEventListBox();
		public ListBox getEventScriptListBox();
		public ListBox getFreeScriptListBox();
	}

	@ProxyCodeSplit
	@NameToken(AdminNameTokens.formTemplatePage)
	public interface MyProxy extends Proxy<FormTemplatePresenter>, Place {
	}

	@Override
	protected void revealInParent() {
		RevealContentEvent.fire(this, RevealContentTypeHolder.getMainContent(), this);
	}
}
