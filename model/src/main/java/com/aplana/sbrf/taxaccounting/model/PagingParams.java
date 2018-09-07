package com.aplana.sbrf.taxaccounting.model;

import java.io.Serializable;

/**
 * Класс, содержащий параметры разбивки списка записей на страницы
 */
public class PagingParams implements Serializable {
    private static final long serialVersionUID = 5113425251692266554L;

    /**
     * Поле сортировки
     */
    private String property;

    /**
     * Направление сортировки
     */
    private String direction;

    /**
     * Номер страницы. По умолчанию = 1
     */
    private int page = 1;

    /**
     * Количество возвращаемых элементов, начиная с индекса startIndex. По умолчанию = 10
     */
    private int count = 10;

    public PagingParams() {
    }

    public PagingParams(String property, String direction) {
        this.property = property;
        this.direction = direction;
    }

    @Deprecated
    // Убрано поле startIndex, поэтому вместо этого конструктора лучше использовать статический метод getInstance
    public PagingParams(int startIndex, int count) {
        setCount(count);
        setStartIndex(startIndex);
    }

    /**
     * Возвращает новый экземпляр PagingParams
     */
    public static PagingParams getInstance(int page, int count) {
        PagingParams toReturn = new PagingParams();
        toReturn.setPage(page);
        toReturn.setCount(count);
        return toReturn;
    }

    /**
     * Получить стартовый индекс
     *
     * @return стартовый индекс списка записей (начиная с 0)
     */
    public int getStartIndex() {
        return count * (page - 1);
    }

    /**
     * Получить количество записей, которые нужно вернуть (может быть возвращено меньше)
     *
     * @return количество записей (по умолчанию - значение 10)
     */
    public Integer getCount() {
        return count;
    }

    /**
     * Поскольку удалено поле startIndex метод сделал deprecated
     * Метод устанавливает номер страницы в зависимости от количества возвращаемых объектов и стартового индекса.
     *
     * @param startIndex стартовый индекс записи (начиная с 0)
     */
    @Deprecated
    public final void setStartIndex(int startIndex) {
        this.page = (int) Math.ceil((startIndex + count) / count);
    }

    /**
     * Задать количество записей, которые нужно вернуть (может быть возвращено меньше)
     *
     * @param count количество записей (по умолчанию - значение 10)
     */
    public final void setCount(Integer count) {
        this.count = count;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    @Override
    public String toString() {
        return "PagingParams [count=" + count + ", page=" + page + ", property=" + property + ", direction=" + direction + "]";
    }
}
