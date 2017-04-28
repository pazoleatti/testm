package com.aplana.sbrf.taxaccounting.web.main.api.server;

import com.aplana.sbrf.taxaccounting.model.DeclarationDataFilter;
import com.aplana.sbrf.taxaccounting.model.TemplateFilter;
import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.HandleStringLength;
import com.gwtplatform.dispatch.shared.Action;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Обработчик входных параметров. Находит и преобразует входные параметры. Если параметр инкапсулирует поле типа String
 * и имя этого поля указано в аннотации @HandleStringLength, а также размер поля больше чем размер указанный
 * в аннотации @HandleStringLength, то значение объекта будет обрезано до указанного размера.
 */
@Aspect
@Component
public class QueryStringLengthAspect {

    @Around(value = "execution (public * com.gwtplatform.dispatch.server.actionhandler.ActionHandler.execute(..)) && @annotation(com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.HandleStringLength))")
    public Object trimString(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        HandleStringLength annotation = methodSignature.getMethod().getAnnotation(HandleStringLength.class);
        List<String> fieldNames = Arrays.asList(annotation.fieldNames());
        int stringLength = annotation.stringLength();
        Object[] args = joinPoint.getArgs();
        for (Object arg : args) {
            if (arg instanceof Action) {
                Class<?> clazz = arg.getClass();
                List<Field> fields = new ArrayList<Field>(Arrays.asList(clazz.getDeclaredFields()));
                addDeclaredAndInheritedFields(clazz, fields);
                for (Field field : fields) {
                    if (field.getType().equals(String.class)) {
                        if (fieldNames.contains(field.getName())) {
                            field.setAccessible(true);
                            field.set(arg, trimString((String) field.get(arg), stringLength));
                            field.setAccessible(false);
                        }
                    } else if (field.getType().equals(DeclarationDataFilter.class)) {
                        decomposeInnerObject(field, fieldNames, stringLength, arg);
                    } else if (field.getType().equals(TemplateFilter.class)) {
                        decomposeInnerObject(field, fieldNames, stringLength, arg);
                    }
                }
            }
        }
        return joinPoint.proceed(args);
    }

    private void addDeclaredAndInheritedFields(Class<?> c, Collection<Field> fields) {
        fields.addAll(Arrays.asList(c.getDeclaredFields()));
        Class<?> superClass = c.getSuperclass();
        if (superClass != null) {
            addDeclaredAndInheritedFields(superClass, fields);
        }
    }

    private String trimString(String originalText, int maxStringLength) {
        String text = originalText;
        if (text == null) {
            text = "";
        }
        if (text.length() <= maxStringLength) {
            return text;
        } else {
            text = text.substring(0, maxStringLength);
            return text;
        }
    }

    private <T> void decomposeInnerObject(Field field, List<String> fieldNames, int stringLength, Object arg) throws IllegalAccessException {
        Class<?> clazz = field.getType();
        for (Field innerField : clazz.getDeclaredFields()) {
            if (innerField.getType().equals(String.class) && fieldNames.contains(innerField.getName())) {
                field.setAccessible(true);
                innerField.setAccessible(true);
                T innerObject = (T)field.get(arg);
                innerField.set(innerObject, trimString((String) innerField.get(innerObject), stringLength));
                field.setAccessible(false);
                innerField.setAccessible(false);
            }
        }
    }
}
