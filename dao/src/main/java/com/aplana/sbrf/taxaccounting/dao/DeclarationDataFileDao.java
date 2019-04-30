package com.aplana.sbrf.taxaccounting.dao;

import com.aplana.sbrf.taxaccounting.model.AttachFileType;
import com.aplana.sbrf.taxaccounting.model.DeclarationDataFile;

import java.util.List;

/**
 * DAO интерфейс для работы с информацией о {@link com.aplana.sbrf.taxaccounting.model.DeclarationDataFile файлах НФ(declaration)}
 */
public interface DeclarationDataFileDao extends PermissionDao {

    /**
     * Получение по идентификатору налоговой формы
     * @param declarationDataId идентификатор налоговой формы
     * @return  список объектов хранящих информацию о файлах налоговой формы
     */
    List<DeclarationDataFile> fetchByDeclarationDataId(long declarationDataId);

    /**
     * Создает или обновляет объекты хранящие информацию о файлах налоговой формы.
     * @param declarationDataId идентификатор налоговой формы
     * @param files             список объектов хранящих информацию о файлах налоговой формы
     */
    void createOrUpdateList(final long declarationDataId, List<DeclarationDataFile> files);

    /**
     * Сохранение в БД объекта хранящего информацию о файлах налоговой формы
     * @param file объект хранящий информацию о файлах налоговой формы
     */
    void create(DeclarationDataFile file);

    /**
     * Удаляет у формы все файлы с указанным типом
     * @param declarationDataId идентификатор налоговой формы
     * @param type              тип файла
     * @return кол-во удаленных объектов хранящих информацию о файлах налоговой формы
     */
    long deleteByDeclarationDataIdAndType(long declarationDataId, AttachFileType type);

    /**
     * Удаляет у формы ТФ с расширением xlsx
     * @param declarationDataId идентификатор налоговой формы
     */
    void deleteTransportFileExcel(long declarationDataId);

    /**
     * Находит файл с максимальным "весом" https://conf.aplana.com/pages/viewpage.action?pageId=27184983
     * @param declarationDataId идентификатор налоговой формы
     * @return объект хранящий информацию о файлах налоговой формы
     */
    DeclarationDataFile fetchWithMaxWeight(Long declarationDataId);

    /**
     * Найти данные по файлам НФ имеющие указаный тип
     * @param declarationDataId идентификатор налоговой формы
     * @param fileType      название типа
     * @return список объектов хранящих информацию о файлах налоговой формы
     */
    List<DeclarationDataFile> findAllByDeclarationIdAndType(Long declarationDataId, AttachFileType fileType);

    /**
     * Проверяет по первичному ключу существование записи
     * @param declarationDataId идентификатор налоговой формы
     * @param blobId            идентификатор таблицы хранящей данные файла
     * @return  возвращает {@code true} если такая запись существует, иначе возвращает {@code false}
     */
    boolean isExists(long declarationDataId, String blobId);
}