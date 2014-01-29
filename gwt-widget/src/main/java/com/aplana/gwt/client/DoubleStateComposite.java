package com.aplana.gwt.client;

import com.google.gwt.user.client.ui.*;

/**
 * Базовый класс для наших виджетов, который в состоянии disable показыввает Label со значением вместо виджета.
 *
 * @author Vitaliy Samolovskikh
 */
public abstract class DoubleStateComposite extends Composite implements HasEnabled {
	public static final String EMPTY_STRING_VALUE = "-";
	/**
	 * Эта панель показывает только один виджет, из всех, которые в неё помещены. У нас она будет показывать либо сам
	 * виджет, либо Label со значением.
	 */
	private DeckPanel deckPanel = new DeckPanel();

	/**
	 * Label со значением.
	 */
	private Label label = new Label(EMPTY_STRING_VALUE);

	/**
	 * Флаг скрытия
	 */
	private boolean enabled = true;

	/**
	 * Здесь мы берем пользовательский виджет и помещаем его в нашу deckPanel. А её уже инициализируем.
	 *
	 * @param widget the widget to be wrapped
	 */
	@Override
	protected void initWidget(Widget widget) {
		deckPanel.add(widget);
		label.addStyleName("aplana-DoubleStateComposite-label");
		deckPanel.add(label);
		deckPanel.showWidget(0);
		super.initWidget(deckPanel);
	}

	/**
	 * Returns true if the widget is enabled, false if not.
	 */
	@Override
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * Sets whether this widget is enabled.
	 *
	 * @param enabled <code>true</code> to enable the widget, <code>false</code>
	 *                to disable it
	 */
	@Override
	public void setEnabled(boolean enabled) {
		if (enabled) {
			// Show the widget
			deckPanel.showWidget(0);
		} else {
			// Show the label
			if (this instanceof HasValue) {
				setLabelValue(((HasValue) this).getValue());
			}
			deckPanel.showWidget(1);
		}

		this.enabled = enabled;
	}

	protected void setLabelValue(Object value){
		String stringValue;
		if (value == null) {
			stringValue = EMPTY_STRING_VALUE;
		} else {
			stringValue = value.toString();
			if (stringValue.trim().isEmpty()) {
				stringValue = EMPTY_STRING_VALUE;
			}
		}
		label.setText(stringValue);
	}
}
