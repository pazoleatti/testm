package com.aplana.sbrf.taxaccounting.dao;

/**
 * Интерфейс тестового объета для дао
 * создан для целей тестирование кэша
 */
public interface DaoObject {
    /**
     * @return Возвращает случайное число
     */
    int getCachedNumberFromCachedMethod();

    int getCachedNumberFromPrivateInsideMethod(String key);
}
