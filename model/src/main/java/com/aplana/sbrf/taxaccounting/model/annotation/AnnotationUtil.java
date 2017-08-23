package com.aplana.sbrf.taxaccounting.model.annotation;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;

import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Set;

/**
 * Класс помогает работать с аннотациями свойств. Т.е. он позволяет получать аннотации свойства по его имени и не только
 * аннотации привзяанные к полю, но и к методам доступа getter и setter.
 *
 * @author Vitalii Samolovskikh
 */
public final class AnnotationUtil {
    /**
     * Защищает от создания экземпляра класса.
     */
    private AnnotationUtil() {
        // Ничего не делает
    }

    /**
     * Находит аннотации заданного типа у свойства класса.
     * Приоритет: поле, getter, setter.
     *
     * @param beanClass       класс в котором мы ищем аннотации.
     * @param pd              дескриптор свойства
     * @param annotationClass класс искомой аннотации
     * @return аннотация
     */
    public static <T extends Annotation> T find(Class<?> beanClass, PropertyDescriptor pd, Class<T> annotationClass) {
        T annotation = null;
        Field field = FieldUtils.getField(beanClass, pd.getName(), true);
        if(field != null) {
            annotation = field.getAnnotation(annotationClass);
        }

        if (annotation == null) {
            Method method = pd.getReadMethod();
            if (method != null) {
                annotation = method.getAnnotation(annotationClass);
            }

            if (annotation == null) {
                method = pd.getWriteMethod();
                if (method != null) {
                    annotation = method.getAnnotation(annotationClass);
                }
            }
        }

        return annotation;
    }

    /**
     * Находит аннотации заданного типа у заданного класса
     *
     * @param beanClass       класс в котором мы ищем аннотации.
     * @param annotationClass класс искомой аннотации
     * @return аннотация
     */
    public static <T extends Annotation> T find(Class<?> beanClass, Class<T> annotationClass){
        return beanClass.getAnnotation(annotationClass);
    }

    /**
     * Возвращает все методы отмеченные аннотацией
     * @param annotationClass аннотация
     * @return список методов
     */
    public static Set<Method> findAllAnnotatedMethods(Class<? extends Annotation> annotationClass) {
        return new Reflections("com.aplana.sbrf.taxaccounting.service.impl.scheduler", new MethodAnnotationsScanner())
                .getMethodsAnnotatedWith(annotationClass);
    }
}
