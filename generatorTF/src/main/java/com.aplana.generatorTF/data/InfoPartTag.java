package com.aplana.generatorTF.data;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Класс, объекты которого хранят атрибуты тега ИнфЧасть и вложенных в него тегов: ПолучДоч и СведОпер
 */
public class InfoPartTag {
    /**
     * Атрибуты тега ИнфЧасть
     */
    private Map<String, String> infoPartTagAttributes;

    /**
     * Атрибуты тега ПолучДоч
     */
    private Map<String, String> incomeTagAttributes;

    /**
     * Список объектов, хранящих атрибуты тега СведОпер и вложенных в него тегов
     */
    private List<OperationInfoTag> operationInfoTags;

    /**
     * Конструктор
     *
     * @param infoPartTagAttributes Атрибуты тега ИнфЧасть
     */
    public InfoPartTag(Map<String, String> infoPartTagAttributes) {
        this.incomeTagAttributes = new LinkedHashMap<String, String>();
        this.infoPartTagAttributes = infoPartTagAttributes;
        this.operationInfoTags = new LinkedList<OperationInfoTag>();
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
     * Получить атрибуты тега ПолучДоч
     *
     * @return Атрибуты тега ПолучДоч
     */
    public Map<String, String> getIncomeTagAttributes() {
        return incomeTagAttributes;
    }

    /**
     * Сохранить атрибуты тега ПолучДох
     *
     * @param incomeTagAttributes Атрибуты тега ПолучДох
     */
    public void setIncomeTagAttributes(Map<String, String> incomeTagAttributes) {
        this.incomeTagAttributes.clear();
        for (Map.Entry<String, String> attr : incomeTagAttributes.entrySet()) {
            this.incomeTagAttributes.put(attr.getKey(), attr.getValue());
        }
    }

    /**
     * Добавить объект, хранящий атрибуты тега СведОпер и вложенных тегов, в список
     *
     * @param operationInfoTag Объект, хранящий атрибуты тега СведОпер и вложенных тегов
     */
    public void addOperationInfoTag(OperationInfoTag operationInfoTag) {
        operationInfoTags.add(operationInfoTag);
    }

    /**
     * Получить список объектов, хранящих атрибуты тега СведОпер и вложенных тегов
     *
     * @return Список объектов, хранящих атрибуты тега СведОпер и вложенных тегов
     */
    public List<OperationInfoTag> getOperationInfoTags() {
        return operationInfoTags;
    }
}
