package com.aplana.sbrf.taxaccounting.web.module.admin.client;

import com.gwtplatform.mvp.client.UiHandlers;

/**
 * Интерфейс обработчика событий формы (представления) редактирования шаблона формы (налоговой формы).
 *
 * @see FormTemplateView
 * @see FormTemplatePresenter
 * @author Vitalii Samolovskikh
 */
public interface FormTemplateUiHandlers extends UiHandlers {
	/**
	 * загрзка формы. Означает не то что форма загрузилась и теперь этот надо как-то обработать.
	 * Означает что формы надо загрузить.
	 */
	public void load();

	/**
	 * Сохранить форму.
	 */
	public void save();

	/**
	 * Выбран скрипт. По этому событию в области редактирования скрипта презентер загружает скрипт.
	 */
	public void selectScript();

	/**
	 * Создать новый скрипт формы
	 */
	public void createScript();

	/**
	 * Удалить скрипт.
	 */
	public void deleteScript();

	/**
	 * Выбрано событие.
	 */
	public void selectEvent();

	/**
	 * Добавить скрипт как обработчик события
	 */
	public void addScriptToEvent();

	/**
	 * Удалить скрипт как обработчик события
	 */
	public void removeScriptFromEvent();

	/**
	 * Поднять скрипт в очередли на выполнение на 1 позицию
	 */
	public void upEventScript();

	/**
	 * Опустить скрипт на одну позицию в очереди на выполнение
	 */
	public void downEventScript();
}
