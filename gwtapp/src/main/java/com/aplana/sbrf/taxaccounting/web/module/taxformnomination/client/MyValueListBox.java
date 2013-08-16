package com.aplana.sbrf.taxaccounting.web.module.taxformnomination.client;

import com.google.gwt.text.shared.Renderer;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.ValueListBox;

import java.util.Collection;

/**
 * ValueListBox без пустого значения
 *
 * @author Stanislav Yasinskiy
 */

public class MyValueListBox<T> extends ValueListBox<T> {

    private ListBox listBox = (ListBox) getWidget();

    public MyValueListBox(Renderer<T> renderer) {
        super(renderer);
    }

    public void setVisibleItemCount(int count) {
        listBox.setVisibleItemCount(count);
    }

    @Override
    public void setAcceptableValues(Collection<T> newValues) {
        super.setAcceptableValues(newValues);
        if(listBox.getItemCount()>newValues.size())
            listBox.removeItem(listBox.getItemCount() - 1);
    }

    public boolean hasSelectedItem() {
        return listBox.getSelectedIndex() != -1;
    }


}
