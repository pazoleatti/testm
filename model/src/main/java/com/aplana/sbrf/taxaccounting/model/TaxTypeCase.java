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
    DEAL('D', "Учет контролируемых сделок", "Учета контролируемых сделок", "Учету контролируемых сделок", "Учет контролируемых сделок", "Учетом контролируемых сделок", "учете контролируемых сделок"),
    /**
     * НДС
     */
    VAT('V', "НДС", "НДС", "НДС", "НДС", "НДС", "НДС"),
    /**
     * Налог на имущество
     */
    PROPERTY('P', "Налог на имущество", "Налога на имущество", "Налогу на имущество", "Налог на имущество", "Налогом на имущество", "Налоге на имущество"),
    /**
     * Рыночные интервалы ТЦО
     */
    MARKET('M', "Рыночные интервалы ТЦО", "Рыночных интервалов ТЦО", "Рыночным интервалам ТЦО", "Рыночные интервалы ТЦО", "Рыночными интервалами ТЦО", "Рыночных интервалах ТЦО"),
    /**
     * НДС
     */
    ETR('E', "Эффективная налоговая ставка", "Эффективной налоговой ставки", "Эффективной налоговой ставке", "Эффективную налоговую ставку", "Эффективной налоговой ставкой", "Эффективной налоговой ставке"),
    /**
     * НДС
     */
    LAND('L', "Земельный налог", "Земельного налога", "Земельному налогу", "Земельный налог", "Земельным налогом", "Земельном налоге");

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
