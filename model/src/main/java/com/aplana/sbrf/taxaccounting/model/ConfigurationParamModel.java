package com.aplana.sbrf.taxaccounting.model;

import java.io.Serializable;
import java.util.*;

/**
 * Сущность для хранения конфигурационных параметров
 * @author Dmitriy Levykin
 */
public class ConfigurationParamModel implements Map<ConfigurationParam, Map<Integer, List<String>>>, Serializable {

    // Хранилище значений
    private Map<ConfigurationParam, Map<Integer, List<String>>> map = new HashMap<ConfigurationParam, Map<Integer, List<String>>>();

    public static final String EMAIL_NAME_ATTRIBUTE = "NAME";
    public static final String EMAIL_VALUE_ATTRIBUTE = "VALUE";
    public static final String EMAIL_DESCRIPTION_ATTRIBUTE = "DESCRIPTION";

    public static final String ASYNC_TYPE_ID = "ID";
    public static final String ASYNC_TYPE = "NAME";
    public static final String ASYNC_LIMIT_KIND = "LIMIT_KIND";
    public static final String ASYNC_LIMIT = "TASK_LIMIT";
    public static final String ASYNC_SHORT_LIMIT = "SHORT_QUEUE_LIMIT";

    private static final String SPLITTER = ";";

    /**
     * Получение объединенных значений из списка
     */
    public String getFullStringValue(ConfigurationParam key, int departmentId) {
        Map<Integer, List<String>> listMap = get(key);
        if (listMap == null || listMap.isEmpty() || listMap.get(departmentId) == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (String str : listMap.get(departmentId)) {
            sb.append(str).append(SPLITTER);
        }
        return sb.toString().trim();
    }

    /**
     * Задание списка значения одной строкой, содержащей несколько строк
     */
    public void setFullStringValue(ConfigurationParam key, int departmentId, String value) {
        if (value == null) {
            put(key, null);
            return;
        }
        String values[] = value.trim().split(SPLITTER);
        List<String> resultList = new ArrayList<String>();
        for (String str : values) {
            String trimVal = str.trim();
            if (!trimVal.isEmpty()) {
                resultList.add(trimVal);
            }
        }
        put(key, departmentId, resultList);
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    public boolean containsKey(ConfigurationParam param, int departmentId) {
        Map<Integer, List<String>> departmenMap = map.get(param);
        if (departmenMap == null) {
            return false;
        }
        return departmenMap.containsKey(departmentId);
    }

    @Override
    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    @Override
    public Map<Integer, List<String>> get(Object key) {
        return map.get(key);
    }

    public List<String> get(Object key, int departmentId) {
        if (map.get(key) == null) {
            return null;
        }
        return map.get(key).get(departmentId);
    }

    @Override
    public Map<Integer, List<String>> put(ConfigurationParam key, Map<Integer, List<String>> value) {
        return map.put(key, value);
    }

    public List<String> put(ConfigurationParam key, int departmentId, List<String> value) {
        if (get(key) == null) {
            put(key, new HashMap<Integer, List<String>>());
        }
        return get(key).put(departmentId, value);
    }

    @Override
    public Map<Integer, List<String>> remove(Object key) {
        return map.remove(key);
    }

    @Override
    public void putAll(Map<? extends ConfigurationParam, ? extends Map<Integer, List<String>>> m) {
        map.putAll(m);
    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public Set<ConfigurationParam> keySet() {
        return map.keySet();
    }

    @Override
    public Collection<Map<Integer, List<String>>> values() {
        return map.values();
    }

    @Override
    public Set<Entry<ConfigurationParam, Map<Integer, List<String>>>> entrySet() {
        return map.entrySet();
    }
}