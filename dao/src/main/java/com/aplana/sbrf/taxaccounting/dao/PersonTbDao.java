package com.aplana.sbrf.taxaccounting.dao;

import com.aplana.sbrf.taxaccounting.model.refbook.PersonTb;
import com.aplana.sbrf.taxaccounting.model.refbook.RegistryPerson;

import java.util.Collection;
import java.util.List;

/**
 * DAO для работы с объектами назначенных физлицу Тербанков (ref_book_person_tb)
 */
public interface PersonTbDao {

    /**
     * Возвращяет список тербанков физлица
     *
     * @param person ФЛ
     * @return список тербанков ФЛ
     */
    List<PersonTb> getByPerson(RegistryPerson person);

    /**
     * Создаёт группу назначенных физлицу Тербанков
     *
     * @param personTbs коллекция назначенных физлицу Тербанков
     */
    void createBatch(final Collection<PersonTb> personTbs);


    /**
     * Обновляет данные группы назначенных физлицу Тербанков
     *
     * @param personTbList коллекция назначенных физлицу Тербанков
     */
    void updateBatch(final Collection<PersonTb> personTbList);

    /**
     * Удаляет назначенные физлицу Тербанки по списку идентификаторов
     *
     * @param ids список идентификаторов назначенных физлицу Тербанков
     */
    void deleteByIds(final Collection<Long> ids);
}
