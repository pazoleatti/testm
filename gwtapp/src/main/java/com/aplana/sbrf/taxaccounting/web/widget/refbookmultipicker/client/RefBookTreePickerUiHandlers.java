package com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.client;

import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.shared.PickerState;
import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.shared.RefBookUiTreeItem;
import com.gwtplatform.mvp.client.UiHandlers;

import java.util.Date;

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
    void init(PickerState pickerState);

    /**
     * Загрузка чилдов для выбранного итема
     *
     * @param uiTreeItem выбранный итем
     */
    void loadForItem(RefBookUiTreeItem uiTreeItem);

    void find(String searchPattern);

    void reload(Date version);
}
