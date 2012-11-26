package com.aplana.sbrf.taxaccounting.web.module.admin.client;

import com.gwtplatform.mvp.client.UiHandlers;

/**
 * Короче это интерфейс обработчика событий формы. обработчиком событий формы в модели MVP является Presenter.
 * В нашем случае <code>AdminPresenter</code>.
 *
 * @see AdminPresenter
 * @see AdminView
 * @author Vitalii Samolovskikh
 */
public interface AdminUiHandlers extends UiHandlers {
	/**
	 * Обработчик события выбора шаблона формы.
	 */
	public void select();
}
