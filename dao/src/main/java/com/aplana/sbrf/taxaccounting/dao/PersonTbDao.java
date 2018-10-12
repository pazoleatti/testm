package com.aplana.sbrf.taxaccounting.dao;

import com.aplana.sbrf.taxaccounting.model.refbook.PersonTb;

import java.util.Collection;

/**
 * DAO для работы с объектами назначенных физлицу Тербанков ref_book_person_tb
 */
public interface PersonTbDao {

    /**
     * Сохранить группу назначенных физлицу Тербанков
     * @param personTbs коллекция назначенных физлицу Тербанков
     */
    void saveBatch(final Collection<PersonTb> personTbs);
}
