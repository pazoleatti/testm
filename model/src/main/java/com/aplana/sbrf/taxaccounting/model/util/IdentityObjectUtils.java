package com.aplana.sbrf.taxaccounting.model.util;

import com.aplana.sbrf.taxaccounting.model.IdentityObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Вспомогательные метода по работе с IdentityObject<T>
 */
public class IdentityObjectUtils {

    /**
     * Возвращяет список идентификаторов из списока объектов
     */
    public static <T extends Number, E extends IdentityObject<T>> List<T> getIds(Collection<E> objects) {
        List<T> ids = new ArrayList<>();
        for (E object : objects) {
            ids.add(object.getId());
        }
        return ids;
    }

    /**
     * Ищет в списке объект по ид и возвращяет его
     */
    public static <T extends Number, E extends IdentityObject<T>> E findById(Collection<E> objects, T id) {
        for (E object : objects) {
            if (id.equals(object.getId())) {
                return object;
            }
        }
        return null;
    }

    /**
     * Возвращяет true, если у объектов совпадают идентификаторы, иначе - false
     */
    public static <T extends Number, A extends IdentityObject<T>, B extends IdentityObject<T>> boolean equalById(A a, B b) {
        return a == b || (a != null && a.getId().equals(b != null ? b.getId() : null));
    }

    /**
     * Удаляет из коллекции первый попавшийся объект по идентификатору
     */
    public static <T extends Number, E extends IdentityObject<T>> void removeById(Collection<E> objects, T id) {
        for (Iterator<E> iterator = objects.iterator(); iterator.hasNext(); ) {
            E object = iterator.next();
            if (id.equals(object.getId())) {
                iterator.remove();
                return;
            }
        }
    }
}
