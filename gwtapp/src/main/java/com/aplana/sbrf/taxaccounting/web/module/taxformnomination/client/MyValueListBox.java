package com.aplana.sbrf.taxaccounting.web.module.taxformnomination.client;

import com.google.gwt.text.shared.Renderer;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.ValueListBox;

import java.util.Collection;

/**
 * Created with IntelliJ IDEA.
 * User: SYasinskiy
 * Date: 15.07.13
 * Time: 12:35
 * To change this template use File | Settings | File Templates.
 */
public class MyValueListBox<T> extends ValueListBox<T> {

    public MyValueListBox(Renderer<T> renderer) {
        super(renderer);
    }

    public void setVisibleItemCount(int count) {
        ((ListBox) getWidget()).setVisibleItemCount(count);

    }

    @Override
    public void setAcceptableValues(Collection<T> newValues) {
        super.setAcceptableValues(newValues);
        ((ListBox) getWidget()).removeItem(((ListBox) getWidget()).getItemCount()-1);
    }


}
