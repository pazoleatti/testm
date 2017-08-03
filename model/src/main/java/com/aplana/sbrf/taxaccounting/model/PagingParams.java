package com.aplana.sbrf.taxaccounting.model;

import org.springframework.data.domain.Sort;

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
     * Начальный номер строки, с которой должен быть возвращен результат. По умолчанию = 0
     */
    //TODO https://jira.aplana.com/browse/SBRFNDFL-1668 удалить поле
    private int startIndex = 0;
    /**
     * Количество возвращаемых элементов, начиная с индекса startIndex. По умолчанию = 10
     */
    private int count = 10;

    public PagingParams() {
    }

    public PagingParams(int startIndex, int count) {
        setStartIndex(startIndex);
        setCount(count);
    }

    /**
     * Получить стартовый индекс
     *
     * @return стартовый индекс списка записей (начиная с 0)
     */
    public int getStartIndex() {
        return startIndex;
    }

    /**
     * Задать стартовый индекс
     *
     * @param startIndex стартовый индекс записи (начиная с 0)
     */
    public final void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
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
        StringBuilder builder = new StringBuilder();
        builder.append("PagingParams [count=");
        builder.append(count);
        builder.append(", page=");
        builder.append(page);
        builder.append(", property=");
        builder.append(property);
        builder.append(", direction=");
        builder.append(direction);
        builder.append("]");
        return builder.toString();
    }

    public Sort getSort() {
        Sort.Order order = new Sort.Order(direction.equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, property);
        return new Sort(order);
    }
}
