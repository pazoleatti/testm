package com.aplana.sbrf.taxaccounting.web.module.admin.client;

import com.gwtplatform.mvp.client.UiHandlers;

/**
 * Короче это интерфейс обработчика событий формы. обработчиком событий формы в модели MVP является Presenter.
 * В нашем случае <code>AdminPresenter</code>.
 *
 * @author Vitalii Samolovskikh
 * @see AdminPresenter
 * @see AdminView
 */
public interface AdminUiHandlers extends UiHandlers {
	/**
	 * Обработчик события выбора шаблона формы.
	 *
	 * @param id идентификатор шаблона выбранной формы. Это, конечно, нарушает чистоту модели MVP, но с другой стороны...
	 *           Ну а что делать?
	 */
	public void select(Integer id);
}
