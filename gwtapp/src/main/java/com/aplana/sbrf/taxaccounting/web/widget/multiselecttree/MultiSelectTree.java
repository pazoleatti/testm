package com.aplana.sbrf.taxaccounting.web.widget.multiselecttree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.event.logical.shared.*;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.*;

/**
 * Дерево с колонкой множественного выбора.
 */
public class MultiSelectTree extends Composite implements HasConstrainedValue<List<Integer>> {

    interface Binder extends UiBinder<Widget, MultiSelectTree> {
    }

    private static Binder binder = GWT.create(Binder.class);

    /** Заголовок. */
    @UiField
    Label label;

    @UiField
    ScrollPanel scrollPanel;

    /** Дерево. */
    @UiField
    Tree tree;

    /** Мапа для получения узлов дерева по чекбоксу. */
    private HashMap<Widget, MultiSelectTreeItem> treeItemsHash = new HashMap<Widget, MultiSelectTreeItem>();

    /** Выбирать ли дочерние элементы при выборе узла. */
    private boolean selectChild;

    /** Дерево с колонкой множественного выбора. */
    public MultiSelectTree() {
        this("");
    }

    public MultiSelectTree(String text) {
        this(text, false);
    }

    public MultiSelectTree(String text, boolean selectChild) {
        initWidget(binder.createAndBindUi(this));
        setText(text);
        this.selectChild = selectChild;
    }

    @Override
    public List<Integer> getValue() {
        List<Integer> result = new ArrayList<Integer>();
        for (MultiSelectTreeItem item : getItems()) {
            CheckBox cb = (CheckBox) item.getWidget();
            if (cb.getValue()) {
                result.add(item.getId());
            }
        }
        return result;
    }

    @Override
    public void setValue(List<Integer> value) {
        setValue(value, false);
    }

    @Override
    public void setValue(List<Integer> values, boolean fireEvents) {
        if (values == null || values.isEmpty()) {
            return;
        }
        for (MultiSelectTreeItem item : getItems()) {
            boolean isContain = values.contains(item.getId());
            if (isContain) {
                // раскрыть дерево до выбранного элемента
                TreeItem parent = item.getParentItem();
                while (parent != null) {
                    parent.setState(true);
                    parent = parent.getParentItem();
                }
            }
            CheckBox cb = (CheckBox) item.getWidget();
            cb.setValue(isContain);
        }
        if (fireEvents) {
            ValueChangeEvent.fire(this, values);
        }
    }

    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<List<Integer>> handler) {
        return addHandler(handler, ValueChangeEvent.getType());
    }

    @Override
    public void setAcceptableValues(Collection<List<Integer>> values) {
        // TODO (Ramil Timerbaev)
    }

    private Collection<MultiSelectTreeItem> getItems() {
        List<MultiSelectTreeItem> result = new ArrayList<MultiSelectTreeItem>();
        for (int i = 0; i < tree.getItemCount(); i++) {
            TreeItem item = tree.getItem(i);
            findAllChild(result, (MultiSelectTreeItem) item);
        }
        return result;
    }

    /**
     * Заполнить дерево.
     *
     * @param items список данных
     */
    public void setTreeItems(List<MultiSelectTreeItem> items) {
        treeItemsHash.clear();
        // заполнение дерева и для каждого узла создание чекбоксов
        for (MultiSelectTreeItem item : items) {
            item.setState(true);
            this.tree.addItem(item);

            // получить все дочерние элементы узла и выделить для них checkBox'ы
            List<MultiSelectTreeItem> allChild = new ArrayList<MultiSelectTreeItem>();
            findAllChild(allChild, item);
            for (MultiSelectTreeItem child : allChild) {
                CheckBox widget = (CheckBox) child.getWidget();
                treeItemsHash.put(child.getWidget(), child);
                // двойной клик по узлу с чекбоксом
                widget.addDoubleClickHandler(new DoubleClickHandler() {
                    @Override
                    public void onDoubleClick(DoubleClickEvent event) {
                        Widget widget = (Widget) event.getSource();
                        MultiSelectTreeItem item = treeItemsHash.get(widget);
                        if (item.getChildCount() > 0) {
                            item.setState(!item.getState());
                        }
                    }
                });
                // изменение значения чекбокса
                widget.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
                    @Override
                    public void onValueChange(ValueChangeEvent<Boolean> event) {
                        if (!selectChild) {
                            return;
                        }
                        // обновить значения у всех дочерних элементов
                        CheckBox widget = (CheckBox) event.getSource();
                        MultiSelectTreeItem item = treeItemsHash.get(widget);
                        List<MultiSelectTreeItem> list = new ArrayList<MultiSelectTreeItem>();
                        findAllChild(list, item);
                        for (MultiSelectTreeItem i : list) {
                            ((CheckBox) i.getWidget()).setValue(widget.getValue());
                        }
                    }
                });

//                i.addClickHandler(new ClickHandler() {
//                    @Override
//                    public void onClick(ClickEvent event) {
//                        Window.alert(event.getSource().getClass().getName());
//                    }
//                });
            }
        }
    }

    /**
     * Найти все дочерние элементы узла. Поиск рекурсивный.
     *
     * @param list список дочерних элементов
     * @param item узел для которого ищутся дочерние
     */
    private void findAllChild(List<MultiSelectTreeItem> list, MultiSelectTreeItem item) {
        list.add(item);
        if (item.getChildCount() > 0) {
            for (int i = 0; i < item.getChildCount(); i++) {
                findAllChild(list, (MultiSelectTreeItem) item.getChild(i));
            }
        }
    }

    /**
     * Получить дочерние элементы узла.
     *
     * @param item узел для которого ищутся узлы
     * @return список дочерних элеметов
     */
    private List<MultiSelectTreeItem> getItemChild(MultiSelectTreeItem item) {
        List<MultiSelectTreeItem> list = new ArrayList<MultiSelectTreeItem>();
        if (item.getChildCount() > 0) {
            for (int i = 0; i < item.getChildCount(); i++) {
                list.add((MultiSelectTreeItem) item.getChild(i));
            }
        }
        return list;
    }

//    /** Добавить чекбоксы для дочерних элементов указанного узла. */
//    private void addCb(MultiSelectTreeItem item) {
//        addCb(cbHash.get(item.getId()));
//        // если узел раскрыт, то добавить еще и дочерние элементы
//        if (item.getState()) {
//            List<MultiSelectTreeItem> list = getItemChild(item);
//            for (MultiSelectTreeItem i : list) {
//                addCb(i);
//            }
//        }
//    }

    public String getText() {
        return label.getText();
    }

    public void setText(String text) {
        label.setText(text == null ? "" : text);
    }

    @Override
    public void setWidth(String width) {
        super.setWidth(width);
        scrollPanel.setWidth(width);
    }

    @Override
    public void setHeight(String height) {
        super.setHeight(height);
        scrollPanel.setHeight(height);
    }

    public boolean isSelectChild() {
        return selectChild;
    }

    public void setSelectChild(boolean selectChild) {
        this.selectChild = selectChild;
    }
}