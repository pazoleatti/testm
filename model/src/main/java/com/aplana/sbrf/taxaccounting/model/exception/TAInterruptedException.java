package com.aplana.sbrf.taxaccounting.model.exception;

/**
 * Класс-исключение, используется, когда происходит превывание потока.

 */
public class TAInterruptedException extends TAException {
    private static final long serialVersionUID = 1L;

    public TAInterruptedException() {
        super("Выполнение задачи было прервано.");
    }
}
