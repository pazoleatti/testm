package com.aplana.sbrf.taxaccounting.web.widget.menu.shared;

import com.gwtplatform.dispatch.shared.Result;

import java.util.List;

/**
 * Абстрактный класс для меню
 *
 * @author Fail Mukhametdinov
 */
public abstract class AbstractMenuResult implements Result {
    private List<MenuItem> menuItems;

    public List<MenuItem> getMenuItems() {
        return menuItems;
    }

    public void setMenuItems(List<MenuItem> menuItems) {
        this.menuItems = menuItems;
    }
}
