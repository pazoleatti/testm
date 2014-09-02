package com.aplana.sbrf.taxaccounting.model;

import java.io.Serializable;

/**
 * Класс, содержащий параметры разбивки списка записей на страницы
 */
public class PagingParams implements Serializable{
	private static final long serialVersionUID = 5113425251692266554L;

	/**
	 * Начальный номер строки, с которой должен быть возвращен результат. По умолчанию = 0
	 */
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
	 * @return стартовый индекс списка записей (начиная с 0)
	 */
	public int getStartIndex() {
		return startIndex;
	}
	
	/**
	 * Задать стартовый индекс
	 * @param startIndex стартовый индекс записи (начиная с 0)
	 */
	public void setStartIndex(int startIndex) {
		this.startIndex = startIndex;
	}
	
	/**
	 * Получить количество записей, которые нужно вернуть (может быть возвращено меньше)
	 * @return количество записей (по умолчанию - значение 10)
	 */
	public Integer getCount() {
		return count;
	}
	
	/**
	 * Задать количество записей, которые нужно вернуть (может быть возвращено меньше)
	 * @param count количество записей (по умолчанию - значение 10)
	 */
	public void setCount(Integer count) {
		this.count = count;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("PagingParams [startIndex=");
		builder.append(startIndex);
		builder.append(", count=");
		builder.append(count);
		builder.append("]");
		return builder.toString();
	}
	
	
}
