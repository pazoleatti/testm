package com.aplana.sbrf.taxaccounting.web.widget.style;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DropdownButton extends LinkButton {
    private Map<String, IsWidget> items = new HashMap<String, IsWidget>();
    private Panel pnl = new VerticalPanel();
    final PopupPanel dropDown = new PopupPanel(true);

    public DropdownButton() {
        this(null, null, null);
    }

    public DropdownButton(String text, String img, String title) {
        super(text, img, title);
        this.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                dropDown.setWidget(pnl);
                dropDown.setPopupPosition(DropdownButton.this.getAbsoluteLeft(),
                        DropdownButton.this.getAbsoluteTop() + DropdownButton.this.getOffsetHeight());
                dropDown.show();
                dropDown.getElement().getStyle().setRight(Document.get().getClientWidth() - DropdownButton.this.getElement().getAbsoluteRight(), Style.Unit.PX);
                dropDown.getElement().getStyle().clearLeft();
            }
        });
    }

    public void addItem(String key, IsWidget item) {
        IsWidget cItem;
        if ((cItem = items.get(key)) != null)
            pnl.remove(cItem);
        items.put(key, item);
        pnl.add(item);
    }

    public void setPanelStyleName(String style) {
        dropDown.addStyleName(style);
    }

    public IsWidget getItem(String key) {
        return items.get(key);
    }

    public void clear() {
        items.clear();
        pnl.clear();
    }
}
