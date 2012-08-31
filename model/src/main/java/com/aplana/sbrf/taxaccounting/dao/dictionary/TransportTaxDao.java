package com.aplana.sbrf.taxaccounting.dao.dictionary;

public interface TransportTaxDao {
	/**
	 * Возращает имя муниципального отделения по коду ОКАТО 
	 * @param okato код ОКАТО
	 * @return имя региона, или null, если такого ОКАТО нет в справочнике
	 */
	String getRegionName(String okato);
	/**
	 * Возвращает значение ставки транспортного налога 
	 * @param code код типа транспортного средства
	 * @param age срок использования в годах
	 * @param power мощность в л.с.
	 * @return значение ставки налога в рублях
	 */
	int getTaxRate(String code, int age, int power);
}
