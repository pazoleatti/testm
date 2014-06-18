package com.aplana.sbrf.taxaccounting.web.widget.style;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.*;

import java.util.ArrayList;
import java.util.List;

public class DropdownButton extends LinkButton {
    List<IsWidget> items = new ArrayList<IsWidget>();
    Panel pnl = new VerticalPanel();
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
            }
        });
    }

    public void addItem(IsWidget item) {
        pnl.add(item);
    }

    public void addItems(List<IsWidget> items) {
        for (IsWidget item : items) {
            addItem(item);
        }
    }

    public void clear() {
        pnl.clear();
    }

//    @UiChild(limit = 1)
//    public void addWidget(IsWidget widget) {
//        pnl.add(widget);
//        addItems(widget);
//    }
}
