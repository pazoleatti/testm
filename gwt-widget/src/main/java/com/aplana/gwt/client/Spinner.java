package com.aplana.gwt.client;

import com.google.gwt.editor.client.LeafValueEditor;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.*;

import static com.google.gwt.dom.client.Style.Unit.PX;

/**
 * Спиннер для ввода целых чисел. Позволяет вводить целые числа непосредственно, как текст.
 * А так же при помощи кнопок инкремента и декремента.
 *
 * @author Vitaliy Samolovskikh
 */
public class Spinner extends DoubleStateComposite
		implements HasValue<Integer>, HasValueChangeHandlers<Integer>, LeafValueEditor<Integer> {
	/**
	 * Название класса стиял для компонента.
	 */
	public static final String STYLE_NAME = Constants.STYLE_PREFIX + "Spinner";

	/**
	 * Высота виджета по умолчанию в пикселях.
	 */
	public static final int DEFAULT_HEIGHT = 20;

	/**
	 * Ширина кнопок инкремента и декремента.
	 */
	public static final int BUTTON_SIZE = DEFAULT_HEIGHT / 2;

	/**
	 * Ширина виджета по умолчанию в пикселях.
	 */
	public static final int DEFAULT_WIDTH = 145;

	/**
	 * Основная панель, на которой размещаются все остальные элементы.
	 */
	private final LayoutPanel panel = new LayoutPanel();

	/**
	 * Поле для ввода значения непосредственно.
	 */
	private com.google.gwt.user.client.ui.TextBox textBox = new com.google.gwt.user.client.ui.TextBox();

	/**
	 * Кнопка для увеличения значения на 1.
	 */
	private Button incButton = new Button();

	/**
	 * Кнопка для уменьшения значения на 1.
	 */
	private Button decButton = new Button();

	/**
	 * Текущее значение спиннера.
	 */
	private Integer value = null;

	/**
	 * Минимальнодопустимое значение. По умолчанию Integer.MIN_VALUE.
	 */
	private int minValue = Integer.MIN_VALUE;

	/**
	 * Максимальнодопустимое значение. По умолчанию Integer.MAX_VALUE.
	 */
	private int maxValue = Integer.MAX_VALUE;

	/**
	 * Обработчики события изменения значения.
	 */
	private ValueChangeHandlerHolder<Integer> handlerHolder = new ValueChangeHandlerHolder<Integer>();

	/**
	 * Создает спиннер со значение по умоляанию 0.
	 */
	public Spinner() {
		initLayout();
		initStyles();
		initBehavior();
	}

    EnterKeyPressHandler handler;

	/**
	 * Задает расположение элементов виджета.
	 */
	private void initLayout() {
		// Инициализируем панель, как основной виджет компонента.
		initWidget(panel);
		setHeight(DEFAULT_HEIGHT + "px");
		setWidth(DEFAULT_WIDTH + "px");

		// Устаналиваем текст слева.
		panel.add(textBox);
		panel.setWidgetLeftRight(textBox, 0, PX, BUTTON_SIZE, PX);
		panel.setWidgetTopBottom(textBox, 0, PX, 0, PX);

		// Кнопка инкремента справа вверху.
		panel.add(incButton);
		panel.setWidgetRightWidth(incButton, 0, PX, BUTTON_SIZE, PX);
		panel.setWidgetTopHeight(incButton, 0, PX, BUTTON_SIZE, PX);

		// Кнопка декремента справа внизу.
		panel.add(decButton);
		panel.setWidgetRightWidth(decButton, 0, PX, BUTTON_SIZE, PX);
		panel.setWidgetBottomHeight(decButton, 0, PX, BUTTON_SIZE, PX);
	}

	/**
	 * Задает поведение виджета.
	 */
	private void initBehavior() {
		textBox.addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				String stringValue = event.getValue();
				if (stringValue.matches("\\-?\\d+")) {
					// Если введено число
					setValue(Integer.decode(stringValue), true);
				} else if (stringValue.trim().isEmpty()) {
					// Устанавливаем пустое значение
					setValue(null, true);
				} else {
					// Если введено не число, а какая-то ерунда.
					textBox.setValue(value == null ? "" : String.valueOf(value));
				}
			}
		});
		textBox.addFocusHandler(new FocusHandler() {
			@Override
			public void onFocus(FocusEvent event) {
				textBox.setSelectionRange(0, textBox.getText().length());
			}
		});
		textBox.addKeyPressHandler(new KeyPressHandler() {
			@Override
			public void onKeyPress(KeyPressEvent event) {
				int key = event.getNativeEvent().getKeyCode();
				if (key == KeyCodes.KEY_UP) {
					incrementValue();
				} else if (key == KeyCodes.KEY_DOWN) {
					decrementValue();
				} else if (key == KeyCodes.KEY_ENTER && handler != null) {
                    // Magic! Скидываем фокус, чтобы отработал ValueChangeHandler
                    textBox.setFocus(false);
                    textBox.setFocus(true);
                    handler.onEnterKeyPress();
                }
			}
		});
		incButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				incrementValue();
			}
		});
		decButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				decrementValue();
			}
		});

		this.addAttachHandler(new AttachEvent.Handler() {
			@Override
			public void onAttachOrDetach(AttachEvent event) {
				incButton.setTabIndex(-1);
				decButton.setTabIndex(-1);
			}
		});
	}

	private void decrementValue() {
		Integer value1 = getValue();
		if (value1 == null) {
			value1 = 0;
		}
		setValue(value1 - 1, true);
	}

	private void incrementValue() {
		Integer value1 = getValue();
		if (value1 == null) {
			value1 = 0;
		}
		setValue(value1 + 1, true);
	}

	/**
	 * Задает название стилей виджета.
	 */
	private void initStyles() {
		setStyleName(STYLE_NAME);
		panel.addStyleName(STYLE_NAME + "-panel");
		textBox.setStyleName(STYLE_NAME + "-textBox");
		incButton.setStyleName(STYLE_NAME + "-incButton");
		decButton.setStyleName(STYLE_NAME + "-decButton");

		incButton.addStyleName(STYLE_NAME + "-button");
		decButton.addStyleName(STYLE_NAME + "-button");
	}

	/**
	 * @return текущее значение
	 */
	@Override
	public final Integer getValue() {
		return value;
	}

	/**
	 * Устанавливает новое значение виджета, без бызова событий.
	 *
	 * @param value новое значение
	 */
	@Override
	public final void setValue(Integer value) {
		if (value == null) {
			this.value = null;
		} else if (minValue <= value && value <= maxValue) {
			this.value = value;
		} else if (value < minValue) {
			this.value = minValue;
		} else if (maxValue < value) {
			this.value = maxValue;
		}

		setLabelValue(value);

		if (value != null) {
			textBox.setValue(String.valueOf(this.value));
		} else {
			textBox.setValue(null);
		}
	}

	/**
	 * Устанавливает новое значение виджета. Вызывает событие ValueChangeEvent если fireEvents==true.
	 *
	 * @param value      новое значение
	 * @param fireEvents если true, то вызывается событие ValueChangeEvent,
	 *                   в противном случае никаких событий не вызывается.
	 */
	@Override
	public final void setValue(Integer value, boolean fireEvents) {
		setValue(value);
		if (fireEvents) {
			ValueChangeEvent.fire(this, value);
		}
	}

	/**
	 * Добавляет обработчик события изменения значения.
	 *
	 * @param handler обработчик события
	 * @return регистратор обработчика события
	 */
	@Override
	public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Integer> handler) {
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

	/**
	 * @return минимальнодопустимое значение
	 */
	public int getMinValue() {
		return minValue;
	}

	/**
	 * Устанавливает минимально допустимое значение.
	 *
	 * @param minValue минимально допустимое значение.
	 */
	public void setMinValue(int minValue) {
		this.minValue = minValue;
	}

	/**
	 * @return минимально допустимое значение.
	 */
	public int getMaxValue() {
		return maxValue;
	}

	/**
	 * устанавливает максимально дпоустимое значение
	 *
	 * @param maxValue максимально допустимое значение
	 */
	public void setMaxValue(int maxValue) {
		this.maxValue = maxValue;
	}

    @Override
    public void setTitle(String title) {
        panel.setTitle(title);
    }

    @Override
    public String getTitle() {
        return panel.getTitle();
    }

    public void addEnterKeyPressHandler(EnterKeyPressHandler handler) {
        this.handler = handler;
    }

    public interface EnterKeyPressHandler {
        void onEnterKeyPress();
    }
}