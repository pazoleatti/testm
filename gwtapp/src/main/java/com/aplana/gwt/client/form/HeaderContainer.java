package com.aplana.gwt.client.form;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiChild;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Контейнер для заголовка типовой формы
 * Состоит из трех обязательных частей. Порядок добавления не важен.
 * <p/>
 * Пример:
 * <f:HeaderContainer>
 * <f:right>
 * <f:Right>
 * <g:Label text="Режим редактирования"/>
 * ....
 * </f:Right>
 * </f:right>
 * <f:resizable>
 * <f:Resizable>
 * <g:Label text="Сводная форма начисленных доходов (доходы сложные) Очень длинный заголовок бла бла бал ба лаб ла бал аб лалалалалалал ла ла"/>
 * </f:Resizable>
 * </f:resizable>
 * <f:left>
 * <f:Left>
 * <g:Label text="Список налоговых форм"/>
 * ...
 * </f:Left>
 * </f:left>
 * </f:HeaderContainer>
 * <p/>
 * <p/>
 *
 * @author aivanov
 */
public class HeaderContainer extends ComplexPanel {

    protected GlobalResources.Style style;

    public HeaderContainer() {
        setElement(DOM.createDiv());

        GlobalResources.Style style = GlobalResources.INSTANCE.style();
        style.ensureInjected();

        //setStyle
        getElement().addClassName(style.typicalFormHeader());
    }

    /**
     * Добавление левой части.
     * Левая часть всегда будет нарисована первой
     *
     * @param widget компонент класса Left
     */
    @UiChild(limit = 1)
    public void addLeft(Left widget) {
        if (getWidgetCount() > 0) {
            insert(widget, getElement(), 0, true);
        } else {
            add(widget, getElement());
        }
    }

    /**
     * Добавление правой части.

     * @param widget компонент класса Right
     */
    @UiChild(limit = 1)
    public void addRight(Right widget) {
        if (getWidgetCount() == 0) {
            add(widget, getElement());
        } else if (getWidgetCount() == 1) {
            if (getWidget(0) instanceof Left) {
                add(widget, getElement());
            } else if (getWidget(0) instanceof Resizable) {
                insert(widget, getElement(), 0, true);
            }
        } else if (getWidgetCount() == 2) {
            insert(widget, getElement(), 1, true);
        }
    }

    /**
     * Добавление резиновой части заголовка.
     *
     * @param widget компонент класса Right
     */
    @UiChild(limit = 1)
    public void addResizable(Resizable widget) {
        insert(widget, getElement(), getWidgetCount(), true);
    }

    /**
     * Добавление других элементов запрещенно
     * @param widget другой элемент
     */
    @Override
    public void add(Widget widget) {
        GWT.log("HeaderContainer may contain only childs of class HeaderChild.");
        throw new IllegalStateException(
                "HeaderContainer may contain only childs of class HeaderChild.");
    }

}
