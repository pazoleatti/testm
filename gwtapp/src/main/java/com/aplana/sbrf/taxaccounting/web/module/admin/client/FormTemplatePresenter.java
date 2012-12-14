package com.aplana.sbrf.taxaccounting.web.module.admin.client;

import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
import com.aplana.sbrf.taxaccounting.model.FormTemplate;
import com.aplana.sbrf.taxaccounting.model.Script;
import com.aplana.sbrf.taxaccounting.web.main.api.client.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.RevealContentTypeHolder;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.MessageEvent;
import com.aplana.sbrf.taxaccounting.web.module.admin.shared.GetFormAction;
import com.aplana.sbrf.taxaccounting.web.module.admin.shared.GetFormResult;
import com.aplana.sbrf.taxaccounting.web.module.admin.shared.UpdateFormAction;
import com.aplana.sbrf.taxaccounting.web.module.admin.shared.UpdateFormResult;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.annotations.Title;
import com.gwtplatform.mvp.client.proxy.Place;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.Proxy;
import com.gwtplatform.mvp.client.proxy.RevealContentEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Это презентер (GWT) формы редактирования шаблона (банковской) формы.
 * <p/>
 * Здесь капец, как всего много. Поэтому я испытываю жуткое желание разделить форму редактирования формы на
 * различные формы: редактирования скриптов и управления событиями. Тем более, что в будущем
 * здесь появятся еще несколько вкладок для работы со столбцами и все такое.
 *
 * @author Vitalii Samolovskikh
 */
public class FormTemplatePresenter extends Presenter<FormTemplatePresenter.MyView, FormTemplatePresenter.MyProxy>
		implements FormTemplateUiHandlers {
	/**
	 * Название параметра запроса идентификатора шаблона формы.
	 */
	public static final String PARAM_FORM_TEMPLATE_ID = "formTemplateId";

	private final DispatchAsync dispatcher;
	private final PlaceManager placeManager;

	/**
	 * Идентификатор шаблона формы. Его мы храним для перезагрузки формы.
	 */
	private int formId;

	/**
	 * Сам шаблон формы.
	 */
	private FormTemplate formTemplate;

	/**
	 * Конструктор каким-то таинственным образом получает диспатчер, view и все остальные нужные нам ништяки.
	 */
	@Inject
	public FormTemplatePresenter(EventBus eventBus, MyView view, MyProxy proxy, PlaceManager placeManager, DispatchAsync dispatcher) {
		super(eventBus, view, proxy);
		this.dispatcher = dispatcher;
		this.placeManager = placeManager;
		getView().setUiHandlers(this);
	}

	/**
	 * Подготовка формы. Здесь мы получаем с сервера шаблон формы и биндим его на форму.
	 *
	 * @param request запрос, из него мы получаем идентификатор шаблона формы.
	 */
	@Override
	public void prepareFromRequest(PlaceRequest request) {
		super.prepareFromRequest(request);

		ListBox elb = getView().getEventListBox();
		elb.clear();
		for (FormDataEvent event : FormDataEvent.values()) {
			elb.addItem(event.getTitle(), String.valueOf(event.getCode()));
		}

		formId = Integer.valueOf(request.getParameter(PARAM_FORM_TEMPLATE_ID, "0"));
		load();
	}

	/**
	 * Поднимает скрипт в очереди на обюработку события на 1 позицию.
	 *
	 * @see com.aplana.sbrf.taxaccounting.web.module.admin.client.FormTemplateUiHandlers#upEventScript()
	 */
	@Override
	public void upEventScript() {
		Script script = getSelectedScript(getView().getEventScriptListBox());
		FormDataEvent event = getSelectedEvent();
		if (script != null && event != null) {
			List<Script> scripts = formTemplate.getScriptsByEvent(event);
			int ind = scripts.indexOf(script);
			if (ind > 0) {
				Script exchange = scripts.get(ind - 1);
				scripts.set(ind - 1, script);
				scripts.set(ind, exchange);
				selectEvent();
				getView().getEventScriptListBox().setSelectedIndex(ind - 1);
			}
		}
	}

	/**
	 * Опускает скрипт в очереди на обработку события на 1 позицию.
	 *
	 * @see com.aplana.sbrf.taxaccounting.web.module.admin.client.FormTemplateUiHandlers#downEventScript()
	 */
	@Override
	public void downEventScript() {
		Script script = getSelectedScript(getView().getEventScriptListBox());
		FormDataEvent event = getSelectedEvent();
		if (script != null && event != null) {
			List<Script> scripts = formTemplate.getScriptsByEvent(event);
			int ind = scripts.indexOf(script);
			if (ind < scripts.size() - 1) {
				Script exchange = scripts.get(ind + 1);
				scripts.set(ind + 1, script);
				scripts.set(ind, exchange);
				selectEvent();
				getView().getEventScriptListBox().setSelectedIndex(ind + 1);
			}
		}
	}

	/**
	 * биндит скрипты события на форму после выбора события.
	 *
	 * @see com.aplana.sbrf.taxaccounting.web.module.admin.client.FormTemplateUiHandlers#selectEvent()
	 */
	@Override
	public void selectEvent() {
		ListBox slb = getView().getEventScriptListBox();
		slb.clear();

		ListBox flb = getView().getFreeScriptListBox();
		flb.clear();

		FormDataEvent event = getSelectedEvent();

		List<Script> scripts = formTemplate.getScripts();

		if (event != null && scripts != null) {
			List<Script> eventScripts = formTemplate.getScriptsByEvent(event);
			if (eventScripts != null) {
				for (Script script : eventScripts) {
					slb.addItem(script.getName(), String.valueOf(formTemplate.indexOfScript(script)));
				}
			}

			List<Script> freeScripts = new ArrayList<Script>(scripts);
			if (eventScripts != null) {
				for (Iterator<Script> i = freeScripts.iterator(); i.hasNext(); ) {
					Script script = i.next();
					if (eventScripts.contains(script)) {
						i.remove();
					}
				}
			}

			for (Script script : freeScripts) {
				flb.addItem(script.getName(), String.valueOf(formTemplate.indexOfScript(script)));
			}
		}
	}

	/**
	 * @return выбранное событие
	 */
	private FormDataEvent getSelectedEvent() {
		FormDataEvent event = null;
		ListBox elb = getView().getEventListBox();
		int selectedIndex = elb.getSelectedIndex();
		if (selectedIndex >= 0) {
			int code = Integer.valueOf(elb.getValue(selectedIndex));
			event = FormDataEvent.getByCode(code);
		}
		return event;
	}

	/**
	 * Добавляет скрипт на событие формы. В конец списка выполнения.
	 *
	 * @see com.aplana.sbrf.taxaccounting.web.module.admin.client.FormTemplateUiHandlers#addScriptToEvent()
	 */
	@Override
	public void addScriptToEvent() {
		Script script = getSelectedScript(getView().getFreeScriptListBox());
		if (script != null) {
			formTemplate.addEventScript(getSelectedEvent(), script);
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
			script = formTemplate.getScripts().get(Integer.valueOf(lb.getValue(ind)));
		}
		return script;
	}

	/**
	 * Удаляет скрипт из списка обработчиков события формы.
	 *
	 * @see com.aplana.sbrf.taxaccounting.web.module.admin.client.FormTemplateUiHandlers#removeScriptFromEvent()
	 */
	@Override
	public void removeScriptFromEvent() {
		Script script = getSelectedScript(getView().getEventScriptListBox());
		if (script != null) {
			formTemplate.removeEventScript(getSelectedEvent(), script);
			selectEvent();
		}
	}

	/**
	 * Биндит данные скрипта на форму редактирования скрипта
	 *
	 * @see com.aplana.sbrf.taxaccounting.web.module.admin.client.FormTemplateUiHandlers#selectScript()
	 */
	@Override
	public void selectScript() {
		bindScript();
	}

	/**
	 * Создает новый скрипт.
	 *
	 * @see com.aplana.sbrf.taxaccounting.web.module.admin.client.FormTemplateUiHandlers#createScript()
	 */
	@Override
	public void createScript() {
		Script script = new Script();
		script.setName("Новый");
		formTemplate.addScript(script);
		bindFormTemplate();

		ListBox listBox = getView().getScriptListBox();
		listBox.setSelectedIndex(listBox.getItemCount() - 1);
		bindScript();
	}

	/**
	 * Удаляет скрипт. Полностью.
	 *
	 * @see com.aplana.sbrf.taxaccounting.web.module.admin.client.FormTemplateUiHandlers#deleteScript()
	 */
	@Override
	public void deleteScript() {
		formTemplate.removeScript(getSelectedScript());
		bindFormTemplate();
	}

	/**
	 * Загружает шабло формы и обновляет поля в представлении.
	 *
	 * @see com.aplana.sbrf.taxaccounting.web.module.admin.client.FormTemplateUiHandlers#load()
	 */
	@Override
	public void load() {
		GetFormAction action = new GetFormAction();
		action.setId(formId);
		dispatcher.execute(action, new AbstractCallback<GetFormResult>() {
			@Override
			public void onReqSuccess(GetFormResult result) {
				formTemplate = result.getForm();
				bindFormTemplate();
				super.onReqSuccess(result);
			}
		});
	}

	/**
	 * Сохраняет шаблон формы. Отправляет его на сервер.
	 *
	 * @see com.aplana.sbrf.taxaccounting.web.module.admin.client.FormTemplateUiHandlers#save()
	 */
	@Override
	public void save() {
		getView().getScriptEditor().flush();
		UpdateFormAction action = new UpdateFormAction();
		action.setForm(formTemplate);
		dispatcher.execute(action, new AbstractCallback<UpdateFormResult>() {
			@Override
			public void onReqSuccess(UpdateFormResult result) {
				MessageEvent.fire(FormTemplatePresenter.this, "Форма Сохранена");
				load();
				super.onReqSuccess(result);
			}

			@Override
			protected boolean needErrorOnFailure(){
				return false;
			}

			@Override
			protected void onReqFailure(Throwable throwable){
				MessageEvent.fire(FormTemplatePresenter.this, "Request Failure", throwable);
			}

		});
	}

	/**
	 * Биндит шаблон формы на представление.
	 */
	private void bindFormTemplate() {
		getView().getTitleLabel().setText(formTemplate.getType().getName());

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
	 * Биндит скрипт в область редактирования скрипта.
	 */
	private void bindScript() {
		getView().getScriptEditor().flush();
		getView().getScriptEditor().setValue(getSelectedScript());
	}

	/**
	 * @return выбранный скрипт
	 */
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

	/**
	 * Закрыть форму редактирования и вернуться на форму администрирования со списком шаблонов форм.
	 */
	@Override
	public void close() {
		placeManager.revealPlace(new PlaceRequest(AdminNameTokens.adminPage));
	}

	/**
	 * Интерфейс представления.
	 */
	public interface MyView extends View, HasUiHandlers<FormTemplateUiHandlers> {
		/**
		 * Элемент редактирования скриптов
		 */
		public ScriptEditor getScriptEditor();

		/**
		 * список скриптов формы
		 */
		public ListBox getScriptListBox();

		/**
		 * список событий
		 */
		public ListBox getEventListBox();

		/**
		 * список скриптов, назначенных на событие
		 */
		public ListBox getEventScriptListBox();

		/**
		 * списко скриптов не назначенных на выбранное событие
		 */
		public ListBox getFreeScriptListBox();

		Label getTitleLabel();
	}

	@Title("Администрирование")
	@ProxyCodeSplit
	@NameToken(AdminNameTokens.formTemplatePage)
	public interface MyProxy extends Proxy<FormTemplatePresenter>, Place {
	}

	@Override
	protected void revealInParent() {
		RevealContentEvent.fire(this, RevealContentTypeHolder.getMainContent(), this);
	}
}
