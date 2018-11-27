package com.aplana.sbrf.taxaccounting.async;

/**
 * Содержит функционал проверяющий ограничения на выполнение асинхронной задачи
 */
//TODO: проработать аргументы методов
public interface AsyncTaskExecutePossibilityVerifier {
    /**
     * Проверяет на основе параметров асинхронных задач ограничение на возможност выполнения задачи
     * @return {@code true} если выполнение возможно
     */
    boolean canExecuteByLimit();

    /**
     * Создает сообщение об ошибке при невозможности выполнения задачи
     * @return созданное сообщение
     */
    String createExecuteByLimitErrorMessage();
}
