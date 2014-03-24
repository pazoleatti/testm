package com.aplana.sbrf.taxaccounting.web.widget.cell;

import com.google.gwt.cell.client.AbstractSafeHtmlCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.text.shared.SafeHtmlRenderer;
import com.google.gwt.text.shared.SimpleSafeHtmlRenderer;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.PopupPanel;

import static com.google.gwt.dom.client.BrowserEvents.*;

/**
 * Ячейка заголовка с возможность сортировки
 *
 * @author
 */
public class SortingHeaderCell extends AbstractSafeHtmlCell<String> {

    private static final String ARROW_ASC_IMAGE_SOURCE = "resources/img/sortAscending.png";
    private static final String ARROW_DES_IMAGE_SOURCE = "resources/img/sortDescending.png";
    private static final Image arrowAscImage;
    private static final Image arrowDesImage;

    static {
        arrowAscImage = new Image(ARROW_ASC_IMAGE_SOURCE, 0, 0, 10, 10);
        arrowAscImage.setTitle("По возврастанию");
        arrowDesImage = new Image(ARROW_DES_IMAGE_SOURCE, 0, 0, 10, 10);
        arrowDesImage.setTitle("По убыванию");
    }

    private boolean isAscSort = true;
    private PopupPanel popupWithArrow;
    private Element lastParent;
    private int lastPositionX = 0;
    private int lastPositionY = 0;

    private int exceedOffsetY = 7;
    private int exceedOffsetX = 15;

    public SortingHeaderCell() {
        this(SimpleSafeHtmlRenderer.getInstance());
    }

    public SortingHeaderCell(SafeHtmlRenderer<String> renderer) {
        super(renderer, CLICK, KEYDOWN, MOUSEMOVE, MOUSEDOWN);

        this.popupWithArrow = new PopupPanel(true, false) {
            @Override
            protected void onPreviewNativeEvent(Event.NativePreviewEvent event) {
                if (Event.ONKEYUP == event.getTypeInt()) {
                    if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ESCAPE) {
                        // Dismiss when escape is pressed
                        popupWithArrow.hide();
                    }
                }
            }
        };

        popupWithArrow.getElement().getStyle().setBorderWidth(0, Style.Unit.PX);
        popupWithArrow.getElement().getStyle().setBackgroundColor("transparent");
        popupWithArrow.getElement().getStyle().setPadding(0, Style.Unit.PX);
    }

    @Override
    public void onBrowserEvent(Context context, Element parent, String value,
                               NativeEvent event, ValueUpdater<String> valueUpdater) {
        super.onBrowserEvent(context, parent, value, event, valueUpdater);
        if (CLICK.equals(event.getType())) {
            onEnterKeyDown(context, parent, value, event, valueUpdater);
        }
    }

    @Override
    protected void onEnterKeyDown(Context context, Element parent, String value,
                                  NativeEvent event, ValueUpdater<String> valueUpdater) {
        if (valueUpdater != null) {
            valueUpdater.update(value);

            lastParent = parent;
            popupWithArrow.clear();

            popupWithArrow.add(isAscSort ? arrowAscImage : arrowDesImage);
            isAscSort = !isAscSort;

            popupWithArrow.setPopupPositionAndShow(new PopupPanel.PositionCallback() {
                public void setPosition(int offsetWidth, int offsetHeight) {
                    lastPositionX = lastParent.getAbsoluteLeft() + lastParent.getClientWidth()  - exceedOffsetX;
                    lastPositionY = lastParent.getAbsoluteTop() + (lastParent.getOffsetHeight() / 2) - exceedOffsetY;
                    popupWithArrow.setPopupPosition(lastPositionX, lastPositionY);
                }
            });
        }
    }

    @Override
    protected void render(Context context, SafeHtml value, SafeHtmlBuilder sb) {
        if (value != null) {
            sb.append(value);
        }
    }

    public boolean isAscSort(){
        return isAscSort;
    }

    public void refreshPosition(Element cell) {
        int cellPositionX = cell.getAbsoluteLeft() + cell.getClientWidth() - exceedOffsetX;
        int cellPositionY = cell.getAbsoluteTop() + (cell.getOffsetHeight() / 2) - exceedOffsetY;
        if (cellPositionX != lastPositionX || cellPositionY != lastPositionY) {
            popupWithArrow.setPopupPosition(cellPositionX, cellPositionY);
        }
    }

    public void setNotHideElement(Element notHideElement){
        popupWithArrow.addAutoHidePartner(notHideElement);
    }

    public void removeNotHideElement(Element notHideElement){
        popupWithArrow.removeAutoHidePartner(notHideElement);
    }
}
