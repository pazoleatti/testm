package com.aplana.sbrf.taxaccounting.web.widget.style;

import com.aplana.sbrf.taxaccounting.web.widget.utils.TrickUtils;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.event.logical.shared.HasAttachHandlers;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.PopupPanel;

import java.math.BigDecimal;

/**
 * Всплывающая подсказка c постепенным появлением и потуханием
 *
 * @author aivanov
 */
public class Tooltip extends Composite {

    public static boolean isBlank(final String str) {
        return (str == null) || (str.trim().isEmpty());
    }

    interface TooltipStyle extends CssResource {
        String tooltipText();
        String tooltip();
    }

    interface MyResources extends ClientBundle {
        @Source("Tooltip.css")
        TooltipStyle css();
    }

    private PopupPanel tooltip;
    private HTML tooltipText;
    private boolean isOnPopup = false;

    protected static final int SHOW_TIMER_DELAY = 25; //ms
    private Timer showTimer;

    protected static final int HIDE_TIMER_DELAY = 25; //ms
    private Timer hideTimer;

    private static final BigDecimal DELTA = BigDecimal.valueOf(1, 1);

    HandlesAllMouseEvents mouseEvents = new HandlesAllMouseEvents() {
        @Override
        public void onMouseDown(MouseDownEvent event) {

        }

        @Override
        public void onMouseMove(MouseMoveEvent event) {
        }

        @Override
        public void onMouseOut(MouseOutEvent event) {
            if(event.getRelatedTarget()!= null && !Element.as(event.getRelatedTarget()).equals(tooltipText.getElement())){
                isOnPopup = false;
                hidePopup();
            } else {
                isOnPopup = true;
            }
        }

        @Override
        public void onMouseOver(MouseOverEvent event) {
            if (tooltipText != null && !tooltipText.getText().isEmpty()) {
                tooltip.setPopupPosition(event.getClientX() + 10, event.getClientY() + 10);
                showPopup();
            }
        }

        @Override
        public void onMouseUp(MouseUpEvent event) {

        }

        @Override
        public void onMouseWheel(MouseWheelEvent event) {

        }
    };

    public Tooltip() {
        MyResources resources = GWT.create(MyResources.class);
        tooltipText = new HTML();
        tooltip = new PopupPanel();
        tooltip.addStyleName(resources.css().tooltip());
        tooltipText.addStyleName(resources.css().tooltipText());
        resources.css().ensureInjected();
        tooltip.add(tooltipText);

        createTimers();

        tooltip.addAttachHandler(new AttachEvent.Handler() {
            @Override
            public void onAttachOrDetach(AttachEvent event) {
                if (event.isAttached()) {
                    int x = tooltip.getPopupLeft();
                    int y = tooltip.getPopupTop();
                    int diffWidth = Window.getClientWidth() - tooltip.getOffsetWidth();
                    int diffHeight = Window.getClientHeight() - tooltip.getOffsetHeight();
                    if (diffHeight < 0) {
                        tooltip.setHeight((Window.getClientHeight() - 15) + "px");
                    }
                    tooltip.setPopupPosition(diffWidth - x < 0 ? diffWidth : x, diffHeight - y < 0 ? diffHeight : (tooltip.getOffsetHeight() + y > Window.getClientHeight()) ? 15 : y);
                }
            }
        });

        tooltipText.addMouseOutHandler(new MouseOutHandler() {
            @Override
            public void onMouseOut(MouseOutEvent event) {
                if(isOnPopup){
                    isOnPopup = false;
                    hidePopup();
                }
            }
        });
    }

    /**
     * Создание таймеров для плавного затухания и появления всплывашки
     */
    private void createTimers() {
        this.showTimer = new Timer() {
            @Override
            public void run() {
                String opacityStr = TrickUtils.impl.getOpacity(tooltip.getElement());
                boolean increased = false;
                if (!isBlank(opacityStr)) {
                    try {
                        BigDecimal opacity = new BigDecimal(opacityStr);
                        if (opacity.compareTo(BigDecimal.ONE) < 0) {
                            double v = opacity.add(DELTA).doubleValue();
                            tooltip.getElement().getStyle().setOpacity(v);
                            increased = true;
                        }
                    } catch (NumberFormatException nfe) {
                        // fallback to showing
                    }
                }
                if (!increased) {
                    tooltip.getElement().getStyle().setOpacity(1);
                    cancel();
                }
            }
        };
        this.hideTimer = new Timer() {
            @Override
            public void run() {
                String opacityStr = TrickUtils.impl.getOpacity(tooltip.getElement());
                boolean decreased = false;
                if (!isBlank(opacityStr)) {
                    try {
                        BigDecimal opacity = new BigDecimal(opacityStr);
                        if (opacity.compareTo(DELTA) > 0) {
                            tooltip.getElement().getStyle().setOpacity(opacity.subtract(DELTA).doubleValue());
                            decreased = true;
                        }
                    } catch (NumberFormatException nfe) {
                        // fallback to hiding
                    }
                }
                if (!decreased) {
                    tooltip.hide();
                    cancel();
                }
            }
        };
    }

    /**
     * Зарегистрировать поведения вплывающей подсказки
     * @param widget для виджета
     */
    public void addHandlersFor(HasAllMouseHandlers widget) {
        if (widget instanceof HasAttachHandlers) {
            ((HasAttachHandlers) widget).addAttachHandler(new AttachEvent.Handler() {
                @Override
                public void onAttachOrDetach(AttachEvent event) {
                    if (!event.isAttached()) {
                        // При детаче скрываем тултип
                        isOnPopup = false;
                        showTimer.cancel();
                        tooltip.hide();
                    }
                }
            });
        }
        widget.addMouseOverHandler(mouseEvents);
        widget.addMouseOutHandler(mouseEvents);
    }

    /**
     * Отображаемый текст
     * @param textHtml в виде html
     */
    public void setTextHtml(String textHtml) {
        tooltipText.setHTML(textHtml);
    }

    private void showPopup() {
        tooltip.getElement().getStyle().setOpacity(0);
        tooltip.show();
        showTimer.scheduleRepeating(SHOW_TIMER_DELAY);
    }

    private void hidePopup() {
        showTimer.cancel();
        hideTimer.scheduleRepeating(HIDE_TIMER_DELAY);
    }

}
