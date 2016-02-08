package com.aplana.sbrf.taxaccounting.service.script.api;

import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.DataRow;

import java.util.List;

/**
 * Работа со строками НФ.
 * 
 * Для более подробной информации о работе методов см. DataRowDao интерфейс
 * 
 * @author sgoryachkin
 * 
 */
public interface DataRowHelper {

	/** Устанавливает количество одновременно вставляемых в бд строк */
	int INSERT_LIMIT = 1024;

	/**
	 * Получение списка сохраненных строк формы
	 * 
	 * @return
	 */
    @SuppressWarnings("unused")
	List<DataRow<Cell>> getAllSaved();

	/**
	 * Получение количества сохранненых строк формы
	 * 
	 * @return
	 */
    @SuppressWarnings("unused")
	int getSavedCount();

	/**
	 * Получение текущих рабочих строк НФ
	 * 
	 * @return
	 */
	List<DataRow<Cell>> getAll();

	/**
	 * Получение количества текущих рабочих строк НФ
	 * 
	 * @return
	 */
	int getCount();
	
	/**
	 * Сохранить строки. Строки сохраняются. Старое временное
	 * состояние удаляется. Порядок  сохраняется.
	 * Метод нужен для легаси способа работы с офрмами
	 * т.е. Получили все строки, обработали как список и сохранили.
	 * 
	 * @param dataRows
	 */
	void save(List<DataRow<Cell>> dataRows);

	/**
	 * Вставка строки
	 * 
	 * @param dataRow
	 * @param index
	 */
	void insert(DataRow<Cell> dataRow, int index);

	/**
	 * Вставка списка строк
	 * 
	 * @param dataRows
	 * @param index
	 */
	void insert(List<DataRow<Cell>> dataRows, int index);

	/**
	 * Обновление строки
	 * 
	 * @param dataRow
	 */
	void update(DataRow<Cell> dataRow);

	/**
	 * Обновление списка строк
	 * 
	 * @param dataRows
	 */
	void update(List<DataRow<Cell>> dataRows);

	/**
	 * Удаление строки
	 * 
	 * @param dataRow
	 */
	void delete(DataRow<Cell> dataRow);

	/**
	 * Удаление списка строк
	 * 
	 * @param dataRows
	 */
	void delete(List<DataRow<Cell>> dataRows);

	/**
	 * Сохранение строк НФ
	 * 
	 * @deprecated Не должен вызываться из скриптов
	 * 
	 */
	@Deprecated
	void commit();

	/**
	 * Отмена изменений сток НФ
	 * 
	 * @deprecated Не должен вызываться из скриптов
	 * 
	 */
	@Deprecated
	void rollback();

    /**
     * Возвращает DataRow по алиасу
     * @param dataRows
     * @param rowAlias
     * @return
     */
    @SuppressWarnings("unused")
    DataRow getDataRow(List<DataRow<Cell>> dataRows, String rowAlias);

    /**
     * Функция для получения данных формы
     * Используется ленивая загрузка + кэш
     */
    List<DataRow<Cell>> getAllCached();

    /**
     * Функция сохранения данных формы в кэш
     * без сохранения в бд
     */
    void setAllCached(List<DataRow<Cell>> dataRows);

    /**
     * Сброс кэша
     */
    void dropCache();

    /**
     * Возвращает индекс строки, имеющий заданный алиас.
     *
     * @param rowAlias
     *            алиас строки
     * @return индекс строки (с нуля)
     * @throws NullPointerException
     *             если rowAlias null
     * @throws IllegalArgumentException
     *             если такого алиас не существует в объекте FormData
     */
    @SuppressWarnings("unused")
    int getDataRowIndex(List<DataRow<Cell>> dataRows, String rowAlias);

    /**
     * Очистка формы
     */
    void clear();

    /**
     * Сохранить отсортированные строки без учета остальных изменении. Обновятся только значения атрибута FORM_DATA_ROW.ORD
     */
    @SuppressWarnings("unused")
    void saveSort();
}
