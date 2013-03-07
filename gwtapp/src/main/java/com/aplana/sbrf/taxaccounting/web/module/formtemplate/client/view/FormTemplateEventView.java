package com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.view;

import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
import com.aplana.sbrf.taxaccounting.model.Script;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.presenter.FormTemplateEventPresenter;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FormTemplateEventView extends ViewWithUiHandlers<FormTemplateEventUiHandlers> implements FormTemplateEventPresenter.MyView {

	public interface Binder extends UiBinder<Widget, FormTemplateEventView> { }

	private final Widget widget;

	@UiField
	ListBox eventListBox;

	@UiField
	ListBox eventScriptListBox;

	@UiField
	ListBox remainScriptListBox;

	@UiField
	Button upEventScript;

	private List<Script> scripts;
	private List<Script> eventScripts;

	@Inject
	public FormTemplateEventView(Binder uiBinder) {
		widget = uiBinder.createAndBindUi(this);

		eventListBox.clear();
		for (FormDataEvent event : FormDataEvent.values()) {
			eventListBox.addItem(event.getTitle(), String.valueOf(event.getCode()));
		}
	}

	@Override
	public Widget asWidget() {
		return widget;
	}


	@UiHandler("upEventScript")
	public void onUpEventScript(ClickEvent event){
		upEventScript();
	}

	@UiHandler("downEventScript")
	public void onDownEventScript(ClickEvent event){
		downEventScript();
	}

	@UiHandler("addScriptToEvent")
	public void onAddScriptToEvent(ClickEvent event){
		addScriptToEvent();
	}

	@UiHandler("removeScriptFromEvent")
	public void onRemoveScriptFromEvent(ClickEvent event){
		removeScriptFromEvent();
	}

	@UiHandler("eventListBox")
	public void onSelectEvent(ChangeEvent event){
		selectEvent();
	}

	/**
	 * @return выбранное событие
	 */
	private FormDataEvent getSelectedEvent() {
		FormDataEvent event = null;
		int selectedIndex = eventListBox.getSelectedIndex();
		if (selectedIndex >= 0) {
			int code = Integer.valueOf(eventListBox.getValue(selectedIndex));
			event = FormDataEvent.getByCode(code);
		}
		return event;
	}

	/**
	 * Поднимает скрипт в очереди на обюработку события на 1 позицию.
	 *
	 */
	public void upEventScript() {
		Script script = getSelectedScript(eventScriptListBox);
		FormDataEvent event = getSelectedEvent();
		if (script != null && event != null) {
			int ind = eventScripts.indexOf(script);
			if (ind > 0) {
				Script exchange = eventScripts.get(ind - 1);
				eventScripts.set(ind - 1, script);
				eventScripts.set(ind, exchange);
				selectEvent();
				eventScriptListBox.setSelectedIndex(ind - 1);
			}
		}
	}

	/**
	 * Опускает скрипт в очереди на обработку события на 1 позицию.
	 *
	 */
	public void downEventScript() {
		Script script = getSelectedScript(eventScriptListBox);
		FormDataEvent event = getSelectedEvent();
		if (script != null && event != null) {
			int ind = eventScripts.indexOf(script);
			if (ind < eventScripts.size() - 1) {
				Script exchange = eventScripts.get(ind + 1);
				eventScripts.set(ind + 1, script);
				eventScripts.set(ind, exchange);
				selectEvent();
				eventScriptListBox.setSelectedIndex(ind + 1);
			}
		}
	}

	/**
	 * биндит скрипты события на форму после выбора события.
	 *
	 */
	@Override
	public void selectEvent() {
		eventScriptListBox.clear();
		remainScriptListBox.clear();
		FormDataEvent event = getSelectedEvent();
		scripts = getUiHandlers().getScripts();
		eventScripts = getUiHandlers().getScriptsByEvent(event);

		if (event != null && scripts != null) {
			if (eventScripts != null) {
				for (Script script : eventScripts) {
					eventScriptListBox.addItem(script.getName(), String.valueOf(scripts.indexOf(script)));
				}
			}

			List<Script> remainScripts = new ArrayList<Script>(scripts);
			if (eventScripts != null) {
				for (Iterator<Script> i = remainScripts.iterator(); i.hasNext(); ) {
					Script script = i.next();
					if (eventScripts.contains(script)) {
						i.remove();
					}
				}
			}

			for (Script script : remainScripts) {
				remainScriptListBox.addItem(script.getName(), String.valueOf(scripts.indexOf(script)));
			}
		}
	}

	/**
	 * Добавляет скрипт на событие формы. В конец списка выполнения.
	 *
	 */
	private void addScriptToEvent() {
		Script script = getSelectedScript(remainScriptListBox);
		if (script != null) {
			getUiHandlers().addEventScript(getSelectedEvent(), script);
			selectEvent();
		}
	}

	/**
	 * Удаляет скрипт из списка обработчиков события формы.
	 *
	 */
	private void removeScriptFromEvent() {
		Script script = getSelectedScript(eventScriptListBox);
		if (script != null) {
			getUiHandlers().removeEventScript(getSelectedEvent(), script);
			selectEvent();
		}
	}

	/**
	 * Возвращает скрипт, выбранный из списка, если в качестве значения в списке используется идентификатор.
	 *
	 * @param lb ListBox
	 */
	private Script getSelectedScript(ListBox lb) {
		Script script = null;
		int ind = lb.getSelectedIndex();
		if (ind >= 0) {
			script = scripts.get(Integer.valueOf(lb.getValue(ind)));
		}
		return script;
	}
}