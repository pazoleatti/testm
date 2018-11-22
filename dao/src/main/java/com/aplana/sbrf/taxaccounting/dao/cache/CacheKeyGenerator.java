package com.aplana.sbrf.taxaccounting.dao.cache;

import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.cache.interceptor.KeyGenerator;

import java.lang.reflect.Method;

/**
 * Генератор ключей для кеша. О необходимости такого генератора можно почитать в
 * комментариях к задаче.
 * <p/>
 * <a
 * href="https://jira.spring.io/browse/SPR-9036">https://jira.spring.io/browse
 * /SPR-9036</a>
 * <p/>
 * Реализация генератора похожа на реализацию SimpleKeyGenerator из
 * spring-context 4.0 однако в отличии от Spring в данном генераторе
 * используются имя класса и метода.
 */
public class CacheKeyGenerator implements KeyGenerator {

    public Object generate(Object target, Method method, Object... params) {
        Class<?> objectclass = AopProxyUtils.ultimateTargetClass(target);
        String className = objectclass.getName().intern();
        String methodName = method.toString().intern();

        if (params.length == 0) {
            return new SimpleKey(className, methodName);
        }

        return new SimpleKey(className, methodName, params);
    }

}
