package com.aplana.gwt.client;

import com.google.gwt.user.client.ui.*;

/**
 * Базовый класс для наших виджетов, который в состоянии disable показыввает Label со значением вместо виджета.
 *
 * @author Vitaliy Samolovskikh
 */
public abstract class DoubleStateComposite extends Composite implements HasEnabled {
	public static final String EMPTY_STRING_VALUE = "-";
    public static final String EMPTY_STRING_TITLE = "В данном поле значение не было задано";
	/**
	 * Эта панель показывает только один виджет, из всех, которые в неё помещены. У нас она будет показывать либо сам
	 * виджет, либо Label со значением.
	 */
	private DeckPanel deckPanel = new DeckPanel();

	/**
	 * Label со значением.
	 */
	protected Label label = new Label(EMPTY_STRING_VALUE);

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
			updateLabelValue();
            label.setWidth(deckPanel.getWidget(0).getElement().getStyle().getWidth());
			deckPanel.showWidget(1);
		}

		this.enabled = enabled;
	}

	/**
	 * Обновляет значение Label в состоянии disabled.
	 * Кошерно для переопределения, например.
	 */
	protected void updateLabelValue() {
		if (this instanceof HasValue) {
			setLabelValue(((HasValue) this).getValue());
		}
	}

	protected void setLabelValue(Object value) {
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
        if (stringValue.equals(EMPTY_STRING_VALUE)){
            label.setTitle(EMPTY_STRING_TITLE);
        }
        else{
		    label.setTitle(stringValue);
        }
	}

	/**
	 * Устанавливает правила переноса по словам в Label.
	 *
	 * @param wrap true - переносить по словам, false - не переносить.
	 */
	public void setLabelWordWrap(boolean wrap) {
		label.setWordWrap(wrap);
	}

	/**
	 * Возвращает правила переноса по словам в Label.
	 *
	 * @return true - есть перенос, false - нет переноса.
	 */
	public boolean getLabelWordWrap() {
		return label.getWordWrap();
	}

    @Override
    public void setVisible(boolean visible) {
        deckPanel.setVisible(visible);
    }
}
