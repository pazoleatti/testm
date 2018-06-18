package com.aplana.generators.data;


import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Класс, объекты которого хранят атрибуты тега ИнфЧасть для ТФ ФЛ и вложенных в него тегов: АнкетДаннФЛ, УдЛичнФЛ, СисИсточ, СведИстДох
 */
public class InfoPartFLTag {
    /**
     * Атрибуты тега ИнфЧасть
     */
    private Map<String, String> infoPartTagAttributes;

    /**
     * Атрибуты тега АнкетДаннФЛ
     */
    private Map<String, String> flDataTagAttributes;

    /**
     * Атрибуты тега УдЛичнФЛ
     */
    private Map<String, String> flDocTagAttributes;

    /**
     * Атрибуты тега СисИсточ
     */
    private Map<String, String> systemSourceTagAttributes;

    /**
     * Атрибуты тега СведИстДох
     */
    private Map<String, String> incomeSourceTagAttributes;


    /**
     * Конструктор
     *
     * @param infoPartTagAttributes Атрибуты тега ИнфЧасть
     */
    public InfoPartFLTag(Map<String, String> infoPartTagAttributes) {
        this.flDataTagAttributes = new LinkedHashMap<String, String>();
        this.flDocTagAttributes = new LinkedHashMap<String, String>();
        this.systemSourceTagAttributes = new LinkedHashMap<String, String>();
        this.incomeSourceTagAttributes = new LinkedHashMap<String, String>();
        this.infoPartTagAttributes = infoPartTagAttributes;
    }

    /**
     * Получить атрибуты тега ИнфЧасть
     *
     * @return Атрибуты тега ИнфЧасть
     */
    public Map<String, String> getInfoPartTagAttributes() {
        return infoPartTagAttributes;
    }

    /**
     * Получить атрибуты тега АнкетДаннФЛ
     *
     * @return Атрибуты тега АнкетДаннФЛ
     */
    public Map<String, String> getFlDataTagAttributes() {
        return flDataTagAttributes;
    }

    /**
     * Сохранить атрибуты тега АнкетДаннФЛ
     *
     * @param flDataTagAttributes Атрибуты тега АнкетДаннФЛ
     */
    public void setFlDataTagAttributes(Map<String, String> flDataTagAttributes) {
        this.flDataTagAttributes.clear();
        for (Map.Entry<String, String> attr : flDataTagAttributes.entrySet()) {
            this.flDataTagAttributes.put(attr.getKey(), attr.getValue());
        }
    }

    public void setInfoPartTagAttributes(Map<String, String> infoPartTagAttributes) {
        this.infoPartTagAttributes = infoPartTagAttributes;
    }

    /**
     * Получить атрибуты тега ИнфЧасть
     *
     * @return Атрибуты тега ИнфЧасть
     */
    public Map<String, String> getFlDocTagAttributes() {
        return flDocTagAttributes;
    }

    /**
     * Сохранить атрибуты тега УдЛичнФЛ
     *
     * @param flDocTagAttributes Атрибуты тега УдЛичнФЛ
     */
    public void setFlDocTagAttributes(Map<String, String> flDocTagAttributes) {
        this.flDocTagAttributes.clear();
        for (Map.Entry<String, String> attr : flDocTagAttributes.entrySet()) {
            this.flDocTagAttributes.put(attr.getKey(), attr.getValue());
        }
    }

    /**
     * Получить атрибуты тега ИнфЧасть
     *
     * @return Атрибуты тега ИнфЧасть
     */
    public Map<String, String> getSystemSourceTagAttributes() {
        return systemSourceTagAttributes;
    }

    /**
     * Сохранить атрибуты тега СисИсточ
     *
     * @param systemSourceTagAttributes Атрибуты тега СисИсточ
     */
    public void setSystemSourceTagAttributes(Map<String, String> systemSourceTagAttributes) {
        this.systemSourceTagAttributes.clear();
        for (Map.Entry<String, String> attr : systemSourceTagAttributes.entrySet()) {
            this.systemSourceTagAttributes.put(attr.getKey(), attr.getValue());
        }
    }

    /**
     * Получить атрибуты тега ИнфЧасть
     *
     * @return Атрибуты тега ИнфЧасть
     */
    public Map<String, String> getIncomeSourceTagAttributes() {
        return incomeSourceTagAttributes;
    }

    /**
     * Сохранить атрибуты тега СведИстДох
     *
     * @param incomeSourceTagAttributes Атрибуты тега СведИстДох
     */
    public void setIncomeSourceTagAttributes(Map<String, String> incomeSourceTagAttributes) {
        this.incomeSourceTagAttributes.clear();
        for (Map.Entry<String, String> attr : incomeSourceTagAttributes.entrySet()) {
            this.incomeSourceTagAttributes.put(attr.getKey(), attr.getValue());
        }
    }
}
