package com.aplana.sbrf.taxaccounting.model;

import java.util.*;

/**
 * Сущность для хранения конфигурационных параметров
 * @author Dmitriy Levykin
 */
public class ConfigurationParamModel implements Map<ConfigurationParam, List<String>> {

    // Хранилище значений
    private Map<ConfigurationParam, List<String>> map = new HashMap<ConfigurationParam, List<String>>();

    private static String SPLITTER = "\n";

    /**
     * Получение объединенных значений из списка
     */
    public String getFullStringValue(ConfigurationParam key) {
        List<String> list = get(key);
        if (list == null || list.isEmpty()) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (String str : list) {
            sb.append(str).append(SPLITTER);
        }
        return sb.toString().trim();
    }

    /**
     * Задание списка значения одной строкой, содержащей несколько строк
     */
    public void setFullStringValue(ConfigurationParam key, String value) {
        if (value == null) {
            map.put(key, null);
            return;
        }

        String values[] = value.trim().split(SPLITTER);
        List<String> resultList = new LinkedList<String>();

        for (String str : values) {
            String trimVal = str.trim();
            if (!trimVal.isEmpty()) {
                resultList.add(trimVal);
            }
        }

        map.put(key, new ArrayList<String>(resultList));
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

    @Override
    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    @Override
    public List<String> get(Object key) {
        return map.get(key);
    }

    @Override
    public List<String> put(ConfigurationParam key, List<String> value) {
        return map.put(key, value);
    }

    @Override
    public List<String> remove(Object key) {
        return map.remove(key);
    }

    @Override
    public void putAll(Map<? extends ConfigurationParam, ? extends List<String>> m) {
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
    public Collection<List<String>> values() {
        return map.values();
    }

    @Override
    public Set<Entry<ConfigurationParam, List<String>>> entrySet() {
        return map.entrySet();
    }
}
