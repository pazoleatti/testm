package com.aplana.gwt.client;

import com.aplana.gwt.client.modal.CanHide;
import com.aplana.gwt.client.modal.OnHideHandler;
import com.aplana.gwt.client.modal.OpenModalWindowEvent;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Модальное окно.
 *
 * getElement() - корневой элемент ModalWindow
 * getWidget().getElement() - элемент DecoratorPanel
 *
 * @author vpetrov
 * @since 16.12.13
 *
 * @author aivanov
 * @since 01.02.14
 */
public class ModalWindow extends DialogBox implements CanHide {

    private static Integer CAPTION_HEIGHT = 27;
    private static Integer MIN_WINDOW_HEIGHT = 101;
    private static Integer MIN_WINDOW_WIDTH = 350;
    private static String CLOSE_ICON_TITLE = "Закрыть окно";

    interface ModalWindowResources extends ClientBundle {
        @Source("close.png")
        @ImageResource.ImageOptions(repeatStyle = ImageResource.RepeatStyle.Both)
        ImageResource closeImage();

        @Source("icon.png")
        @ImageResource.ImageOptions(repeatStyle = ImageResource.RepeatStyle.Both)
        ImageResource iconImage();
    }

    private ModalWindowResources mwRes = GWT.create(ModalWindowResources.class);

    private Image icon;
    private Image close;
    private HTMLPanel caption;
    private Label title;

    private boolean bDragDrop = false;
    protected boolean isResizable = false;
    private List<ModalWindowResizeListener> panelResizedListeners = new ArrayList<ModalWindowResizeListener>();

    private OnHideHandler<CanHide> hideHandler = new OnHideHandler<CanHide>() {
        @Override
        public void OnHide(CanHide modalWindow) {
            modalWindow.hide();
        }
    };

    public ModalWindow() {
        super(false, true);
        this.setGlassEnabled(true);

        title = new Label();
        close = new Image(mwRes.closeImage());
        icon = new Image(mwRes.iconImage());

        title.getElement().getStyle().setFloat(Style.Float.LEFT);
        title.getElement().getStyle().setLineHeight(26, Style.Unit.PX);
        close.getElement().getStyle().setCursor(Style.Cursor.POINTER);
        close.getElement().getStyle().setFloat(Style.Float.RIGHT);
        close.getElement().getStyle().setMargin(4, Style.Unit.PX);
        icon.getElement().getStyle().setFloat(Style.Float.LEFT);
        icon.getElement().getStyle().setMargin(4, Style.Unit.PX);
        icon.setTitle(CLOSE_ICON_TITLE);

        caption = new HTMLPanel("");
        caption.getElement().getStyle().setDisplay(Style.Display.INLINE_BLOCK);
        caption.setWidth("100%");
        caption.addStyleName("Caption");

        caption.add(icon);
        caption.add(title);
        caption.add(close);

        Element td = getCellElement(0, 1);
        td.getFirstChildElement().getParentElement().getStyle().setHeight(CAPTION_HEIGHT, Style.Unit.PX);
        DOM.removeChild(td, (Element) td.getFirstChildElement());
        DOM.appendChild(td, caption.getElement());

        DOM.sinkEvents(this.getElement(),
                Event.ONMOUSEDOWN |
                        Event.ONMOUSEMOVE |
                        Event.ONMOUSEUP |
                        Event.ONMOUSEOVER
        );
        // удаляем не нужный bottom элемент
        getCellElement(2,0).getParentElement().getParentElement().removeFromParent();

        setMainInnerElementProperty("minWidth", MIN_WINDOW_WIDTH);
        setMainInnerElementProperty("minHeight", MIN_WINDOW_HEIGHT - CAPTION_HEIGHT);
    }

    /**
     * @param title - заголовок окна
     */
    public ModalWindow(String title) {
        this();
        setTitle(title);
    }

    /**
     *
     * @param title
     * @param iconUrl
     */
    public ModalWindow(String title, String iconUrl) {
        this(title);
        setIconUrl(iconUrl);
    }

    /**
     *
     * @param hideHandler
     */
    public ModalWindow(OnHideHandler<CanHide> hideHandler) {
        this();
        this.hideHandler = hideHandler;
    }

    public ModalWindow(String title, OnHideHandler<CanHide> hideHandler) {
        this(title);
        this.hideHandler = hideHandler;
    }

    public void setIconUrl(String iconUrl) {
        if (iconUrl != null && !iconUrl.trim().isEmpty()) {
            icon.setUrl(iconUrl);
            icon.getElement().getStyle().setFloat(Style.Float.LEFT);
            icon.getElement().getStyle().setMargin(4, Style.Unit.PX);
            icon.setTitle(CLOSE_ICON_TITLE);
        }
    }

    public String getIconUrl() {
        return icon.getUrl();
    }

    @Override
    public void setTitle(String title) {
        this.title.setText(title);
    }

    @Override
    public String getTitle() {
        return this.title.getText();
    }

    @Override
    public String getText() {
        return this.title.getText();
    }

    @Override
    public void setText(String text) {
        this.title.setText(text);
    }

    @Override
    protected void onPreviewNativeEvent(Event.NativePreviewEvent event) {
        NativeEvent nativeEvent = event.getNativeEvent();
        if ((!event.isCanceled()
                && (event.getTypeInt() == Event.ONCLICK)
                && isCloseEvent(nativeEvent))
                || (Event.ONKEYUP == event.getTypeInt() && event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ESCAPE)) {
            hideHandler.OnHide(this);
        }
        super.onPreviewNativeEvent(event);
    }

    public void center() {
        super.center();
        OpenModalWindowEvent.fire(this);
    }

    private void setMainInnerElementProperty(String propName, String value){
        getCellElement(1, 1).getStyle().setProperty(propName, value);
    }
    private void setMainInnerElementProperty(String propName, Integer value){
        getCellElement(1, 1).getStyle().setPropertyPx(propName, value);
    }

    public void setSize(String width, String height) {
        setWidth(width);
        setHeight(height);
    }

    public void setMinWidth(String minWidth){
        setMainInnerElementProperty("minWidth", minWidth);
    }

    public void setWidth(String width) {
        setMainInnerElementProperty("width", width);
        //getWidget().setWidth(width);
    }

    public void setHeight(String height) {
        if (height != null && !height.isEmpty()) {
            Integer heightInt = Integer.valueOf(height.replace("px", "")) - CAPTION_HEIGHT;
            if (heightInt > 0) {
                setMainInnerElementProperty("height", heightInt);
            }
        } else {
            setMainInnerElementProperty("height", height);
        }

        //getWidget().setHeight(height);
    }

    public HandlerRegistration addOpenModalWindowHandler(OpenModalWindowEvent.OpenHandler handler) {
        return this.addHandler(handler, OpenModalWindowEvent.getType());
    }

    private boolean isCloseEvent(NativeEvent event) {
        return event.getEventTarget().equals(close.getElement());
    }

    public void setOnHideHandler(OnHideHandler<CanHide> hideHandler) {
        if (hideHandler != null) {
            this.hideHandler = hideHandler;
        }
    }

    @Override
    public void onBrowserEvent(Event event) {
        final int eventType = DOM.eventGetType(event);
        if (isResizable) {
            if (Event.ONMOUSEOVER == eventType) {

                if (isCursorResize(event)) {
                    DOM.setStyleAttribute(this.getElement(), "cursor", "se-resize");
                } else {
                    DOM.setStyleAttribute(this.getElement(), "cursor", "default");
                }
            }
            if (Event.ONMOUSEDOWN == eventType) {
                if (isCursorResize(event)) {
                    //enable/disable resize
                    if (!bDragDrop) {
                        bDragDrop = true;

                        DOM.setCapture(this.getElement());
                        event.preventDefault();
                        event.stopPropagation();
                    }
                }
            } else if (Event.ONMOUSEMOVE == eventType) {
                //reset cursor-type
                if (!isCursorResize(event)) {
                    DOM.setStyleAttribute(this.getElement(), "cursor", "default");
                }

                //calculate and set the new size
                if (bDragDrop) {
                    int absX = DOM.eventGetClientX(event);
                    int absY = DOM.eventGetClientY(event);
                    int originalX = DOM.getAbsoluteLeft(getCellElement(1, 1));
                    int originalY = DOM.getAbsoluteTop(getCellElement(1, 1));

                    //System.out.println("absY " + absY + " " + originalY + " "  + (absY - originalY) + " " + getElement().getOffsetHeight());
                    //System.out.println("absY " + absX + " " + originalX + " "  + (absX - originalX) + " " + getElement().getOffsetWidth());
                    if (absY > originalY && absX > originalX) {
                        boolean isChange = false;
                        Integer height = absY - originalY + 2;
                        Integer width = absX - originalX + 2;
                        Integer minHeight = Integer.valueOf(getCellElement(1, 1).getStyle().getProperty("minHeight").replace("px", ""));
                        Integer minWidth = Integer.valueOf(getCellElement(1, 1).getStyle().getProperty("minWidth").replace("px", ""));

                        if (height >= minHeight) {
                            setMainInnerElementProperty("height", height);
                            isChange = true;
                        }

                        if (width >= minWidth) {
                            setMainInnerElementProperty("width", width);
                            isChange = true;
                        }
                        if (isChange) {
                            notifyPanelResizedListeners(width, height);
                        }
                    }
                    event.preventDefault();
                    event.stopPropagation();
                }
            } else if (Event.ONMOUSEUP == eventType) {
                if (bDragDrop) {
                    bDragDrop = false;
                    DOM.releaseCapture(this.getElement());
                    event.preventDefault();
                    event.stopPropagation();
                }
            }
        }
        super.onBrowserEvent(event);
    }

    /**
     * Проверка на то что курсор над правый нижним углом
     */
    protected boolean isCursorResize(Event event) {
        int cursorY = DOM.eventGetClientY(event);
        int initialY = this.getAbsoluteTop();
        int height = this.getOffsetHeight();

        int cursorX = DOM.eventGetClientX(event);
        int initialX = this.getAbsoluteLeft();
        int width = this.getOffsetWidth();

        //правый нижний угол (площадь 10 пикселей)
        if (((initialX + width - 10) < cursorX && cursorX <= (initialX + width)) &&
                ((initialY + height - 10) < cursorY && cursorY <= (initialY + height)))
            return true;
        else
            return false;
    }

    public void addPanelResizedListener(ModalWindowResizeListener listener) {
        panelResizedListeners.add(listener);
    }

    private void notifyPanelResizedListeners(Integer width, Integer height) {
        for (ModalWindowResizeListener listener : panelResizedListeners) {
            listener.onResized(width, height);
        }
    }

    public boolean isResizable() {
        return isResizable;
    }

    public void setResizable(boolean isResizable) {
        this.isResizable = isResizable;
    }


}
