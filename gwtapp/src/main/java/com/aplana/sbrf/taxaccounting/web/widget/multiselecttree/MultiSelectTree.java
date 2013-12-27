package com.aplana.sbrf.taxaccounting.web.widget.multiselecttree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.event.logical.shared.*;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.*;

/**
 * Дерево множественного выбора.
 */
public abstract class MultiSelectTree<H extends List> extends Composite implements HasValue<H> {

    interface Binder extends UiBinder<Widget, MultiSelectTree> {
    }

    private static Binder binder = GWT.create(Binder.class);

    /** Заголовок. */
    @UiField
    Label label;

    @UiField
    ScrollPanel scrollPanel;

    /** Дерево. */
    @UiField(provided = true)
    protected Tree tree;

    /** Мапа для получения узлов дерева по чекбоксу. */
    private HashMap<Widget, MultiSelectTreeItem> treeItemsHash = new HashMap<Widget, MultiSelectTreeItem>();

    /** Выбирать ли дочерние элементы при выборе узла. */
    private boolean selectChild;

    /** Признак возможности выбора нескольких узлов дерева. */
    protected boolean multiSelection;

    private String GROUP_NAME = "treeGroup_" + this.hashCode();

    public static interface Style extends CssResource {
        String msiHeader();
        String msiMainPanel();
        String msiTree();
        String msiTreePanel();
        String msiTreeItem();
        String msiTableTag();
        String msiImg();
    }

    @UiField
    Style style;

    public interface MultiSelectTreeResources extends Tree.Resources {

        @ImageResource.ImageOptions
        ImageResource treeClosed();

        @ImageResource.ImageOptions
        ImageResource treeOpen();
    }
    MultiSelectTreeResources resources = GWT.create(MultiSelectTreeResources.class);

    /** Дерево множественного выбора. */
    public MultiSelectTree() {
        this("");
    }

    /** Дерево множественного выбора. */
    public MultiSelectTree(String text) {
        this(text, true);
    }


    /**
     * Дерево множественного выбора.
     *
     * @param text заголовок
     * @param multiSelection true - выбрать несколько элементов, false - выбрать один элемент
     */
    public MultiSelectTree(String text, boolean multiSelection) {
        tree = new Tree(resources);
        initWidget(binder.createAndBindUi(this));
        setHeader(text);
        setMultiSelection(multiSelection);
    }


    public abstract H getValue();

    @Override
    public void setValue(H value) {
        setValue(value, false);
    }

    @Override
    public void setValue(H values, boolean fireEvents) {
        if (values == null || values.isEmpty()) {
            for (MultiSelectTreeItem item : getItems()) {
                item.setValue(false);
            }
            return;
        }
        for (MultiSelectTreeItem item : getItems()) {
            // выставлять или все значения или только первое
            boolean isContain = (multiSelection ? values.contains(item.getId()) : values.get(0).equals(item.getId()));
            if (isContain) {
                // раскрыть дерево до выбранного элемента
                TreeItem parent = item.getParentItem();
                while (parent != null) {
                    parent.setState(true);
                    parent = parent.getParentItem();
                }
            }
            item.setValue(isContain);
        }
        if (fireEvents) {
            ValueChangeEvent.fire(this, values);
        }
    }

    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<H> handler) {
        return addHandler(handler, ValueChangeEvent.getType());
    }

    protected List<MultiSelectTreeItem> getItems() {
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

    public void addTreeItem(MultiSelectTreeItem item) {
        addTreeItem(null, item);
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
            child.setMultiSelection(multiSelection);
            child.setGroup(GROUP_NAME);

            CheckBox widget = (CheckBox) child.getWidget();
            treeItemsHash.put(child.getWidget(), child);
            child.getPanel().getElement().addClassName(style.msiTreeItem());

            // изменение значения чекбокса
            widget.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
                @Override
                public void onValueChange(ValueChangeEvent<Boolean> event) {
                    if (!selectChild || !multiSelection) {
                        return;
                    }
                    // обновить значения у всех дочерних элементов
                    CheckBox widget = (CheckBox) event.getSource();
                    MultiSelectTreeItem item = treeItemsHash.get(widget);
                    List<MultiSelectTreeItem> list = new ArrayList<MultiSelectTreeItem>();
                    findAllChild(list, item);
                    for (MultiSelectTreeItem i : list) {
                        i.setValue(widget.getValue());
                    }
                }
            });
        }
        updateItems();
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

    public String getHeader() {
        return label.getText();
    }

    public void setHeader(String text) {
        label.setText(text == null ? "" : text);
    }

    @Override
    public void setWidth(String width) {
//        super.setWidth(width);
        scrollPanel.setWidth(width);
    }

    @Override
    public void setHeight(String height) {
//        super.setHeight(height);
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

    public int getItemCount() {
        return tree.getItemCount();
    }

    public MultiSelectTreeItem getItem(int index) {
        return (MultiSelectTreeItem) tree.getItem(index);
    }

    /** Получить признак возможности выбора нескольких узлов дерева. */
    public boolean isMultiSelection() {
        return multiSelection;
    }

    /** Установить признак возможности выбора нескольких узлов дерева. */
    public void setMultiSelection(boolean multiSelection) {
        this.multiSelection = multiSelection;
        for (MultiSelectTreeItem i : getItems()) {
            i.setMultiSelection(multiSelection);
        }
        setValue(null);
    }

    /** Обновить верстку узлов дерева. */
    public void updateItems() {
        for (int index = 0; index < tree.getItemCount(); index++) {
            MultiSelectTreeItem item = (MultiSelectTreeItem) tree.getItem(index);
            // найти все таблицы (узлы, у которых есть дочерние элементы, имеют ограниченную ширину)
            // и задать им ширину 100% + найти картинки +/- и задать им ширину 16px
            NodeList<Element> tableTags = item.getElement().getElementsByTagName("table");
            for (int i = 0; i < tableTags.getLength(); i++) {
                Element tableTag = tableTags.getItem(i);
                tableTag.addClassName(style.msiTableTag()); // tableTag.getStyle().setWidth(100, Style.Unit.PCT);
                NodeList<Element> tdTags = tableTag.getElementsByTagName("td");
                tdTags.getItem(0).addClassName(style.msiImg()); // tdTags.getItem(0).getStyle().setWidth(16, Style.Unit.PX);
            }
        }
    }
}