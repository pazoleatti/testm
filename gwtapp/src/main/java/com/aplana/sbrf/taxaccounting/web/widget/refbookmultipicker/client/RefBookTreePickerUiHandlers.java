package com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.client;

import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.shared.RefBookUiTreeItem;
import com.gwtplatform.mvp.client.UiHandlers;

import java.util.Date;

public interface RefBookTreePickerUiHandlers extends UiHandlers {

    /**
     * Инициализация дерева. Должна выполняться только один раз.
     * Тут происхлжит установка значений для поиска данных
     *
     * @param refBookAttrId идентификатор атрибута справочника
     * @param filter        строка поиска
     * @param relevanceDate дата актуальности
     */
    void init(long refBookAttrId, String filter, Date relevanceDate);

    /**
     * Перезагрузка всего дерева, например, при смене фильтра или даты
     */
    void reload();

    /**
     * Загрузка чилдов для выбранного итема
     *
     * @param uiTreeItem выбранный итем
     */
    void loadForItem(RefBookUiTreeItem uiTreeItem);

    void search();

    void versionChange();
}
