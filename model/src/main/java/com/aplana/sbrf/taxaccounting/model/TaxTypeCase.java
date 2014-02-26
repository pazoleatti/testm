package com.aplana.sbrf.taxaccounting.model;

/**
 * Типы налогов в падежах
 */
public enum TaxTypeCase {
    /**
     * Налог на прибыль
     */
    INCOME('I', "Налог на прибыль", "Налога на прибыль", "Налогу на прибыль", "Налог на прибыль", "Налогом на прибыль", "Налоге на прибль"),
    /**
     * Транспортный налог
     */
    TRANSPORT('T', "Транспортный налог", "Транспортного налога", "Транспортному налогу", "Транспортный налог", "Транспортным налогом", "Траснпортном налоге"),
    /**
     * УКС
     */
    DEAL('D', "Учет контролируемых сделок", "Учета контролируемых сделок", "Учету контролируемых сделок", "Учет контролируемых сделок", "Учетом контролируемых сделок", "учете контролируемых сделок");

    private final char code;
    /* Именительный падеж */
    private final String nominative;
    /* Родительный падеж */
    private final String genitive;
    /* Дательный падеж */
    private final String dative;
    /* Винительный падеж */
    private final String accusative;
    /* Творительный падеж */
    private final String ablative;
    /* Предложный падеж */
    private final String prepositional;

    TaxTypeCase(char code, String nominative, String genitive, String dative, String accusative, String ablative, String prepositional) {
        this.code = code;
        this.nominative = nominative;
        this.genitive = genitive;
        this.dative = dative;
        this.accusative = accusative;
        this.ablative = ablative;
        this.prepositional = prepositional;
    }

    public char getCode() {
        return code;
    }

    public String getNominative() {
        return nominative;
    }

    public String getGenitive() {
        return genitive;
    }

    public String getDative() {
        return dative;
    }

    public String getAccusative() {
        return accusative;
    }

    public String getAblative() {
        return ablative;
    }

    public String getPrepositional() {
        return prepositional;
    }

    /**
     * Получить объект по символьному коду
     * @param code код вида налога
     * @return объект, представляющий вид налога
     * @throws IllegalArgumentException если не существет вида налога с заданным кодом
     */
    public static TaxTypeCase fromCode(char code) {
        for (TaxTypeCase t: values()) {
            if (code == t.getCode()) {
                return t;
            }
        }
        throw new IllegalArgumentException("Wrong TaxType code: '" + code + "'");
    }
}
