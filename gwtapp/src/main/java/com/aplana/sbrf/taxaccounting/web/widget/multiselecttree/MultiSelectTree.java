package com.aplana.sbrf.taxaccounting.web.widget.multiselecttree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.*;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.*;

/**
 * Дерево множественного выбора.
 */
public class MultiSelectTree extends Composite implements HasValue<List<Integer>> {

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

    public static interface Resources extends ClientBundle {
        public static final Resources INSTANCE = GWT.create(Resources.class);
        public static interface Style extends CssResource {
            String msiHeader();
            String msiMainPanel();
            String msiTree();
            String msiTreePanel();
            String msiTreeItem();
        }
        @ClientBundle.Source("MultiSelectTree.css")
        Style style();
    }
    private final Resources.Style style;

    /** Дерево с колонкой множественного выбора. */
    public MultiSelectTree() {
        this("");
    }

    /** Дерево с колонкой множественного выбора. */
    public MultiSelectTree(String text) {
        this(text, false);
    }

    /** Дерево с колонкой множественного выбора. */
    public MultiSelectTree(String text, boolean selectChild) {
        this.style = Resources.INSTANCE.style();
        style.ensureInjected();

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

    private Collection<MultiSelectTreeItem> getItems() {
        List<MultiSelectTreeItem> result = new ArrayList<MultiSelectTreeItem>();
        for (int i = 0; i < tree.getItemCount(); i++) {
            TreeItem item = tree.getItem(i);
            findAllChild(result, (MultiSelectTreeItem) item);
        }
        return result;
    }

    public void clear() {
        treeItemsHash.clear();
        tree.clear();
    }

    /**
     * Заполнить дерево.
     *
     * @param items список данных
     */
    public void setTreeItems(List<MultiSelectTreeItem> items) {
        clear();
        if (items == null || items.isEmpty()) {
            return;
        }
        // заполнение дерева и для каждого узла создание чекбоксов
        for (MultiSelectTreeItem item : items) {
            item.setState(true);
            addTreeItem(null, item);
        }
    }

    /**
     * Добавить элемент в дерево.
     *
     * @param parent родительский элемент
     * @param item добавляемый узел
     */
    public void addTreeItem(MultiSelectTreeItem parent, MultiSelectTreeItem item) {
        if (parent != null) {
            parent.addItem(item);
        } else {
            tree.addItem(item);
        }

        // получить все дочерние элементы узла и выделить для них checkBox'ы
        List<MultiSelectTreeItem> allChild = new ArrayList<MultiSelectTreeItem>();
        findAllChild(allChild, item);
        for (MultiSelectTreeItem child : allChild) {
            CheckBox widget = (CheckBox) child.getWidget();
            treeItemsHash.put(child.getWidget(), child);
            child.getPanel().getElement().addClassName(style.msiTreeItem());

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

    public MultiSelectTreeItem getSelectedItem() {
        return (MultiSelectTreeItem) tree.getSelectedItem();
    }

    public void removeItem(MultiSelectTreeItem item) {
        item.remove();
    }

    public void removeItems(List<MultiSelectTreeItem> items) {
        for (MultiSelectTreeItem i : items) {
            tree.removeItem(i);
        }
    }
}