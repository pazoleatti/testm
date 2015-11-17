package com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.client;

import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.client.event.CheckValuesCountHandler;
import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.shared.model.PickerState;
import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.shared.model.RefBookUiTreeItem;
import com.gwtplatform.mvp.client.UiHandlers;

import java.util.Date;
import java.util.List;

/**
 * Интерфейс для взаимодествия
 *
 * @author aivanov
 */
public interface RefBookTreePickerUiHandlers extends UiHandlers {

    /**
     * Инициализация дерева. Должна выполняться только один раз.
     * Тут происхлжит установка значений для поиска данных
     */
    void init(PickerState pickerState, boolean force);

    /**
     * Загрузка чилдов для выбранного итема
     *
     * @param uiTreeItem выбранный итем
     */
    void loadForItem(RefBookUiTreeItem uiTreeItem);

    /**
     * Загрузка дерева с указанием фильтра
     * @param searchPattern строка поиска
     */
    void find(String searchPattern);

    /**
     * Перезагрузка верхушки дерева
     */
    void reload();

    /**
     * Перезагрузка верхушки дерева с нужными для выделения ид
     */
    void reload(List<Long> needToSelectIds);

    /**
     * Загружает списко ид родителей ваверх по иерархии начиная с uniqueRecordId и открывает их последовательно
     * @param uniqueRecordId идентификатор итема
     * @param isChild не включать uniqueRecordId в списко открытия
     */
    void openFor(Long uniqueRecordId, boolean isChild);

    /**
     * Перезагрузить дерево
     * @param version дата версии на которую загружить итемы
     */
    void reloadForDate(Date version);

    void selectFirstItenOnLoad();

    void highLightItem(RefBookUiTreeItem uiTreeItem);

    void getValuesCount(String text, CheckValuesCountHandler checkValuesCountHandler);
}
