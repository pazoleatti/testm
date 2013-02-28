package com.aplana.sbrf.taxaccounting.model;

/**
 * Типы налогов
 * TODO: возможно в будущем стоит переделать в обычный класс и создать таблицу в БД
 */
//TODO: Изменить префиксы для "Налог на имущество" и "НДС"
public enum TaxType {
	/**
	 * Налог на прибыль
	 */
	INCOME('I', "Налог на прибыль", "NO_PRIB"),
	/**
	 * Налог на имущество
	 */
	PROPERTY('P', "Налог на имущество", "NO_IM"),
	/**
	 * Транспортный налог
	 */
	TRANSPORT('T', "Транспортный налог", "NO_TRAND"),
	/**
	 * НДС
	 */
	VAT('V', "НДС", "NO_VAT");
	
	private final char code;
	private final String name;
	private final String declarationPrefix;
	
	private TaxType(char code, String name, String declarationPrefix) {
		this.code = code;
		this.name = name;
		this.declarationPrefix = declarationPrefix;
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
	 * Получить значение префикса
	 */
	public String getDeclarationPrefix() {
		return declarationPrefix;
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
