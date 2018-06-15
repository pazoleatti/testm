package com.aplana.sbrf.taxaccounting.model;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

/**
 *  Класс для построения запроса и параметров к нему
 *  @author dloshkarev
 */
public class QueryBuilder {

	// Основной запрос на отбор сущностей. Не включает пэйджинг и сортировку
	private StringBuilder query = new StringBuilder();
	// Основной запрос + сортировка
	private String sortedQuery = null;
	// Основной запрос + пэйджинг + сортировка (если она была установлена ранее)
	private String pagedQuery = null;

    private MapSqlParameterSource namedParams = new MapSqlParameterSource();

    public QueryBuilder append(String s) {
        query.append(s);
        return this;
    }

    public void addNamedParam(String key, Object value){
        namedParams.addValue(key, value);
    }

    public QueryBuilder withSort(String sortColumn, String direction) {
        if (query.toString().isEmpty()) {
            throw new IllegalArgumentException("Main query cannot be empty!");
        }
        sortedQuery = "SELECT r.*, row_number() over (order by " + sortColumn + " " + direction + ") as rn FROM (\n" + query.toString() + ") r\n";
        return this;
    }

    public QueryBuilder withPaging(PagingParams pagingParams) {
        if (pagingParams != null) {
            addNamedParam("paging_start", pagingParams.getStartIndex() + 1);
            addNamedParam("paging_end", pagingParams.getStartIndex() + pagingParams.getCount());
            pagedQuery = "SELECT * FROM (\n" + (sortedQuery != null ? sortedQuery : query) + ") WHERE rn between :paging_start and :paging_end";
        } else {
            pagedQuery = sortedQuery != null ? sortedQuery : query.toString();
        }
        return this;
    }
    public String getSortedQuery() {
        return sortedQuery;
    }

    public String getPagedQuery() {
        return pagedQuery;
    }

    public MapSqlParameterSource getNamedParams() {
        return namedParams;
    }

    public String getCountQuery() {
        return "SELECT count(*) FROM (\n" + query.toString() + ")";
    }

}
