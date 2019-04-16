package com.aplana.sbrf.taxaccounting.model;

import lombok.Data;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import java.io.Serializable;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * Класс, содержащий параметры разбивки списка записей на страницы
 */
@Data
public class PagingParams implements Serializable {
    private static final long serialVersionUID = 5113425251692266554L;

    /**
     * Поле сортировки
     */
    private String property;

    /**
     * Направление сортировки
     */
    private String direction = "ASC";

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
     * Возвращает новый экземпляр PagingParams
     */
    public static PagingParams getInstance(int page, int count, String property, String direction) {
        PagingParams toReturn = new PagingParams();
        toReturn.setPage(page);
        toReturn.setCount(count);
        toReturn.setProperty(property);
        toReturn.setDirection(direction);
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
     * Возвращяет true, если пагинация не задана и значит надо возвращять все записи с сортировкой, иначе - false
     */
    public boolean isNoPaging() {
        return count == -1;
    }

    /**
     * Устанавливает что пагинация не задана и значит надо возвращять все записи с сортировкой
     */
    public void setNoPaging() {
        count = -1;
    }

    /**
     * Оборачивает основной запрос в запрос, добавляя пагинацию и сортировку
     *
     * @param query  основной запрос без пагинации и сортировки
     * @param params параметры запроса
     * @return запрос с пагинацией и сортировкой
     */
    public String wrapQuery(String query, MapSqlParameterSource params) {
        if (isNotBlank(property)) {
            query = query + " order by " + property + " " + direction + ", id";
        } else {
            query = query + " order by id";
        }
        if (isNoPaging()) {
            return query;
        } else {
            params.addValue("startIndex", getStartIndex());
            params.addValue("count", count);
            return "select * \n" +
                    "from ( \n" +
                    "   select a.*, rownum rn \n" +
                    "   from ( \n" + query + ") a \n" +
                    ") \n" +
                    "where rn > :startIndex and rownum <= :count";
        }
    }
}
