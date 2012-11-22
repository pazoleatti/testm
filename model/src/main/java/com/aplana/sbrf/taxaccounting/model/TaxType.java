package com.aplana.sbrf.taxaccounting.model;

/**
 * Типы налогов
 * TODO: возможно в будущем стоит переделать в обычный класс и создать таблицу в БД
 */
public enum TaxType {
	/**
	 * Налог на прибыль
	 */
	INCOME('I', "Налог на прибыль"),
	/**
	 * Налог на имущество
	 */
	PROPERTY('P', "Налог на имущество"),
	/**
	 * Транспортный налог
	 */
	TRANSPORT('T', "Транспортный налог"),
	/**
	 * НДС
	 */
	VAT('V', "НДС");
	
	private final char code;
	private final String name;
	
	private TaxType(char code, String name) {
		this.code = code;
		this.name = name;
	}
	
	/**
	 * Получить код вида налога (используется в БД)
	 */
	public char getCode() {
		return code;
	}
	
	/**
	 * Получить название вида налога
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Получить объект по символьному коду
	 * @param code код вида налога
	 * @return объект, представляющий вид налога
	 * @throws IllegalArgumentException если не существет вида налога с заданным кодом 
	 */
	public static TaxType fromCode(char code) {
		for (TaxType t: values()) {
			if (code == t.getCode()) {
				return t;
			}
		}
		throw new IllegalArgumentException("Wrong TaxType code: '" + code + "'");
	}
}
