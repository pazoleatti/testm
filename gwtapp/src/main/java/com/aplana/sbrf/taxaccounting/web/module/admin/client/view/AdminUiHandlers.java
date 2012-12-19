package com.aplana.sbrf.taxaccounting.web.module.admin.client.view;

import com.gwtplatform.mvp.client.UiHandlers;

/**
 * Короче это интерфейс обработчика событий формы. обработчиком событий формы в модели MVP является Presenter.
 * В нашем случае <code>AdminPresenter</code>.
 *
 * @author Vitalii Samolovskikh
 * @see com.aplana.sbrf.taxaccounting.web.module.admin.client.presenter.AdminPresenter
 * @see AdminView
 */
public interface AdminUiHandlers extends UiHandlers {
	/**
	 * Обработчик события выбора шаблона формы.
	 *
	 * @param id идентификатор шаблона выбранной формы.
	 */
	public void selectForm(Integer id);
}
