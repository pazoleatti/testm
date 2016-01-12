package com.aplana.gwt.client;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
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
	public static final int XIX = 1900;

	/**
	 * Основная панель, на которой размещаются все остальные элементы.
	 */
	private final HorizontalPanel panel = new HorizontalPanel();

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

		initStyles();
	}

	private void initStyles() {
		panel.addStyleName(STYLE_NAME);
		date.addStyleName(STYLE_NAME + "-date");
		month.addStyleName(STYLE_NAME + "-month");
		year.addStyleName(STYLE_NAME + "-year");
	}

	/**
	 * Init layout
	 */
	private void initLayout() {
		initWidget(panel);

		date.setMinValue(1);
		date.setMaxValue(31);
		date.setValue(1);
		date.setWidth("35px");
		panel.add(date);

		for (Month month1 : Month.values()) {
			month.addItem(month1.getName(), month1.getStringOrder());
		}
		month.setWidth("85px");
		panel.add(month);

		year.setMinValue(1900);
		year.setMaxValue(2100);
		year.setWidth("50px");
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
		month.addChangeHandler(subElementValueChangeHandler);
		year.addValueChangeHandler(subElementValueChangeHandler);
	}

	/**
	 * Устанавливает точность: день, месяц, год.
	 *
	 * @param precision точность
	 * @see com.aplana.gwt.client.SimpleDatePicker.Precision
	 */
	public void setPrecision(Precision precision) {
		// Remove date element if precision is MONTH or YEAR
		if (precision.compareTo(Precision.DATE) > 0) {
			panel.remove(date);
		}

		// Remove month element if precision id YEAR
		if (precision.compareTo(Precision.MONTH) > 0) {
			panel.remove(month);
		}

		// Add month element if precision is MONTH or DATE
		if (precision.compareTo(Precision.YEAR) < 0 && panel.getWidgetIndex(month) == -1) {
			panel.insert(month, panel.getWidgetIndex(year));
		}

		if (precision == Precision.DATE && panel.getWidgetIndex(date) == -1) {
			panel.insert(date, panel.getWidgetIndex(month));
		}
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
		return new Date(year.getValue() - XIX, getMonthValue() - 1, date.getValue(), 12, 0);
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
	public final void setValue(Date value) {
		this.value = value;

		// TODO Надо б что-нибудь получше придумать.
		date.setValue(value.getDate());
		month.setSelectedIndex(value.getMonth());
		year.setValue(value.getYear() + 1900);
	}

	@Override
	public final void setValue(Date value, boolean fireEvents) {
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
	 * Точность вводимого значения: день, месяц, год.
	 */
	public enum Precision {
		DATE, MONTH, YEAR
	}

	/**
	 * ValueChangeHandler для субэлементов. Обновляет значение в DatePicker'е.
	 */
	private class SubElementValueChangeHandler implements ValueChangeHandler, ChangeHandler {
		@Override
		public void onValueChange(ValueChangeEvent event) {
			setValue(createValue(), true);
		}

		@Override
		public void onChange(ChangeEvent event) {
			onValueChange(null);
		}
	}

	/**
	 * Don't use! For testing only.
	 */
	Spinner getDate() {
		return date;
	}

	/**
	 * Don't use! For testing only.
	 */
	ListBox getMonth() {
		return month;
	}

	/**
	 * Don't use! For testing only.
	 */
	Spinner getYear() {
		return year;
	}
}
