package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.DaoObject;
import com.aplana.sbrf.taxaccounting.dao.impl.cache.CacheConstants;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import java.util.Random;

/**
 * Реализация объекта дао
 */
@Repository
public class DaoObjectImpl implements DaoObject {
    @Cacheable(value = CacheConstants.FORM_TEMPLATE)
    public int getRandomInt(){
        return new Random().nextInt();
    }

    @Override
    public int getCachedNumberFromCachedMethod() {
        return getRandomInt();
    }

    @Cacheable(value = CacheConstants.FORM_TEMPLATE, key = "#key")
    private int privateGetRandomInt(String key){
        return new Random().nextInt();
    }

    @Override
    public int getCachedNumberFromPrivateInsideMethod(String key) {
        return privateGetRandomInt(key);
    }
}