package com.aplana.sbrf.taxaccounting.web.widget.multiselecttree;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.editor.client.LeafValueEditor;
import com.google.gwt.event.logical.shared.OpenHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.*;

import java.util.*;

/**
 * Дерево множественного выбора.
 *
 * @author rtimerbaev
 */
public abstract class MultiSelectTree<H extends List, T extends MultiSelectTreeItem> extends Composite implements HasValue<H>, LeafValueEditor<H> {

    interface Binder extends UiBinder<Widget, MultiSelectTree> {
    }

    private static Binder binder = GWT.create(Binder.class);

    @UiField
    VerticalPanel labelPanel;

    /** Заголовок. */
    @UiField
    Label label;

    @UiField
    ScrollPanel scrollPanel;

    /** Дерево. */
    @UiField(provided = true)
    protected Tree tree;

    /** Мапа для получения узлов дерева по чекбоксу. */
    private Map<Widget, T> treeItemsHash = new HashMap<Widget, T>();

    /** Выбирать ли дочерние элементы при выборе узла. */
    private boolean selectChild;

    /** Признак возможности выбора нескольких узлов дерева. */
    protected boolean multiSelection;

    /** Признак поиска на точное соответствие. */
    protected boolean exactSearch;

    private final String groupName = "treeGroup_" + this.hashCode();

    public interface Style extends CssResource {
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

        @Override
        @ImageResource.ImageOptions
        ImageResource treeClosed();

        @Override
        @ImageResource.ImageOptions
        ImageResource treeOpen();
    }
    MultiSelectTreeResources resources = GWT.create(MultiSelectTreeResources.class);

    /** Дерево множественного выбора. */
    public MultiSelectTree() {
        this("");
    }

    /**
     * Дерево множественного выбора.
     *
     * @param text заголовок
     */
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
        setValueChangeHandler();
    }

    /**
     * Проверить содержиться ли среди списка значений элемент со значением id.
     *
     * @param values список значений
     * @param id идентификатор узла
     */
    protected abstract boolean containInValues(H values, Integer id);

    /**
     * Сравнить идентификатор значения с идентификатором узла.
     *
     * @param value значение, идентификатор которого надо проверить
     * @param id идентификатор узла
     */
    protected abstract boolean equalsValue(Object value, Integer id);

    /** Получить выбранные элементы. */
    @Override
    public abstract H getValue();

    @Override
    public final void setValue(H value) {
        setValue(value, false);
    }

    @Override
    public void setValue(H values, boolean fireEvents) {
        if (values == null || values.isEmpty()) {
            for (T item : getItems()) {
                item.setValue(false);
            }
            return;
        }
        for (T item : getItems()) {
            // выставлять или все значения или только первое
            boolean isContain = (multiSelection ? containInValues(values, item.getId()) : equalsValue(values.get(0), item.getId()));
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
    public final HandlerRegistration addValueChangeHandler(ValueChangeHandler<H> handler) {
        return addHandler(handler, ValueChangeEvent.getType());
    }

    /** Получить все элементы дерева. */
    public List<T> getItems() {
        return getAllItems(false);
    }

    /** Получить все элементы дерева. */
    public List<T> getItemsWithFilter() {
        return getAllItems(true);
    }

    private List<T> getAllItems(boolean withFilter) {
        List<T> result = new ArrayList<T>();
        for (int i = 0; i < tree.getItemCount(); i++) {
            TreeItem item = tree.getItem(i);
            if (withFilter) {
                if (item.isVisible()) {
                    findAllChildWithFilter(result, (T) item);
                }
            } else {
                findAllChild(result, (T) item);
            }
        }
        return result;
    }

    public void selectAll(){
        setSelectOrUnselect(true);
    }

    public void unselectAll(){
        setSelectOrUnselect(false);
    }

    /**
     * Выделить всех чилдов или развыделить
     * @param selectOrUnselect true - выдетиь все, false развыделить
     */
    protected void setSelectOrUnselect(boolean selectOrUnselect){
        List<T> items = getItemsWithFilter();
        for (T item : items) {
            if(item.isVisible()){
                item.setValue(selectOrUnselect);
            }
        }
        ValueChangeEvent.fire(this, getValue());
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
    public void setTreeItems(List<T> items) {
        clear();
        if (items == null || items.isEmpty()) {
            return;
        }
        // заполнение дерева и для каждого узла создание чекбоксов
        for (T item : items) {
            item.setState(true);
            addTreeItem(null, item);
        }
    }

    /** Добавить элемент в первый уровень дерева. */
    public void addTreeItem(T item) {
        addTreeItem(null, item);
    }

    /**
     * Добавить элемент в дерево.
     *
     * @param parent родительский элемент
     * @param item добавляемый узел
     */
    public void addTreeItem(T parent, T item) {
        if (parent != null) {
            parent.addItem(item);
        } else {
            tree.addItem(item);
        }
        // получить все дочерние элементы узла и выделить для них checkBox'ы
        for (T child : getAllChild(item)) {
            child.setGroup(groupName);
            if (child.isMultiSelection() == null) {
                continue;
            }
            child.setMultiSelection(multiSelection);
            treeItemsHash.put(child.getWidget(), child);
            child.getWidget().getElement().getParentElement().getParentElement().addClassName(style.msiTreeItem());
        }

        /** Обновить верстку узлов дерева. */
        // задать им ширину 100% + найти картинки +/- и задать им ширину 16px
        NodeList<Element> tableTags = item.getElement().getElementsByTagName("table");
        for (int i = 0; i < tableTags.getLength(); i++) {
            Element tableTag = tableTags.getItem(i);
            tableTag.addClassName(style.msiTableTag());
            NodeList<Element> tdTags = tableTag.getElementsByTagName("td");
            tdTags.getItem(0).addClassName(style.msiImg());
        }
    }

    /**
     * Найти все дочерние элементы узла
     * @param item если null то и включая корненые элементы
     * @return плоский список веток
     */
    public List<T> getAllChild(T item){
        List<T> result = new ArrayList<T>();
        findAllChild(result, item);
        return result;
    }

    /**
     * Найти все дочерние элементы узла. Поиск рекурсивный.
     *
     * @param list список дочерних элементов
     * @param item узел для которого ищутся дочерние
     */
    private void findAllChild(List<T> list, T item) {
        if (item == null) {
            if (getItemCount() > 0) {
                for (int i = 0; i < getItemCount(); i++) {
                    findAllChild(list, getItem(i));
                }
            }
        } else {
            list.add(item);
            if (item.getChildCount() > 0) {
                for (int i = 0; i < item.getChildCount(); i++) {
                    findAllChild(list, (T) item.getChild(i));
                }
            }
        }
    }

    /**
     * Найти все дочерние элементы узла учитывая фильтр. Поиск рекурсивный.
     *
     * @param list список дочерних элементов
     * @param item узел для которого ищутся дочерние
     */
    private void findAllChildWithFilter(List<T> list, T item) {
        if (item == null) {
            if (getItemCount() > 0) {
                for (int i = 0; i < getItemCount(); i++) {
                    T rootItem = getItem(i);
                    if (rootItem.isVisible()) {
                        findAllChildWithFilter(list, rootItem);
                    }
                }
            }
        } else {
            list.add(item);
            if (item.getChildCount() > 0) {
                for (int i = 0; i < item.getChildCount(); i++) {
                    TreeItem child = item.getChild(i);
                    if (child.isVisible()) {
                        findAllChildWithFilter(list, (T) item.getChild(i));
                    }

                }
            }
        }
    }

    public String getHeader() {
        return label.getText();
    }

    public final void setHeader(String text) {
        label.setText(text == null ? "" : text);
    }

    @Override
    public final void setWidth(String width) {
        super.setWidth(width);
        scrollPanel.setWidth(width);
    }

    @Override
    public final void setHeight(String height) {
        super.setHeight(height);
        scrollPanel.setHeight(height);
    }

    public boolean isSelectChild() {
        return selectChild;
    }

    /** Установить выбирать ли дочерние элементы при выборе узла дерева. */
    public void setSelectChild(boolean selectChild) {
        this.selectChild = selectChild;
    }

    /** Получить выделенный узел. */
    public T getSelectedItem() {
        return (T) tree.getSelectedItem();
    }

    /** Удалить элемент из дерева. */
    public void removeItem(T item) {
        item.remove();
    }

    /** Удалить элементы из дерева. */
    public void removeItems(List<T> items) {
        for (T i : items) {
            tree.removeItem(i);
        }
    }

    /** Получить количество корневых элементов. */
    public int getItemCount() {
        return tree.getItemCount();
    }

    /** Получить элемент дерева по индексу. */
    public T getItem(int index) {
        return (T) tree.getItem(index);
    }

    /** Получить признак возможности выбора нескольких узлов дерева. */
    public boolean isMultiSelection() {
        return multiSelection;
    }

    /** Установить признак возможности выбора нескольких узлов дерева. */
    public final void setMultiSelection(boolean multiSelection) {
        this.multiSelection = multiSelection;
        for (T i : getItems()) {
            if (i.isMultiSelection() != null) {
                i.setMultiSelection(multiSelection);
            }
        }
        setValue(null);
    }

    public void setHeaderVisible(boolean value) {
        labelPanel.setVisible(value);
    }

    public boolean getHeaderVisible() {
        return labelPanel.isVisible();
    }

    public Iterator<TreeItem> treeItemIterator() {
         return tree.treeItemIterator();
    }

    public void addOpenHandler(OpenHandler<TreeItem> handler) {
        tree.addOpenHandler(handler);
    }

    /** Установить обработчик изменения значения элемента дерева (выбрать дочерние). */
    private void setValueChangeHandler() {
        addValueChangeHandler(new ValueChangeHandler<H>() {
            @Override
            public void onValueChange(ValueChangeEvent<H> event) {
                if (selectChild && multiSelection && getSelectedItem() != null) {
                    // обновить значения у всех дочерних элементов
                    CheckBox widget = (CheckBox) getSelectedItem().getWidget();
                    T item = treeItemsHash.get(widget);
                    for (T i : getAllChild(item)) {
                        if (i.isVisible()) {    // визиблом управляет фильтр
                            i.setValue(widget.getValue());
                        }
                    }
                }
            }
        });
    }

    /**
     * Фильтр элементов дерева по названию.
     *
     * @param filter строка для фильтра
     * @param exactSearch точное соответствие
     */
    public void filter(String filter, boolean exactSearch) {
        String lowerFilter = filter.toLowerCase();
        if ("".equals(filter)) {
            for (T item : getItems()) {
                item.setVisible(true);
                item.setText(item.getText());//снимаем подсветку
            }
            return;
        }
        for (T item : getItems()) {
            item.highLightText(filter);
            String itemValue = item.getText().toLowerCase();
            if (!exactSearch && itemValue.contains(lowerFilter) || exactSearch && itemValue.equals(lowerFilter)) {
                item.setVisible(true);
                T parent = (T) item.getParentItem();
                while (parent != null) {
                    parent.setVisible(true);
                    parent.setState(true);
                    parent = (T) parent.getParentItem();
                }
            } else {
                item.setValue(false);
                item.setState(false);
                item.setVisible(false);
            }
        }
        ValueChangeEvent.fire(this, getValue());
    }

    public boolean isExactSearch() {
        return exactSearch;
    }

    public void setExactSearch(boolean exactSearch) {
        this.exactSearch = exactSearch;
    }
}