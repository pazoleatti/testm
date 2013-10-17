package com.aplana.sbrf.taxaccounting.scheduler.core.utils;

import com.google.gson.Gson;
import com.aplana.sbrf.taxaccounting.scheduler.api.entity.TaskContext;
import com.aplana.sbrf.taxaccounting.scheduler.api.entity.TaskParam;

import java.io.*;
import java.util.*;

/**
 * Утилитный класс для работы с планировщиком
 *
 * @author dloshkarev
 */
public class TaskUtils {

    /**
     * Сериализация параметров задачи для сохранения в бд
     *
     * @param params параметры задачи
     * @return сериализованные параметры
     * @throws java.io.IOException
     */
    public static byte[] serializeParams(Map<String, TaskParam> params) throws IOException {
        //Сортировка параметров по id для правильного отображения
        Map<String, TaskParam> sortedParams = sortById(params);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out = null;
        try {
            out = new ObjectOutputStream(bos);
            out.writeObject(sortedParams);
            return bos.toByteArray();
        } finally {
            if (out != null) {
                out.close();
            }
            bos.close();
        }
    }

    private static <K, V extends Comparable<? super V>> Map<K, V> sortById(Map<K, V> map) {
        List<Map.Entry<K, V>> list =
                new LinkedList<Map.Entry<K, V>>(map.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
            public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
                return (o1.getValue()).compareTo(o2.getValue());
            }
        });

        Map<K, V> result = new LinkedHashMap<K, V>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    /**
     * Десериализация параметров задачи из бд
     *
     * @param contextBytes сериализованные параметры задачи
     * @return параметры задачи
     * @throws java.io.IOException
     * @throws ClassNotFoundException
     */
    public static Map<String, TaskParam> deserializeParams(byte[] contextBytes) throws IOException,
            ClassNotFoundException {
        ByteArrayInputStream bis = new ByteArrayInputStream(contextBytes);
        ObjectInput in = null;
        try {
            in = new ObjectInputStream(bis);
            return (Map<String, TaskParam>) in.readObject();
        } finally {
            bis.close();
            if (in != null) {
                in.close();
            }
        }
    }

    public static String contextToJSON(TaskContext taskContext) {
        Gson gson = new Gson();
        return gson.toJson(taskContext);
    }

    public static TaskContext contextToObject(String contextJson) {
        Gson gson = new Gson();
        return gson.fromJson(contextJson, TaskContext.class);
    }

    private static class CustomParamsComparator implements Comparator<String>, Serializable {
        private static final long serialVersionUID = -3791674119101960757L;
        private Map<String, TaskParam> base;

        private CustomParamsComparator(Map<String, TaskParam> base) {
            this.base = base;
        }

        @Override
        public int compare(String o1, String o2) {
            if (base.get(o1).getId() >= base.get(o2).getId()) {
                return 1;
            } else {
                return -1;
            }
        }
    }
}
