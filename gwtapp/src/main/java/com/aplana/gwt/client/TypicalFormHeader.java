package com.aplana.gwt.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

import java.util.ArrayList;
import java.util.List;

/**
 * Компонент заголовка типовых форм
 * <p/>
 * Пример кода java:
 *
 *
 * @UiField(provided = true)
 * TypicalFormHeader formHeader;
 * ...
 * formHeader = new TypicalFormHeader();
 * formHeader.addLeftWidget(new Label("Список налоговых форм пример"));
 * formHeader.addLeftWidget(new Label("-"));
 * <p/>
 * Label label = new Label("Например кнопка");
 * label.getElement().getStyle().setProperty("fontSize", 20, Style.Unit.PX);
 * formHeader.addRightWidget(label);
 * formHeader.addRightWidget(new Label("Режим редактирования"));
 * formHeader.addMiddleWidget(new Label("Сводная форма начисленных доходов (доходы сложные) Очень длинный заголовок бла бла бал ба лаб ла бал аб лалалалалалал ла ла"));
 * <p/>
 * <p/>
 * В UiBinder:
 * <my:TypicalFormHeader ui:field="formHeader"/>
 *
 * @author aivanov
 */
public class TypicalFormHeader extends Composite {

    HTMLPanel container,
            right,
            left,
            middle,
            clearBoth;

    private Style style;

    private List<Widget> leftWidgets = new ArrayList<Widget>();
    private List<Widget> rightWidgets = new ArrayList<Widget>();

    public static interface Resources extends ClientBundle {

        /**
         * The styles used in this widget.
         */
        @Source("TypicalFormHeaderStyle.css")
        Style style();
    }

    public static interface Style extends CssResource {

        // главный контейнер
        String typicalFormHeader();

        // элементы справа
        String left();

        // элементы справа
        String right();

        /**
         * Скрывающаяся часть
         */
        @ClassName("resize-overflow")
        String resizeOverflow();

        /**
         * Доречние элементы
         */
        @ClassName("child-inherit")
        String childInherit();
    }

    private static Resources defaultResources;

    private static Style getStyle() {
        if (defaultResources == null) {
            defaultResources = GWT.create(Resources.class);
        }
        return defaultResources.style();
    }

    private void createAndSetupParts() {
        style = getStyle();
        style.ensureInjected();
        container = new HTMLPanel("");


        right = new HTMLPanel("");
        left = new HTMLPanel("");
        middle = new HTMLPanel("");
        clearBoth = new HTMLPanel("");
        clearBoth.getElement().getStyle().setProperty("clear", "both");
    }

    @Inject
    @UiConstructor
    public TypicalFormHeader() {
        createAndSetupParts();
        container = new HTMLPanel("");
        container.add(left);
        container.add(right);
        container.add(middle);
        container.add(clearBoth);

        initWidget(container);
        left.getElement().addClassName(style.left());
        right.getElement().addClassName(style.right());
        container.getElement().addClassName(style.typicalFormHeader());
    }

    public void addLeftWidget(Widget widget) {
        widget.getElement().addClassName(style.childInherit());
        left.add(widget);
    }

    public void addLeftWidget(List<Widget> widgets) {
        for (Widget widget : widgets) {
            addLeftWidget(widget);
        }
    }

    public void addRightWidget(Widget widget) {
        widget.getElement().addClassName(style.childInherit());
        right.add(widget);
    }

    public void addRightWidget(List<Widget> widgets) {
        for (Widget widget : widgets) {
            addLeftWidget(widget);
        }
    }

    /**
     * Помещает виджет в растягивающийся блок.
     * В блоке может быть только один виджет
     *
     * @param widget виджет
     */
    public void addMiddleWidget(Widget widget) {
        if (middle.getWidgetCount() < 1) {
            widget.getElement().addClassName(style.resizeOverflow());
            middle.add(widget);
        } else {
            throw new IllegalArgumentException(
                    "Only one OVERFLOW widget may be added");
        }

    }


}
