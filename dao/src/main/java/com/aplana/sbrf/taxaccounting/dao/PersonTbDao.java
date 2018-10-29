package com.aplana.sbrf.taxaccounting.dao;

import com.aplana.sbrf.taxaccounting.model.refbook.PersonTb;
import com.aplana.sbrf.taxaccounting.model.refbook.RegistryPerson;

import java.util.Collection;
import java.util.List;

/**
 * DAO для работы с объектами назначенных физлицу Тербанков ref_book_person_tb
 */
public interface PersonTbDao {

    /**
     * Сохранить группу назначенных физлицу Тербанков
     * @param personTbs коллекция назначенных физлицу Тербанков
     */
    void saveBatch(final Collection<PersonTb> personTbs);

    /**
     * Получить список тербанков физлица
     * @param person
     * @return
     */
    List<PersonTb> getByPerson(RegistryPerson person);
}
