package com.aplana.sbrf.taxaccounting.web.widget.multiselecttree;

import com.aplana.sbrf.taxaccounting.web.widget.multiselecttree.event.HasLazyTreeSelectionHandlers;
import com.aplana.sbrf.taxaccounting.web.widget.multiselecttree.event.LazyTreeSelectionEvent;
import com.aplana.sbrf.taxaccounting.web.widget.multiselecttree.event.LazyTreeSelectionHandler;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.view.client.*;

import java.util.*;

/**
 * Дерево множественного выбора.
 *
 * @author aivanov
 */
public class LazyTree<H extends LazyTreeItem> extends Tree implements HasLazyTreeSelectionHandlers<H> {

    public interface MultiSelectTreeResources extends Tree.Resources {
        @Override
        @ImageResource.ImageOptions
        ImageResource treeClosed();

        @Override
        @ImageResource.ImageOptions
        ImageResource treeOpen();
    }

    public static MultiSelectTreeResources resources = GWT.create(MultiSelectTreeResources.class);

    private ProvidesKey<H> providesKey;

    private SetSelectionModel<H> selectionModel;
    /* Выбирать ли дочерние элементы при выборе узла. */
    private boolean isSelectChildViaParent;
    /* Признак возможности выбора нескольких узлов дерева. */
    private Boolean multiSelect;
    private List<H> loadedItems = new ArrayList<H>();
    private Integer itemsCount = 0;
    private HandlerRegistration selectionHandlerRegistration;
    DivElement glass;

    /**
     * Дерево множественного выбора.
     */
    public LazyTree(ProvidesKey<H> providesKey) {
        this(true, providesKey);
    }

    /**
     * Дерево множественного выбора.
     *
     * @param multiSelect true - выбрать несколько элементов, false - выбрать один элемент
     */
    public LazyTree(Boolean multiSelect, ProvidesKey<H> providesKey) {
        super(resources);
        this.multiSelect = multiSelect;
        this.providesKey = providesKey;
        setAnimationEnabled(true);
        setScrollOnSelectEnabled(false);

        super.addSelectionHandler(new SelectionHandler<TreeItem>() {
            @Override
            public void onSelection(SelectionEvent<TreeItem> event) {
                // По завершению этого метода итем становится селектальным
                H item = (H) event.getSelectedItem();

                Boolean multiSelect = item.isMultiSelection();

                if (multiSelect != null && multiSelect) {
                    setSelected(item, !item.isSelected());
                } else {
                    clearSelection();
                    setSelected(item, true);
                }
                LazyTreeSelectionEvent.fire(LazyTree.this, item);
            }
        });

        this.selectionModel = getSelectionModel(multiSelect, providesKey);

        selectionHandlerRegistration = selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                LazyTree.this.onSelectionChange();
            }
        });

        glass = Document.get().createDivElement();
        glass.setAttribute("id", "666");

        Style style = glass.getStyle();
        style.setOpacity(0.5);
        style.setBackgroundColor("#ffffff");
        style.setPosition(Style.Position.ABSOLUTE);
        style.setLeft(0, Style.Unit.PX);
        style.setTop(0, Style.Unit.PX);
        style.setRight(0, Style.Unit.PX);
        style.setBottom(0, Style.Unit.PX);
        style.setZIndex(10); // Maximum z-index
    }

    private void onSelectionChange(){
    }

    @Override
    public void clear() {
        super.clear();
        loadedItems = new ArrayList<H>();
        itemsCount = 0;
    }

    public void setSelected(H refBookUiTreeItem, boolean selected) {
        selectionModel.setSelected(refBookUiTreeItem, selected);
        refBookUiTreeItem.setItemState(selected ? true : null);
    }

    private Set<H> getSelectedSet(){
        return selectionModel.getSelectedSet();
    }

    public boolean isSelectChildViaParent() {
        return isSelectChildViaParent;
    }

    /* Установить выбирать ли дочерние элементы при выборе узла дерева. */
    public void setSelectChildViaParent(boolean selectChildViaParent) {
        this.isSelectChildViaParent = selectChildViaParent;
    }

    public boolean getMultiSelect() {
        return multiSelect;
    }

    public void setMultiSelect(Boolean multiSelect) {
        this.multiSelect = multiSelect;

        clearSelection();

        this.selectionModel = getSelectionModel(this.multiSelect, providesKey);
        selectionHandlerRegistration.removeHandler();

        selectionHandlerRegistration = selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                LazyTree.this.onSelectionChange();
            }
        });
    }

    /**
     * Добавить элемент в первый уровень дерева.
     */
    public void addTreeItem(H item) {
        addTreeItem(null, item);
    }

    public void addItem(H item) {
        if (item.getChildCount() == 0 && !item.isChildLoaded()) {
            item.addItem("Загрузка...");
        }
        ensureSelection(item);
        super.addItem(item);
        itemsCount++;
    }

    /**
     * Проверка что этот элемент содержится в списке выделенных
     * Если содержится то выделяем цветом, и переопределяем объект что содержится в списке выделенных
     * @param item объект для проверки
     */
    private void ensureSelection(H item){
        if (selectionModel.isSelected(item)){
            item.setItemState(true);
            selectionModel.setSelected(item, true);
        }
    }

    /**
     * Добавить элемент в дерево.
     *
     * @param parent родительский элемент
     * @param item   добавляемый узел
     */
    public void addTreeItem(H parent, H item) {
        ensureSelection(item);
        if (parent != null) {
            parent.addItem(item);
            itemsCount++;
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
     * Обход по загруженным элементам всего дерева, исключая те элементы которые служат для отображения "Загрузки"
     *
     * @param parent узел для которого ищутся дочерние, если null что ищется с root'a
     * @return list список элементов
     */
    public List<H> getLoadedItems(H parent) {
        List<H> hs = new LinkedList<H>();
        findAllChild(hs, parent);
        return hs;
    }

    public List<H> getAllLoadedItems() {
        if(loadedItems.size()!= itemsCount){
            loadedItems.clear();
            List<H> loadedItems1 = getLoadedItems(null);
            loadedItems.addAll(loadedItems1);
            itemsCount = loadedItems1.size();
        }
        return loadedItems;
    }

    /**
     * Обход по загруженным элементам всего дерева, исключая те элементы которые служат для отображения "Загрузки"
     *
     * @param list список элементов
     * @param item узел для которого ищутся дочерние, если null что ищется с root'a
     */
    public void findAllChild(List<H> list, H item) {
        if (item == null) {
            if (getItemCount() > 0) {
                for (int j = 0; j < getItemCount(); j++) {
                    H par = (H) getItem(j);
                    list.add(par);
                    if (par.getChildCount() > 0) {
                        for (int i = 0; i < par.getChildCount(); i++) {
                            if (par.getChild(i) instanceof LazyTreeItem) {
                                findAllChild(list, (H) par.getChild(i));
                            }
                        }
                    }
                }
            }
        } else {
            list.add(item);
            if (item.getChildCount() > 0) {
                for (int i = 0; i < item.getChildCount(); i++) {
                    if (item.getChild(i) instanceof LazyTreeItem) {
                        findAllChild(list, (H) item.getChild(i));
                    }
                }
            }
        }
    }

    @Override
    @Deprecated
    public void setSelectedItem(TreeItem item) {
        setSelectedItem(item, true);
    }

    @Override
    @Deprecated
    public void setSelectedItem(TreeItem item, boolean fireEvents) {
        // заглушка
    }

    @Override
    @Deprecated
    public H getSelectedItem() {
        // заглушка
        return null;
    }

    public Set<H> getSelectedItems() {
        for (H h : getAllLoadedItems()) {
            ensureSelection(h);
        }
        return Collections.unmodifiableSet(selectionModel.getSelectedSet());
    }

    /**
     * Очистка модели выделенности
     */
    public void clearSelection() {
        for (H item : selectionModel.getSelectedSet()) {
            //очищаем цвет
            item.setItemState(null);
        }
        selectionModel.clear();
    }

    /**
     * Открытие нодов всех родителей по иерархрии вверх у чилда
     * @param child чилд
     */
    public void openAllParent(H child){
        H parent = (H)child.getParentItem();
        if(parent != null){
            parent.setState(true);
            openAllParent(parent);
        }
    }

    /**
     * Удалить элемент из дерева.
     */
    public void removeItem(H item) {
        loadedItems.remove(item);
        itemsCount--;
        item.remove();
    }

    /**
     * Удалить элемент из дерева.
     */
    public void removeChildItems(H parent) {
        for(int i = 0; i < parent.getChildCount(); i++){
            loadedItems.remove(parent.getChild(i));
            itemsCount--;
            parent.getChild(i).remove();
        }
        parent.setChildLoaded(false);
        parent.addItem("Загрузка...");
        parent.setState(false);
    }

    private SetSelectionModel<H> getSelectionModel(Boolean multiSelect, ProvidesKey<H> key) {
        return multiSelect != null && multiSelect ?
                new MultiSelectionModel<H>(key) {
                    // Переопределение методов - попытка убрать задержку при сеттинге селекта
                    @Override
                    protected boolean isEventScheduled() {
                        return false;
                    }
                    @Override
                    protected void scheduleSelectionChangeEvent() {
                        fireSelectionChangeEvent();
                    }
                } :
                new SingleSelectionModel<H>(key) {
                    // Переопределение методов - попытка убрать задержку при сеттинге селекта
                    @Override
                    public boolean isEventScheduled() {
                        return false;
                    }
                    @Override
                    public void scheduleSelectionChangeEvent() {
                        fireSelectionChangeEvent();
                    }
                };
    }

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

    public void setEnabled(boolean isEnabled) {
        this.getElement().appendChild(glass);
        if (isEnabled) {
            this.getElement().removeChild(glass);
        }
    }
}