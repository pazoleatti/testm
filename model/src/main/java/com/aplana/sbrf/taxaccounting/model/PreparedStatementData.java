package com.aplana.sbrf.taxaccounting.model;

import java.util.ArrayList;
import java.util.List;

/**
 *  Модель содержащая данные для
 *  подготовки sql запроса
 *  preparedstatement
 */
public class PreparedStatementData {

	// запрос
	private StringBuilder query = new StringBuilder();

	// часть с join'ами
    private String joinPartsOfQuery;

	// набор параметров
	private List<Object> params = new ArrayList<Object>();

	public PreparedStatementData(){
	}

	public PreparedStatementData(String query, Object... params) {
		this.query.append(query);
		this.params.add(params);
	}

    public void setQuery(StringBuilder query){
        this.query = query;
    }

    public StringBuilder getQuery(){
        return query;
    }

    public void setParams(List<Object> params){
        this.params = params;
    }

    public List<Object> getParams(){
        return params;
    }

    public void addParam(Object object){
        params.add(object);
    }

    public void addParam(List<Object> objects){
        params.addAll(objects);
    }

    public void appendQuery(String query){
        this.query.append(query);
    }

    public void setJoinPartsOfQuery(String joinPartsOfQuery){
        this.joinPartsOfQuery = joinPartsOfQuery;
    }

    public String getJoinPartsOfQuery(){
        return joinPartsOfQuery;
    }

    @Override
    public String toString() {
        return "PreparedStatementData{" +
                "query=" + query +
                ", joinPartsOfQuery='" + joinPartsOfQuery + '\'' +
                ", params=" + params +
                '}';
    }
}
