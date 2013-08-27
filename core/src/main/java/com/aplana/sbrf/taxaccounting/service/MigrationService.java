package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.migration.Exemplar;
import com.aplana.sbrf.taxaccounting.model.migration.row.AbstractRnuRow;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Сервис для проекта Миграции данных
 *
 * @author Alexande Ivanov
 */
public interface MigrationService {

    public static List<Integer> RNU_LIST = Arrays.asList(25, 26, 27, 31);
    public static List<Integer> XML_LIST = Arrays.asList(51, 53, 54, 59, 60, 64);
    /**
     * Получение актуальных записей экземпляров конкретного типа рну
     *
     * @param rnuTypeId тип рну (25,26..)
     * @return список экземпляров с необходимыми данными
     */
    List<Exemplar> getActualExemplarByRnuType(long rnuTypeId);

    /**
     * Получение актуальных записей экземпляров, к которым привязаны строки налоговой формы
     *
     * @param rnuIds список типов рну
     * @return список экземпляров с необходимыми данными
     */
    List<Exemplar> getActualExemplarByRnuType(List<Long> rnuIds);

    /**
     * Основной метод для формирования содержимых файлов.
     *
     * !Применяется для отладки
     *
     * @param rnuIds список видов рну для которых надо сформировать ТФ
     * @return связную карту с названием файла и его содержимым
     */
    Map<String, String> startMigrationProcessDebug(List<Long> rnuIds);

    /**
     * Получение списка строк налоговой формы
     *
     *
     * @param ex основные параметры наловогой формы
     * @return список строк + 2 итоговые строки
     */
    List<? extends AbstractRnuRow> getRnuList(Exemplar ex);
    /**
     * Основной метод для формирования содержимых файлов
     *
     * @param rnuIds список видов рну для которых надо сформировать ТФ
     * @return связную карту с названием файла и его содержимым
     */
    Map<String, byte[]> startMigrationProcess(List<Long> rnuIds);
}
