package com.aplana.sbrf.taxaccounting.model;

/**
 * Типы налогов
 */
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
	VAT('V', "НДС", "NO_NDS"),
	/**
	 * УКС
	 */
	DEAL('D', "Учет контролируемых сделок", "UT_UVKNRSD"),
    /**
     * ЭНС
     */
    ETR('E', "Эффективная налоговая ставка", ""),
    /**
     * Земельный налог
     */
    LAND('L', "Земельный налог", "NO_ZEMND"),
    /**
     * НДФЛ
     */
    NDFL('N', "НДФЛ", "NO_NDFL"),
    /**
     * Фонды и Сборы
     */
    PFR('F', "Страховые сборы, взносы", "NO_PFR");

	private final char code;
	private final String name;
	private final String declarationPrefix;
	
	TaxType(char code, String name, String declarationPrefix) {
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

    public String getDeclarationShortName() {
        if (this.equals(DEAL)) {
            return "уведомления";
        } else {
            return "налоговой формы";
        }
    }

	public boolean isTax() {
		//TODO удалить метод
		return true;
	}
}
