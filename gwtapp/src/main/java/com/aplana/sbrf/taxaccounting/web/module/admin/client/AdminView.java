package com.aplana.sbrf.taxaccounting.web.module.admin.client;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

/**
 * Представление для страницы администрирования.
 *
 * @author Vitalii Samolovskikh
 */
public class AdminView extends ViewWithUiHandlers<AdminUiHandlers> implements AdminPresenter.MyView {
	interface Binder extends UiBinder<Widget, AdminView> {
	}

	private final Widget widget;

	/**
	 * Список шаблонов форм. А больше здесь нифига нет.
	 */
	@UiField
	ListBox formListBox;

	@Inject
	public AdminView(Binder binder) {
		widget = binder.createAndBindUi(this);
	}

	/**
	 * Вызываем обработчик на выбор пользователем формы.
	 *
	 * @param event событие выбора. Мы его, конечно, не используем. Но, насколько я понимаю,
	 *              оно тут должно быть, чтобы было ясно какое именно событие мы будем обрабатывать.
	 */
	@UiHandler("formListBox")
	public void onFormListBoxChange(ChangeEvent event) {
		if (getUiHandlers() != null) {
			getUiHandlers().select();
		}
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	/**
	 * @see AdminPresenter.MyView
	 */
	@Override
	public ListBox getListBox() {
		return formListBox;
	}
}
