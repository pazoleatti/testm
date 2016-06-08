package com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.client;

import com.aplana.sbrf.taxaccounting.web.main.api.client.handler.DeferredInvokeHandler;
import com.aplana.sbrf.taxaccounting.web.widget.multiselecttree.LazyTree;
import com.aplana.sbrf.taxaccounting.web.widget.multiselecttree.event.LazyTreeSelectionEvent;
import com.aplana.sbrf.taxaccounting.web.widget.multiselecttree.event.LazyTreeSelectionHandler;
import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.client.event.CheckValuesCountHandler;
import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.client.event.ChildrenLoadedEvent;
import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.client.event.RootLoadedEvent;
import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.shared.model.PickerState;
import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.shared.model.RefBookRecordDereferenceValue;
import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.shared.model.RefBookTreeItem;
import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.shared.model.RefBookUiTreeItem;
import com.aplana.sbrf.taxaccounting.web.widget.utils.WidgetUtils;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.logical.shared.OpenEvent;
import com.google.gwt.event.logical.shared.OpenHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HasVisibility;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.Widget;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

import java.util.*;

/**
 * Представление для отображения иерархического справочника
 *
 * @author aivanov
 */
public class RefBookTreePickerView extends ViewWithUiHandlers<RefBookTreePickerUiHandlers>
        implements RefBookTreePickerPresenter.MyView, RefBookView, HasVisibility {


    interface Binder extends UiBinder<Widget, RefBookTreePickerView> {
    }

    private static Binder binder = GWT.create(Binder.class);

    @UiField(provided = true)
    LazyTree<RefBookUiTreeItem> tree;
    @UiField
    ScrollPanel scrollPanel;

    private Set<Long> longList = new LinkedHashSet<Long>();

    private Boolean isEnabledFireChangeEvent = true;
    private Boolean multiSelect = false;

    /* флаг что идет операция последовательного открывания веток */
    private boolean isOpeningOperation = false;
    /* итератор для последовательного автоматического открывания веток с загрузкой*/
    private Iterator<Long> iterator;

    public RefBookTreePickerView() {
        this(false, null);
    }

    public RefBookTreePickerView(final Boolean multiSelect, RefBookPicker refBookPicker) {
        this.multiSelect = multiSelect;
        tree = new LazyTree<RefBookUiTreeItem>(multiSelect, RefBookPickerUtils.TREE_KEY_PROVIDER);

        initWidget(binder.createAndBindUi(this));

        //TODO: подключение презентера на прямую, почему не инъкцией через модуль?
        // подключаем презентер
        new RefBookTreePickerPresenter(this);

        tree.addOpenHandler(new OpenHandler<TreeItem>() {
            @Override
            public void onOpen(OpenEvent<TreeItem> event) {
                RefBookUiTreeItem refBookUiTreeItem = (RefBookUiTreeItem) event.getTarget();
                //TODO: надо было бы использовать параметр RefBookTreeItem.hasChild, чтобы не делать запрос в базу
                if (!refBookUiTreeItem.isChildLoaded()) {
                    getUiHandlers().loadForItem(refBookUiTreeItem);
                    refBookUiTreeItem.setChildLoaded(true);
                }
            }
        });

        tree.addLazyTreeSelectionHandler(new LazyTreeSelectionHandler<RefBookUiTreeItem>() {
            @Override
            public void onSelected(LazyTreeSelectionEvent<RefBookUiTreeItem> event) {
                onSelection();
            }
        });
    }

    private void onSelection() {
        if (!isEnabledFireChangeEvent) {
            isEnabledFireChangeEvent = true;
        } else {
            widgetFireChangeEvent(getSelectedIds());
        }
    }

    @Override
    public void loadRoot(List<RefBookTreeItem> values, boolean openOnLoad) {
        tree.clear();
        for (RefBookTreeItem value : values) {
            RefBookUiTreeItem uiTreeItem = new RefBookUiTreeItem(value, multiSelect);
            getUiHandlers().highLightItem(uiTreeItem);
            tree.addTreeItem(uiTreeItem);
            if (openOnLoad) {
                uiTreeItem.setState(true);
            }
        }
        RootLoadedEvent.fire(this);
    }

    /**
     * Открыть каскадно все дочерние ветки из веток рута
     */
    public void cascadeOpen() {
        for (RefBookUiTreeItem uiTreeItem : tree.getAllLoadedItems()) {
            uiTreeItem.setState(true);
        }
    }

    @Override
    public void insertChildrens(RefBookUiTreeItem uiTreeItem, List<RefBookTreeItem> values, boolean openOnLoad) {
        for (RefBookTreeItem value : values) {
            RefBookUiTreeItem item = new RefBookUiTreeItem(value, multiSelect);
            getUiHandlers().highLightItem(item);
            tree.addTreeItem(uiTreeItem, item);
            if (openOnLoad) {
                item.setState(true);
            }
        }
        ChildrenLoadedEvent.fire(this, uiTreeItem);
        if (!openOnLoad) {
            tryOpen();
        }
    }

    @Override
    public List<RefBookTreeItem> getSelectionValues() {
        List<RefBookTreeItem> refBookTreeItems = new LinkedList<RefBookTreeItem>();
        for (RefBookUiTreeItem uiItem : getSelectedSet()) {
            refBookTreeItems.add(uiItem.getRefBookTreeItem());
        }
        return refBookTreeItems;
    }

    /**
     * Попытка выделить
     * @param preudoItems
     */
    private void trySelectValues(List<RefBookTreeItem> preudoItems) {
        for (RefBookTreeItem pseudoItem : preudoItems) {
            if (pseudoItem.getId() != null) {
                RefBookUiTreeItem uiTreeItem = new RefBookUiTreeItem(pseudoItem);
                tree.setSelected(uiTreeItem, true);
            }
        }
    }

    @Override
    public void setSelection(List<RefBookTreeItem> values) {
        if (values != null) {
            clearSelected(false);
            if (!values.isEmpty()) {
                for (RefBookTreeItem item : values) {
                    tree.setSelected(new RefBookUiTreeItem(item, multiSelect), true);
                }
                ensureVisibleSelectedItem();
            }
            widgetFireChangeEvent(getSelectedIds());
        }
    }

    @Override
    public void openListItems(List<Long> ids) {
        iterator = ids.iterator();
        isOpeningOperation = true;
        tryOpen();
    }

    /**
     * Последовательная загрузка родителей первого выделенного элемента и фокусировка на него в дереве
     */
    @Override
    public void ensureVisibleSelectedItem() {
        if (tree.isAttached()) {
            RefBookUiTreeItem selectItem = getSelectedItem();
            Long aLong = selectItem != null && selectItem.getRefBookTreeItem() != null ? selectItem.getRefBookTreeItem().getId() : null;
            if (aLong != null) {
                getUiHandlers().openFor(aLong, true);
            } else {
                ensureVisible(selectItem);
            }
        }
    }

    /**
     * Если в итераторе есть идентификаторы которые еще необходимо открыть дерево будет пытаться их открыть
     * Если ветка уже была загружена, он просто откроет и продолжится открывание
     * Если ветка не загружена, то он откроет, начнется подгрузка, и после подгрузки будет
     * продолжение алгоритма открывания см RefBookTreePickerView#insertChildrens()
     */
    private void tryOpen() {
        if (iterator != null && iterator.hasNext()) {
            Long aLong = iterator.next();
            RefBookUiTreeItem uiTreeItem = getUiTreeItem(aLong);
            if (uiTreeItem != null) {
                if (uiTreeItem.isChildLoaded()) {
                    uiTreeItem.setState(true);
                } else {
                    uiTreeItem.setState(true);
                    return;
                }
            }
        } else {
            if (isOpeningOperation) {           // Если это операция лейзи-открывания
                isOpeningOperation = false;     // то отключаем этот участок так как
                                                // когда открывание закончилось
                RefBookUiTreeItem selectedItem = getSelectedItem();
                if (selectedItem != null) {
                    ensureVisible(selectedItem);
                }
            }
        }
    }

    /**
     * Сделать выделенный итем видимым на компоненте
     * @param item итем
     */
    private void ensureVisible(final RefBookUiTreeItem item) {
        // так как отрисовка запаздывает и координаты сетятся чуть позже используем поторяющееся выполнения обновления
        Scheduler.get().scheduleFixedDelay(new Scheduler.RepeatingCommand() {
            @Override
            public boolean execute() {
                int scroll = scrollPanel.getVerticalScrollPosition();
                int maxScroll = scrollPanel.getMaximumVerticalScrollPosition();
                int offsetTop = item.getElement().getOffsetTop();
                int absTop = item.getElement().getAbsoluteTop();
                if (scroll == 0 && maxScroll == 0) {
                    return false;
                }

                scrollPanel.setVerticalScrollPosition(offsetTop);

                boolean isScrolled = (absTop != 0 || absTop == offsetTop) && (offsetTop == scroll || (offsetTop > maxScroll && scroll == maxScroll));
                return !isScrolled;
            }
        }, 500);
    }

    @Override
    public void load(PickerState pickerState, boolean force) {
        getUiHandlers().init(pickerState, force);
    }

    @Override
    public void reload() {
        getUiHandlers().reload();
    }

    @Override
    public void reload(List<Long> needToSelectIds)  {
        getUiHandlers().reload(needToSelectIds);
    }

    @Override
    public void find(String searchPattern, boolean exactSearch) {
        getUiHandlers().find(searchPattern, exactSearch);
    }

    @Override
    public void reloadOnDate(Date version) {
        clearSelected(true);
        getUiHandlers().reloadForDate(version);
    }

    @Override
    public void clearSelected(boolean fireChangeEvent) {
        isEnabledFireChangeEvent = fireChangeEvent;
        longList.clear();
        tree.clearSelection();
        onSelection();
    }

    @Override
    public Set<Long> getSelectedIds() {
        longList.clear();
        for (RefBookUiTreeItem item : getSelectedSet()) {
            longList.add(item.getRefBookTreeItem().getId());
        }
        return longList;
    }

    @Override
    public void setSingleColumn(String columnAlias) {
        //do nothing
    }

    private Set<RefBookUiTreeItem> getSelectedSet() {
        return tree.getSelectedItems();
    }

    private RefBookUiTreeItem getSelectedItem() {
        Iterator<RefBookUiTreeItem> it = tree.getSelectedItems().iterator();
        return it.hasNext() ? it.next() : null;
    }

    @Override
    public RefBookUiTreeItem getUiTreeItem(Long id) {
        for (RefBookUiTreeItem item : tree.getAllLoadedItems()) {
            if (item.getRefBookTreeItem().getId().equals(id)) {
                return item;
            }
        }
        return null;
    }

    public void selectFirstItenOnLoad(){
        getUiHandlers().selectFirstItenOnLoad();
    }

    /**
     * Удаление загруженной записи из дерева
     *
     * @param id идентификатор записи (не итема)
     */
    public void deleteRecord(Long id) {
        if (id != null) {
            for (RefBookUiTreeItem item : getSelectedSet()) {
                if (item.getRefBookTreeItem().getId().equals(id)) {
                    tree.removeItem(item);
                    return;
                }
            }
        }
    }

    /**
     * Обновление записи
     * поиск идет по реальным идентификаторам записи
     *
     * @param id          идентификатор записи (не итема)
     * @param newParentId новый ид родителя, может быть нулом тогда переместиться вверх дерева
     * @param name        имя, может быть нулл если не меняется
     */
    public void updateRecord(Long id, Long newParentId, String name) {
        if (id != null) {
            RefBookUiTreeItem uiTreeItem = getUiTreeItem(id);       // поиск итема который обновили или создали
            if (uiTreeItem != null) {                               // редактирование записи
                RefBookTreeItem parent = uiTreeItem.getRefBookTreeItem() != null ? uiTreeItem.getRefBookTreeItem().getParent() : null;

                if (WidgetUtils.isWasChange(name, uiTreeItem.getName())) {
                    // Если изменилось наименование
                    uiTreeItem.getRefBookTreeItem().setDereferenceValue(name);
                    uiTreeItem.setName(name);
                }

                if ((newParentId != null || parent != null) &&
                        (newParentId == null || parent == null || !newParentId.equals(parent.getId()))) {
                    // Если парент изменился
                    tree.removeItem(uiTreeItem);        // удаляем выделеный итем
                    if (newParentId == null) {
                        // если добавляется в корень
                        uiTreeItem.getRefBookTreeItem().setParent(null);
                        tree.addTreeItem(uiTreeItem);
                        ensureVisible(uiTreeItem);
                    } else {
                        // если добавляется в другой итем
                        RefBookUiTreeItem newParentUiTreeItem = getUiTreeItem(newParentId);     // поиск итема нового родителя в загруженых итемах
                        if (newParentUiTreeItem != null) {
                            // новый итем был уже загружен
                            uiTreeItem.getRefBookTreeItem().setParent(newParentUiTreeItem.getRefBookTreeItem());    // обновляем ссылку на родителя

                            if (newParentUiTreeItem.isChildLoaded()) {
                                // итем уже открывался и загружал своих чилдов
                                tree.addTreeItem(newParentUiTreeItem, uiTreeItem);
                                tree.openAllParent(uiTreeItem);     // открываем всех родителей итемак
                                ensureVisible(uiTreeItem);
                            } else {
                                // иначе открывает, редактируемый итем уже появится при загрузке
                                isOpeningOperation = true;
                                newParentUiTreeItem.setState(true);
                            }
                        } else {
                            // если не загружен то узнаем его путь до иерархии вверх
                            // получаем лист ид - путь от рута до родителя чилда
                            // 1, 2, 3
                            // и последовательно открываем
                            getUiHandlers().openFor(newParentId, false);
                        }
                    }
                }
            } else {
                // добавление новой записи
                RefBookTreeItem pseudoTreeItem = new RefBookTreeItem(id, name);     // создаем псевдо итем
                pseudoTreeItem.addRecordValues(name, "NAME", null);

                if (newParentId == null) {
                    // добавление в главный корень
                    reload(Arrays.asList(id));
                } else {
                    RefBookUiTreeItem newParentUiTreeItem = getUiTreeItem(newParentId);
                    if (newParentUiTreeItem != null) {
                        // если родитель уже загружен
                        tree.openAllParent(newParentUiTreeItem);

                        if (newParentUiTreeItem.isChildLoaded()) {
                            // Чилды были уже загружены, значит нужно перезагрузить, удаляем старые
                            tree.removeChildItems(newParentUiTreeItem);
                        }
                        trySelectValues(Arrays.asList(pseudoTreeItem));
                        newParentUiTreeItem.setState(true, true);       // и снова открываем, чилды загужаются
                    } else {
                        trySelectValues(Arrays.asList(pseudoTreeItem));
                        getUiHandlers().openFor(newParentId, false);
                    }
                }
            }
        }
    }

    @Override
    public String getDereferenceValue() {
        Set<RefBookUiTreeItem> selectedItems = getSelectedSet();
        StringBuilder sb = new StringBuilder();
        if (!selectedItems.isEmpty()) {
            for (RefBookUiTreeItem item : selectedItems) {
                sb.append(item.getRefBookTreeItem().getDereferenceValue());
                sb.append("; ");
            }
            sb.deleteCharAt(sb.length() - 2);
        }
        return sb.toString();
    }

    @Override
    public String getOtherDereferenceValue(Long attrId) {
        Set<RefBookUiTreeItem> selectedItems = getSelectedSet();
        if (selectedItems != null && !selectedItems.isEmpty()) {
            List<RefBookRecordDereferenceValue> dereferenceValues =
                    selectedItems.iterator().next().getRefBookTreeItem().getRefBookRecordDereferenceValues();
            return RefBookPickerUtils.getDereferenceValue(dereferenceValues, attrId);
        }
        return null;
    }

    @Override
    public String getOtherDereferenceValue(Long attrId, Long attrId2) {
        Set<RefBookUiTreeItem> selectedItems = getSelectedSet();
        if (selectedItems != null && !selectedItems.isEmpty()) {
            List<RefBookRecordDereferenceValue> dereferenceValues =
                    selectedItems.iterator().next().getRefBookTreeItem().getRefBookRecordDereferenceValues();
            return RefBookPickerUtils.getDereferenceValue(dereferenceValues, attrId, attrId2);
        }
        return null;
    }

    @Override
    public void setMultiSelect(Boolean multiSelect) {
        this.multiSelect = multiSelect;
        tree.setMultiSelect(this.multiSelect);
        widgetFireChangeEvent(getSelectedIds());
    }

    @Override
    public void selectAll(DeferredInvokeHandler handler) {
        if (multiSelect) {
            for (RefBookUiTreeItem uiTreeItem : tree.getAllLoadedItems()) {
                tree.setSelected(uiTreeItem, true);
            }
            widgetFireChangeEvent(getSelectedIds());
            if (handler != null) {
                handler.onInvoke();
            }
        }
    }

    @Override
    public void unselectAll(DeferredInvokeHandler handler) {
        if (multiSelect) {
            for (RefBookUiTreeItem uiTreeItem : tree.getAllLoadedItems()) {
                tree.setSelected(uiTreeItem, false);
            }
            widgetFireChangeEvent(getSelectedIds());
            if (handler != null) {
                handler.onInvoke();
            }
        }
    }

    @Override
    public void checkCount(String text, Date relevanceDate, boolean exactSearch, CheckValuesCountHandler checkValuesCountHandler) {
        getUiHandlers().getValuesCount(text, relevanceDate, exactSearch, checkValuesCountHandler);
    }

    @Override
    public void cleanValues() {
        tree.clear();
    }

    public void widgetFireChangeEvent(Set<Long> value) {
        ValueChangeEvent.fire(RefBookTreePickerView.this, value);
    }

    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Set<Long>> handler) {
        return asWidget().addHandler(handler, ValueChangeEvent.getType());
    }

    public HandlerRegistration addRootLoadedHandler(RootLoadedEvent.RootLoadedHandler handler) {
        return asWidget().addHandler(handler, RootLoadedEvent.getType());
    }

    public HandlerRegistration addChildrenLoadedHandler(ChildrenLoadedEvent.Handler handler) {
        return asWidget().addHandler(handler, ChildrenLoadedEvent.getType());
    }

    @Override
    public void fireEvent(GwtEvent<?> event) {
        asWidget().fireEvent(event);
    }

    @Override
    public boolean isVisible(){
        return scrollPanel.isVisible();
    }

    @Override
    public void setVisible(boolean visible){
        scrollPanel.setVisible(visible);
    }

    public void setEnabled(boolean isEnabled) {
        tree.setEnabled(isEnabled);
    }

    /**
     * Прерывает последний отправленный запрос
     */
    public void cancelRequest() {
        if (getUiHandlers() != null)
            getUiHandlers().cancelRequest();
    }
}