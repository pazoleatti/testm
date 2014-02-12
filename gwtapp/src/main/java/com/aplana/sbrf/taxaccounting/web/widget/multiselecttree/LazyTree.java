package com.aplana.sbrf.taxaccounting.web.widget.multiselecttree;

import com.aplana.sbrf.taxaccounting.web.widget.multiselecttree.event.HasLazyTreeSelectionHandlers;
import com.aplana.sbrf.taxaccounting.web.widget.multiselecttree.event.LazyTreeSelectionEvent;
import com.aplana.sbrf.taxaccounting.web.widget.multiselecttree.event.LazyTreeSelectionHandler;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.*;

import java.util.*;

/**
 * Дерево множественного выбора.
 *
 * @author aivanov
 */
public class LazyTree<H extends LazyTreeItem> extends Tree implements HasLazyTreeSelectionHandlers<H> {

    private List<H> selectedItems = new LinkedList<H>();

    /**
     * Выбирать ли дочерние элементы при выборе узла.
     */
    private boolean isSelectChildViaParent;

    /**
     * Признак возможности выбора нескольких узлов дерева.
     */
    protected Boolean multiSelection;


    public interface MultiSelectTreeResources extends Tree.Resources {

        @ImageResource.ImageOptions
        ImageResource treeClosed();

        @ImageResource.ImageOptions
        ImageResource treeOpen();
    }

    public static MultiSelectTreeResources resources = GWT.create(MultiSelectTreeResources.class);

    /**
     * Дерево множественного выбора.
     */
    public LazyTree() {
        this(true);
    }

    /**
     * Дерево множественного выбора.
     *
     * @param multiSelection true - выбрать несколько элементов, false - выбрать один элемент
     */
    public LazyTree(Boolean multiSelection) {
        super(resources);
        this.multiSelection = multiSelection;
        setAnimationEnabled(true);
        setScrollOnSelectEnabled(false);
        super.addSelectionHandler(new SelectionHandler<TreeItem>() {
            @Override
            public void onSelection(SelectionEvent<TreeItem> event) {
                // По завершению этого метода итем становится селектальным
                LazyTreeItem lazyTreeItem = (LazyTreeItem) event.getSelectedItem();

                Boolean multiSelect = lazyTreeItem.isMultiSelection();

                if (multiSelect != null) {

                    if (!multiSelect) {
                        if (!selectedItems.isEmpty()) {
                            if (selectedItems.get(0).equals(lazyTreeItem)) {
                                return;
                            }
                            // для предыдушего радио-значения удаляем выделение
                            selectedItems.get(0).setItemState(null);
                        }
                        selectedItems.clear();
                        lazyTreeItem.setItemState(true);
                        selectedItems.add((H) lazyTreeItem);
                    } else {
                        Boolean isSelected = lazyTreeItem.isSelected();
                        if (!isSelected) {
                            lazyTreeItem.setItemState(true);
                            selectedItems.add((H) lazyTreeItem);
                        } else {
                            lazyTreeItem.setItemState(null);
                            selectedItems.remove((H) lazyTreeItem);
                        }
                    }
                    LazyTreeSelectionEvent.fire(LazyTree.this, ((H) lazyTreeItem));
                }

            }
        });
    }

    public boolean isSelectChildViaParent() {
        return isSelectChildViaParent;
    }

    /**
     * Установить выбирать ли дочерние элементы при выборе узла дерева.
     */
    public void setSelectChildViaParent(boolean selectChildViaParent) {
        this.isSelectChildViaParent = selectChildViaParent;
    }

    /**
     * Добавить элемент в первый уровень дерева.
     */
    public void addTreeItem(H item) {
        addTreeItem(null, item);
    }

    public void addItem(H item) {
        item.addItem("Загрузка...");
        super.addItem(item);
    }

    /**
     * Добавить элемент в дерево.
     *
     * @param parent родительский элемент
     * @param item   добавляемый узел
     */
    public void addTreeItem(H parent, H item) {
        if (parent != null) {
            parent.addItem(item);
        } else {
            addItem(item);
        }
    }


    @Override
    @SuppressWarnings("fallthrough")
    public void onBrowserEvent(Event event) {

        int eventType = DOM.eventGetType(event);

        // Блокируем нажатия клавиш. Приходится идти на такой
        // шаг потому что опять же постоянно срабатывает смена селекта
        switch (eventType) {
            case Event.ONKEYDOWN:
            case Event.ONKEYPRESS:
            case Event.ONKEYUP:
                event.stopPropagation();
                event.preventDefault();
                return;
        }

        if(eventType == Event.ONCLICK ||
                eventType == Event.ONMOUSEDOWN ||
                eventType == Event.ONMOUSEUP ){
            event.preventDefault();
            event.stopPropagation();
        }
        super.onBrowserEvent(event);
    }


    /**
     * Найти все дочерние элементы узла. Поиск рекурсивный.
     *
     * @param list список дочерних элементов
     * @param item узел для которого ищутся дочерние
     */
    private void findAllChild(List<H> list, H item) {
        list.add(item);
        if (item.getChildCount() > 0) {
            for (int i = 0; i < item.getChildCount(); i++) {
                findAllChild(list, (H) item.getChild(i));
            }
        }
    }

    /**
     * Получить дочерние элементы узла.
     *
     * @param item узел для которого ищутся узлы
     * @return список дочерних элеметов
     */
    private List<H> getItemChild(H item) {
        List<H> list = new ArrayList<H>();
        if (item.getChildCount() > 0) {
            for (int i = 0; i < item.getChildCount(); i++) {
                list.add((H) item.getChild(i));
            }
        }
        return list;
    }

    @Override
    public void setSelectedItem(TreeItem item) {
        setSelectedItem(item, true);
    }

    @Override
    public void setSelectedItem(TreeItem item, boolean fireEvents) {
        // заглушка
    }

    public H getSelectedItem() {
        // заглушка
        return null;
    }

    public List<H> getSelectedItems() {
        return Collections.unmodifiableList(selectedItems);
    }

    public void clearSelection() {
        for (H item : selectedItems) {
            item.setItemState(null);
        }
        selectedItems.clear();
    }

    /**
     * Удалить элемент из дерева.
     */
    public void removeItem(H item) {
        item.remove();
    }

    /**
     * Удалить элементы из дерева.
     */
    public void removeItems(List<H> items) {
        for (H i : items) {
            super.removeItem(i);
        }
    }

    /**
     * Получить признак возможности выбора нескольких узлов дерева.
     */
    public boolean isMultiSelection() {
        return multiSelection;
    }

//    /**
//     * Фильтр элементов дерева по названию.
//     *
//     * @param filter строка для фильтра
//     */
//    public void filter(String filter) {
//        List<H> list = getItems();
//        if (filter == null || "".equals(filter)) {
//            for (H item : list) {
//                item.setVisible(true);
//            }
//            return;
//        }
//        for (H item : list) {
//            String itemValue = item.getName().toLowerCase();
//            if (itemValue.contains(filter.toLowerCase())) {
//                item.setVisible(true);
//                H parent = (H) item.getParentItem();
//                while (parent != null) {
//                    parent.setVisible(true);
//                    parent.setState(true);
//                    parent = (H) parent.getParentItem();
//                }
//            } else {
//                item.setVisible(false);
//                item.setState(false);
//            }
//        }
//    }

    @Override
    public HandlerRegistration addSelectionHandler(SelectionHandler<TreeItem> handler) {
        // блокируем регистрацию этого события, у нас будет свое
        // обходим потому что наша будет работать для мультиселекта
        return null;
    }

    @Override
    public HandlerRegistration addLazyTreeSelectionHandler(LazyTreeSelectionHandler<H> handler) {
        return addHandler(handler, LazyTreeSelectionEvent.getType());
    }
}