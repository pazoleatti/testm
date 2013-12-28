package com.aplana.gwt.client;

import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.*;

import java.util.Date;

/**
 * Простой DatePicker для выбора даты без календарика. Позволяет набирать дату на клавиатуре.
 *
 * @author Vitaliy Samolovskikh
 */
public class SimpleDatePicker extends Composite
		implements HasValue<Date>, HasValueChangeHandlers<Date>, HasEnabled {
	/**
	 * Название класса стиял для компонента.
	 */
	public static final String STYLE_NAME = Constants.STYLE_PREFIX + "SimpleDatePicker";

	/**
	 * Название стиля для задисабленных элементов. Это нужно для поддержки IE8.
	 */
	public static final String STYLE_DISABLED = "disabled";

	/**
	 * Основная панель, на которой размещаются все остальные элементы.
	 */
	private final LayoutPanel panel = new LayoutPanel();

	private final Spinner date = new Spinner();
	private final ListBox month = new ListBox();
	private final Spinner year = new Spinner();

	private Date value;

	private boolean enabled = true;

	/**
	 * Обработчики события изменения значения.
	 */
	private ValueChangeHandlerHolder<Date> handlerHolder = new ValueChangeHandlerHolder<Date>();

	public SimpleDatePicker() {
		initLayout();

		initBehavior();
	}

	/**
	 * Init layout
	 */
	private void initLayout() {
		initWidget(panel);

		date.setMinValue(1);
		date.setMaxValue(31);
		date.setValue(1);
		panel.add(date);

		for (Month month1 : Month.values()) {
			month.addItem(month1.getName(), month1.getStringOrder());
		}
		panel.add(month);

		year.setMinValue(1900);
		year.setMaxValue(2100);
		panel.add(year);
	}

	/**
	 * Init behavior
	 */
	@SuppressWarnings("unchecked")
	private void initBehavior() {
		setValue(new Date());
		SubElementValueChangeHandler subElementValueChangeHandler = new SubElementValueChangeHandler();
		date.addValueChangeHandler(subElementValueChangeHandler);
		date.addValueChangeHandler(subElementValueChangeHandler);
		date.addValueChangeHandler(subElementValueChangeHandler);
	}

	@Override
	public Date getValue() {
		// Да, да, да. Deprecated. А что поделаешь? Календаря в GWT нет.
		// DateTimeFormat поступает точно так же.
		return value;
	}

	/**
	 * Собирает дату по кусочкам из 3х субэлементов.
	 *
	 * @return дата
	 */
	private Date createValue() {
		return new Date(year.getValue(), getMonthValue() - 1, date.getValue(), 12, 0);
	}

	private int getMonthValue() {
		return Month.findByOrder(month.getValue(month.getSelectedIndex())).getOrder();
	}

	/**
	 * Устанавливает новое значение элемента.
	 *
	 * @param value новое значение
	 */
	@Override
	public void setValue(Date value) {
		this.value = value;

		// TODO Надо б что-нибудь получше придумать.
		date.setValue(value.getDate());
		month.setSelectedIndex(value.getMonth());
		year.setValue(value.getYear());
	}

	@Override
	public void setValue(Date value, boolean fireEvents) {
		setValue(value);
		if (fireEvents) {
			ValueChangeEvent.fire(this, value);
		}
	}

	@Override
	public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Date> handler) {
		return handlerHolder.addValueChangeHandler(handler);
	}

	/**
	 * !!! НЕ ИСПОЛЬЗОВАТЬ !!!
	 * <p/>
	 * Передает событие всем обработчикам.
	 */
	@Override
	public void fireEvent(GwtEvent<?> event) {
		handlerHolder.fireEvent(event);
		super.fireEvent(event);
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public void setEnabled(boolean enabled) {
		date.setEnabled(enabled);
		month.setEnabled(enabled);
		year.setEnabled(enabled);
		this.enabled = enabled;
	}

	/**
	 * ValueChangeHandler для субэлементов. Обновляет значение в DatePicker'е.
	 */
	private class SubElementValueChangeHandler implements ValueChangeHandler {
		@Override
		public void onValueChange(ValueChangeEvent event) {
			setValue(createValue(), true);
		}
	}
}
