package com.aplana.sbrf.taxaccounting.web.widget.departmentpicker;

import java.util.List;
import java.util.Set;

import com.aplana.gwt.client.modal.OpenModalWindowEvent;
import com.aplana.sbrf.taxaccounting.model.Department;
import com.google.gwt.editor.client.LeafValueEditor;
import com.google.gwt.user.client.ui.HasValue;

import java.util.List;
import java.util.Set;

/**
 * @author Eugene Stetsenko
 */
public interface DepartmentPicker extends HasValue<List<Integer>>, LeafValueEditor<List<Integer>> {
    /**
     * Установка списка подразделений для дерева и подразделений, доступных для выбора
     * @param departments Подразделения для построения дерева
     * @param availableDepartments Доступные для выбора подразделения
     */
	void setAvalibleValues(List<Department> departments, Set<Integer> availableDepartments);

    Integer getSingleValue();

    void addOpenModalWindowHandler(OpenModalWindowEvent.OpenHandler handler);

    void removeOpenModalWindowHandler();

    /**
     * Список подразделений доступных для выбора
     */
    List<Integer> getAvalibleValues();
    /** Получить список названий выбранных подразделений. */
	List<String> getValueDereference();

    /** Установить заголовок в окне выбора подразделений. */
	void setTitle(String title);

    void setSelectButtonFocus(boolean focused);

    void clearFilter();

    /**
     * Возвращает текстовое представление для выбранных элементов
     * @return
     */
    String getText();
}
