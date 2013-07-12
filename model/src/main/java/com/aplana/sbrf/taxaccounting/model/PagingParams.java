package com.aplana.sbrf.taxaccounting.model;

/**
 * Класс, содержащий параметры разбивки списка записей на страницы
 */
public class PagingParams {
	private int startIndex = 0;
	private int count = 10;
	
	public PagingParams() {
		
	}
	
	public PagingParams(int startIndex, int count) {
		this.startIndex = startIndex;
		this.count = count;
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
}
