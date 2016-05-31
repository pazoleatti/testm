package com.aplana.gwt.client.dialog;

/**
 * Обработчик событий формы.
 *
 * @author ashatunov
 * @version 1.0
 */
public abstract class DialogHandler {

    /** Вызывается при нажатии на кнопку Да. */
    public void yes() {
    }

    /** Вызывается при нажатии на кнопку Нет. */
    public void no() {
    }

    /** Вызывается при нажатии на кнопку Отмена. */
    public void cancel() {
    }

    /** Вызывается при нажатии на кнопку OK. */
    public void ok() {
    }

    /** Вызывается при закрытии окна. */
    public void close() {
    }
}
