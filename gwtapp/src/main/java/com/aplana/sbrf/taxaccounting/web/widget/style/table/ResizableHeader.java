package com.aplana.sbrf.taxaccounting.web.widget.style.table;

import static com.google.gwt.dom.client.Style.Unit.PX;

import com.aplana.sbrf.taxaccounting.web.widget.cell.SortingHeaderCell;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.AbstractCellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.Header;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Event.NativePreviewHandler;

/**
 * Заголовок с возможностью резайза
 * @param <T>
 */
public abstract class ResizableHeader<T> extends Header<String> {

    interface IDragCallback {
        void dragFinished();
    }

    private static final String RESIZE = "";
    private static final String RESIZE_tt = "Изменение размера";
    private static final Cursor resizeCursor = Cursor.COL_RESIZE;
    private static final String RESIZE_COLOR = "#A49AED";
    private static final String FOREGROUND_COLOR = "white";
    private static final int MINIMUM_COLUMN_WIDTH = 30;
    private static final int RESIZE_HANDLE_WIDTH = 5;

    private static class HeaderCell extends AbstractCell<String> {
        public HeaderCell() {
            super("mousemove");
        }

        @Override
        public void render(Context context, String value, SafeHtmlBuilder sb) {
            sb.append(SafeHtmlUtils.fromString(value));
        }
    }

    private static NativeEvent getEventAndPreventPropagation(NativePreviewEvent event) {
        final NativeEvent nativeEvent = event.getNativeEvent();
        nativeEvent.preventDefault();
        nativeEvent.stopPropagation();
        return nativeEvent;
    }

    private static void setLine(Style style, int width, int top, int height, String color) {
        style.setPosition(Position.ABSOLUTE);
        style.setTop(top, PX);
        style.setHeight(height, PX);
        style.setWidth(width, PX);
        style.setBackgroundColor(color);
        style.setZIndex(4);
    }

    private String title;
    private final Document document = Document.get();
    private final AbstractCellTable<T> table;
    private final Element tableElement;
    private HeaderHelper current;
    protected final Column<T, ?> column;
    private final String resizeStyle;
    private final String resizeToolTip;

    public ResizableHeader(String title, AbstractCellTable<T> table, Column<T, ?> column) {
        this(title, table, column, null, null, null);
    }

    public ResizableHeader(String title, AbstractCellTable<T> table, Column<T, ?> column, AbstractCell<String> cell) {
        this(title, table, column, cell, null, null);
    }

    public ResizableHeader(String title, AbstractCellTable<T> table, Column<T, ?> column, AbstractCell<String> cell,
                           String resizeStyle, String resizeToolTip) {
        super(cell != null ? cell : new HeaderCell());
        if (title == null || table == null || column == null)
            throw new NullPointerException();
        this.title = title;
        this.column = column;
        this.table = table;
        this.tableElement = table.getElement();
        this.resizeStyle = resizeStyle;
        this.resizeToolTip = resizeToolTip;
    }

    @Override
    public String getValue() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public void onBrowserEvent(Context context, Element target, NativeEvent event) {
        if (current == null)
            current = new HeaderHelper(target, event);
        super.onBrowserEvent(context, target, event);
    }

    private class HeaderHelper implements NativePreviewHandler, IDragCallback {
        private final HandlerRegistration handler = Event.addNativePreviewHandler(this);
        private final Element source;
        private boolean dragging;
        final Element mover, right;

        public HeaderHelper(Element target, NativeEvent event) {
            this.source = target;
            event.preventDefault();
            event.stopPropagation();
            mover = document.createDivElement();
            final int leftBound = target.getOffsetLeft() + target.getOffsetWidth();
            if (resizeStyle != null) {
                right = createSpanElement(resizeStyle, resizeToolTip, leftBound - RESIZE_HANDLE_WIDTH);
            } else {
                right = createSpanElement(RESIZE, RESIZE_tt, resizeCursor, leftBound - RESIZE_HANDLE_WIDTH);
            }
            mover.appendChild(right);
            source.appendChild(mover);
            if (getCell() instanceof SortingHeaderCell) {
                // для сортировочной ячейки добавляем элемент-партнера для НЕскрытия стрелочки
                ((SortingHeaderCell) getCell()).setNotHideElement(mover);
            }
        }

        private SpanElement createSpanElement(String styleClassName, String title, double left) {
            final SpanElement span = document.createSpanElement();
            span.setClassName(styleClassName);
            if (title != null) {
                span.setTitle(title);
            }
            final Style style = span.getStyle();
            style.setPosition(Position.ABSOLUTE);
            style.setBottom(0, PX);
            style.setHeight(source.getOffsetHeight(), PX);
            style.setTop(source.getOffsetTop(), PX);
            style.setWidth(RESIZE_HANDLE_WIDTH, PX);
            style.setLeft(left, PX);
            return span;
        }

        private SpanElement createSpanElement(String innerText, String title, Cursor cursor, double left) {
            final SpanElement span = document.createSpanElement();
            span.setInnerText(innerText);
            span.setAttribute("title", title);
            final Style style = span.getStyle();
            style.setCursor(cursor);
            style.setPosition(Position.ABSOLUTE);
            style.setBottom(0, PX);
            style.setHeight(source.getOffsetHeight(), PX);
            style.setTop(source.getOffsetTop(), PX);
            style.setColor(FOREGROUND_COLOR);
            style.setWidth(RESIZE_HANDLE_WIDTH, PX);
            style.setLeft(left, PX);
            return span;
        }

        @Override
        public void onPreviewNativeEvent(NativePreviewEvent event) {
            final NativeEvent natEvent = event.getNativeEvent();
            final Element element = natEvent.getEventTarget().cast();
            final String eventType = natEvent.getType();
            if (!(element == right)) {
                if ("mousedown".equals(eventType)) {
                    //No need to do anything, the event will be passed on to the column sort handler
                } else if (!dragging && "mouseover".equals(eventType)) {
                    cleanUp();
                }
                return;
            }
            final NativeEvent nativeEvent = getEventAndPreventPropagation(event);
            if ("mousedown".equals(eventType)) {
                new ColumnResizeHelper(this, source, right, nativeEvent);
                dragging = true;
            }
        }

        private void cleanUp() {
            handler.removeHandler();
            mover.removeFromParent();
            if (getCell() instanceof SortingHeaderCell) {
                // для сортировочной ячейки удаляем элемент-партнера для НЕскрытия стрелочки
                ((SortingHeaderCell) getCell()).removeNotHideElement(mover);
            }
            current = null;
        }

        public void dragFinished() {
            dragging = false;
            cleanUp();
        }
    }

    private class ColumnResizeHelper implements NativePreviewHandler {
        private final HandlerRegistration handler = Event.addNativePreviewHandler(this);
        private final DivElement resizeLine = document.createDivElement();
        private final Style resizeLineStyle = resizeLine.getStyle();
        private final Element header;
        private final IDragCallback dragCallback;
        private final Element caret;

        private ColumnResizeHelper(IDragCallback dragCallback, Element header, Element caret, NativeEvent event) {
            this.dragCallback = dragCallback;
            this.header = header;
            this.caret = caret;
            setLine(resizeLineStyle, 2, header.getAbsoluteTop() + header.getOffsetHeight(), getTableBodyHeight(), RESIZE_COLOR);
            moveLine(event.getClientX());
            tableElement.getParentElement().appendChild(resizeLine);
        }

        @Override
        public void onPreviewNativeEvent(NativePreviewEvent event) {
            final NativeEvent nativeEvent = getEventAndPreventPropagation(event);
            final int clientX = nativeEvent.getClientX();
            final String eventType = nativeEvent.getType();
            if ("mousemove".equals(eventType)) {
                moveLine(clientX);
            } else if ("mouseup".equals(eventType)) {
                handler.removeHandler();
                resizeLine.removeFromParent();
                dragCallback.dragFinished();
                columnResized(Math.max(clientX - header.getAbsoluteLeft(), MINIMUM_COLUMN_WIDTH));
                if (getCell() instanceof SortingHeaderCell) {
                    // для сортировочной ячейки сдвигаем позицию стрелочки
                    ((SortingHeaderCell) getCell()).refreshPosition(header);
                }
            }
        }

        private void moveLine(final int clientX) {
            final int xPos = clientX - table.getAbsoluteLeft();
            caret.getStyle().setLeft(xPos - caret.getOffsetWidth() / 2, PX);
            resizeLineStyle.setLeft(xPos, PX);
            resizeLineStyle.setTop(header.getOffsetHeight(), PX);
        }
    }

    protected void columnResized(int newWidth) {
        table.setColumnWidth(column, newWidth + "px");
    }

    protected abstract int getTableBodyHeight();
}