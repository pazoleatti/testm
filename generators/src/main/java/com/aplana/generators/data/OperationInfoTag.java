package com.aplana.generators.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Класс, объекты которого хранят атрибуты тега СведОпер и вложенных в него тегов: СведДохНал, СведВыч, СведАванс
 */
public class OperationInfoTag {

    /**
     * Атрибуты тега СведОпер
     */
    private Map<String, String> operationInfoTagAttributes;

    /**
     * Список атрибутов тегов СведВыч. Каждый элемент списка - набор атрибутов 1 тега
     */
    private List<Map<String, String>> incomeTaxInfoTagsAttributesList;

    /**
     * Список атрибутов тегов СведДохНал. Каждый элемент списка - набор атрибутов 1 тега
     */
    private List<Map<String, String>> deductionInfoTagsAttributesList;

    /**
     * Список атрибутов тегов СведАванс. Каждый элемент списка - набор атрибутов 1 тега
     */
    private List<Map<String, String>> prepaymentInfoTagsAttributesList;

    /**
     * Конструктор
     *
     * @param operationInfoTagAttributes Атрибуты тега СведОпер
     */
    public OperationInfoTag(Map<String, String> operationInfoTagAttributes) {
        this.operationInfoTagAttributes = operationInfoTagAttributes;
        incomeTaxInfoTagsAttributesList = new ArrayList<Map<String, String>>();
        deductionInfoTagsAttributesList = new ArrayList<Map<String, String>>();
        prepaymentInfoTagsAttributesList = new ArrayList<Map<String, String>>();
    }

    /**
     * Добавить в список атрибуты тега СведДохНал
     *
     * @param attributes Атрибуты тега
     */
    public void addIncomeTaxInfoTagAttributes(Map<String, String> attributes) {
        incomeTaxInfoTagsAttributesList.add(attributes);
    }

    /**
     * Добавить в список атрибуты тега СведВыч
     *
     * @param attributes Атрибуты тега
     */
    public void addDeductionInfoTagAttributes(Map<String, String> attributes) {
        deductionInfoTagsAttributesList.add(attributes);
    }

    /**
     * Добавить в список атрибуты тега СведАванс
     *
     * @param attributes Атрибуты тега
     */
    public void addPrepaymentInfoTagAttributes(Map<String, String> attributes) {
        prepaymentInfoTagsAttributesList.add(attributes);
    }

    /**
     * Получить атрибуты тега СведОпер
     *
     * @return Атрибуты тега СведОпер
     */
    public Map<String, String> getOperationInfoTagAttributes() {
        return operationInfoTagAttributes;
    }

    /**
     * Получить список атрибутов тегов СведВыч, аждый элемент списка - набор атрибутов 1 тега
     *
     * @return Список атрибутов тегов СведВыч
     */
    public List<Map<String, String>> getIncomeTaxInfoTagsAttributesList() {
        return incomeTaxInfoTagsAttributesList;
    }

    /**
     * Получить список атрибутов тегов СведДохНал. Каждый элемент списка - набор атрибутов 1 тега
     *
     * @return Список атрибутов тегов СведДохНал
     */
    public List<Map<String, String>> getDeductionInfoTagsAttributesList() {
        return deductionInfoTagsAttributesList;
    }

    /**
     * Получить список атрибутов тегов СведАванс. Каждый элемент списка - набор атрибутов 1 тега
     *
     * @return Список атрибутов тегов СведАванс
     */
    public List<Map<String, String>> getPrepaymentInfoTagsAttributesList() {
        return prepaymentInfoTagsAttributesList;
    }
}
