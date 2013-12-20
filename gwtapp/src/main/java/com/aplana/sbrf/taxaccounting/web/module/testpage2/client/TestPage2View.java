package com.aplana.sbrf.taxaccounting.web.module.testpage2.client;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.DepartmentPair;
import com.aplana.sbrf.taxaccounting.web.widget.departmentpicker.DepartmentTreeWidget;
import com.aplana.sbrf.taxaccounting.web.widget.multiselecttree.MultiSelectTree;
import com.aplana.sbrf.taxaccounting.web.widget.multiselecttree.MultiSelectTreeItem;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

import java.util.ArrayList;
import java.util.List;

public class TestPage2View extends ViewWithUiHandlers<TestPage2UiHandlers> implements TestPage2Presenter.MyView {

    interface Binder extends UiBinder<Widget, TestPage2View> {
    }

    @UiField(provided = true)
    DepartmentTreeWidget tree;

    @UiField(provided = true)
    MultiSelectTree mstree;

    @UiField(provided = true)
    com.aplana.sbrf.taxaccounting.web.widget.multiselecttreeold.MultiSelectTree mstreeOld;

    @UiField
    Button ok;

    @UiField
    CheckBox selectChild;

    @Inject
    public TestPage2View(final Binder uiBinder) {
        initTree();
        initMultiSelectTree();
        initMultiSelectTreeOld();

        initWidget(uiBinder.createAndBindUi(this));
    }

    void initTree() {
        tree = new DepartmentTreeWidget("test182", true);

        List<Department> departments = new ArrayList<Department>();
        Department d = new Department();
        d.setId(1);
        d.setName("111");
        d.setParentId(null);
        departments.add(d);

        d = new Department();
        d.setId(2);
        d.setName("222");
        d.setParentId(1);
        departments.add(d);

        d = new Department();
        d.setId(3);
        d.setName("333");
        d.setParentId(1);
        departments.add(d);

        d = new Department();
        d.setId(33);
        d.setName("333-333");
        d.setParentId(3);
        departments.add(d);

        tree.setAvailableValues(departments);

        List<DepartmentPair> values = new ArrayList<DepartmentPair>();
        values.add(new DepartmentPair(1, null, "111"));
        values.add(new DepartmentPair(2, 1, "222"));
        values.add(new DepartmentPair(3, 1, "333"));
        values.add(new DepartmentPair(33, 3, "333-333"));
        tree.setValue(values);
    }

    void initMultiSelectTree() {
        mstree = new MultiSelectTree("Наименование подразделения");

        List<MultiSelectTreeItem> items = new ArrayList<MultiSelectTreeItem>();
        MultiSelectTreeItem item1 = new MultiSelectTreeItem(1, "Открытое акционерное общестро «Сбербанк России»");
        MultiSelectTreeItem item2 = new MultiSelectTreeItem(2, "222222222222222222222222222222222222222222222222222");
        MultiSelectTreeItem item3 = new MultiSelectTreeItem(3, "333");
        MultiSelectTreeItem item33 = new MultiSelectTreeItem(33, "333-333");
        MultiSelectTreeItem item4 = new MultiSelectTreeItem(4, "444");
        MultiSelectTreeItem item5 = new MultiSelectTreeItem(5, "Байкальский банк 555");
        MultiSelectTreeItem item55 = new MultiSelectTreeItem(55, "Бурятское ОСБ №98601 Байкальского банка");
        MultiSelectTreeItem item555 = new MultiSelectTreeItem(555, "555-555-555");
        MultiSelectTreeItem item6 = new MultiSelectTreeItem(6, "666");
        MultiSelectTreeItem item7 = new MultiSelectTreeItem(7, "777");
        MultiSelectTreeItem item8 = new MultiSelectTreeItem(8, "888");
        MultiSelectTreeItem item88 = new MultiSelectTreeItem(88, "888-888");
        MultiSelectTreeItem item9 = new MultiSelectTreeItem(9, "999");
        MultiSelectTreeItem item99 = new MultiSelectTreeItem(99, "999-999");
        MultiSelectTreeItem item10 = new MultiSelectTreeItem(10, "Северо-Западный банк 1110");
        MultiSelectTreeItem item11 = new MultiSelectTreeItem(11, "Северо-Западный банк 1111");
        MultiSelectTreeItem item12 = new MultiSelectTreeItem(12, "Северо-Западный банк 1112");
        MultiSelectTreeItem item13 = new MultiSelectTreeItem(13, "Северо-Западный банк 1113");
        MultiSelectTreeItem item14 = new MultiSelectTreeItem(14, "Северо-Западный банк 1114");
        MultiSelectTreeItem item15 = new MultiSelectTreeItem(15, "Северо-Западный банк 1115");
        MultiSelectTreeItem item16 = new MultiSelectTreeItem(16, "Северо-Западный банк 1116");
        MultiSelectTreeItem item17 = new MultiSelectTreeItem(17, "Северо-Западный банк 1117");

        item1.addItem(item2);
        item1.addItem(item3);
        item1.addItem(item4);
        item1.addItem(item5);
        item3.addItem(item33);
        item5.addItem(item55);
        item55.addItem(item555);
        item1.addItem(item6);
        item1.addItem(item7);
        item1.addItem(item8);
        item8.addItem(item88);
        item1.addItem(item9);
        item9.addItem(item99);
        item1.addItem(item10);
        item1.addItem(item11);
        item1.addItem(item12);
        item1.addItem(item13);
        item1.addItem(item14);
        item1.addItem(item15);
        item1.addItem(item16);
        item1.addItem(item17);

        items.add(item1);
        mstree.setTreeItems(items);

        List<Integer> values = new ArrayList<Integer>();
        values.add(555);
        values.add(7);
        mstree.setValue(values);
    }

    void initMultiSelectTreeOld() {
        mstreeOld = new com.aplana.sbrf.taxaccounting.web.widget.multiselecttreeold.MultiSelectTree("Наименование подразделения");

        List<com.aplana.sbrf.taxaccounting.web.widget.multiselecttreeold.MultiSelectTreeItem> items
                = new ArrayList<com.aplana.sbrf.taxaccounting.web.widget.multiselecttreeold.MultiSelectTreeItem>();
        com.aplana.sbrf.taxaccounting.web.widget.multiselecttreeold.MultiSelectTreeItem item1 = new com.aplana.sbrf.taxaccounting.web.widget.multiselecttreeold.MultiSelectTreeItem(1, "111");
        com.aplana.sbrf.taxaccounting.web.widget.multiselecttreeold.MultiSelectTreeItem item2 = new com.aplana.sbrf.taxaccounting.web.widget.multiselecttreeold.MultiSelectTreeItem(2, "222222222222222222222222222222222222222222222222222");
        com.aplana.sbrf.taxaccounting.web.widget.multiselecttreeold.MultiSelectTreeItem item3 = new com.aplana.sbrf.taxaccounting.web.widget.multiselecttreeold.MultiSelectTreeItem(3, "333");
        com.aplana.sbrf.taxaccounting.web.widget.multiselecttreeold.MultiSelectTreeItem item33 = new com.aplana.sbrf.taxaccounting.web.widget.multiselecttreeold.MultiSelectTreeItem(33, "333-333");
        com.aplana.sbrf.taxaccounting.web.widget.multiselecttreeold.MultiSelectTreeItem item4 = new com.aplana.sbrf.taxaccounting.web.widget.multiselecttreeold.MultiSelectTreeItem(4, "444");
        com.aplana.sbrf.taxaccounting.web.widget.multiselecttreeold.MultiSelectTreeItem item5 = new com.aplana.sbrf.taxaccounting.web.widget.multiselecttreeold.MultiSelectTreeItem(5, "555");
        com.aplana.sbrf.taxaccounting.web.widget.multiselecttreeold.MultiSelectTreeItem item55 = new com.aplana.sbrf.taxaccounting.web.widget.multiselecttreeold.MultiSelectTreeItem(55, "555-555");
        com.aplana.sbrf.taxaccounting.web.widget.multiselecttreeold.MultiSelectTreeItem item555 = new com.aplana.sbrf.taxaccounting.web.widget.multiselecttreeold.MultiSelectTreeItem(555, "555-555-555");
        com.aplana.sbrf.taxaccounting.web.widget.multiselecttreeold.MultiSelectTreeItem item6 = new com.aplana.sbrf.taxaccounting.web.widget.multiselecttreeold.MultiSelectTreeItem(6, "666");
        com.aplana.sbrf.taxaccounting.web.widget.multiselecttreeold.MultiSelectTreeItem item7 = new com.aplana.sbrf.taxaccounting.web.widget.multiselecttreeold.MultiSelectTreeItem(7, "777");
        com.aplana.sbrf.taxaccounting.web.widget.multiselecttreeold.MultiSelectTreeItem item8 = new com.aplana.sbrf.taxaccounting.web.widget.multiselecttreeold.MultiSelectTreeItem(8, "888");
        com.aplana.sbrf.taxaccounting.web.widget.multiselecttreeold.MultiSelectTreeItem item88 = new com.aplana.sbrf.taxaccounting.web.widget.multiselecttreeold.MultiSelectTreeItem(88, "888-888");
        com.aplana.sbrf.taxaccounting.web.widget.multiselecttreeold.MultiSelectTreeItem item9 = new com.aplana.sbrf.taxaccounting.web.widget.multiselecttreeold.MultiSelectTreeItem(9, "999");
        com.aplana.sbrf.taxaccounting.web.widget.multiselecttreeold.MultiSelectTreeItem item99 = new com.aplana.sbrf.taxaccounting.web.widget.multiselecttreeold.MultiSelectTreeItem(99, "999-999");
        com.aplana.sbrf.taxaccounting.web.widget.multiselecttreeold.MultiSelectTreeItem item10 = new com.aplana.sbrf.taxaccounting.web.widget.multiselecttreeold.MultiSelectTreeItem(10, "1110");
        com.aplana.sbrf.taxaccounting.web.widget.multiselecttreeold.MultiSelectTreeItem item11 = new com.aplana.sbrf.taxaccounting.web.widget.multiselecttreeold.MultiSelectTreeItem(11, "1111");
        com.aplana.sbrf.taxaccounting.web.widget.multiselecttreeold.MultiSelectTreeItem item12 = new com.aplana.sbrf.taxaccounting.web.widget.multiselecttreeold.MultiSelectTreeItem(12, "1112");
        com.aplana.sbrf.taxaccounting.web.widget.multiselecttreeold.MultiSelectTreeItem item13 = new com.aplana.sbrf.taxaccounting.web.widget.multiselecttreeold.MultiSelectTreeItem(13, "1113");
        com.aplana.sbrf.taxaccounting.web.widget.multiselecttreeold.MultiSelectTreeItem item14 = new com.aplana.sbrf.taxaccounting.web.widget.multiselecttreeold.MultiSelectTreeItem(14, "1114");
        com.aplana.sbrf.taxaccounting.web.widget.multiselecttreeold.MultiSelectTreeItem item15 = new com.aplana.sbrf.taxaccounting.web.widget.multiselecttreeold.MultiSelectTreeItem(15, "1115");
        com.aplana.sbrf.taxaccounting.web.widget.multiselecttreeold.MultiSelectTreeItem item16 = new com.aplana.sbrf.taxaccounting.web.widget.multiselecttreeold.MultiSelectTreeItem(16, "1116");
        com.aplana.sbrf.taxaccounting.web.widget.multiselecttreeold.MultiSelectTreeItem item17 = new com.aplana.sbrf.taxaccounting.web.widget.multiselecttreeold.MultiSelectTreeItem(17, "1117");

        item1.addItem(item2);
        item1.addItem(item3);
        item1.addItem(item4);
        item1.addItem(item5);
        item3.addItem(item33);
        item5.addItem(item55);
        item55.addItem(item555);
        item1.addItem(item6);
        item1.addItem(item7);
        item1.addItem(item8);
        item8.addItem(item88);
        item1.addItem(item9);
        item9.addItem(item99);
        item1.addItem(item10);
        item1.addItem(item11);
        item1.addItem(item12);
        item1.addItem(item13);
        item1.addItem(item14);
        item1.addItem(item15);
        item1.addItem(item16);
        item1.addItem(item17);

        items.add(item1);
        mstreeOld.setTreeValues(items);

        List<Integer> values = new ArrayList<Integer>();
        values.add(555);
        values.add(7);
        mstreeOld.setValue(values);
    }

    @UiHandler("ok")
    void okButtonClicked(ClickEvent event) {
        // mstree.setText("".equals(mstree.getText()) ? "Наименование подразделения" : "");
        System.out.println("test! " + mstree.getValue());
        String w, h;
        if (mstree.getOffsetWidth() > 500) {
            w = "150px";
            h = "250px";
        } else {
            w = (mstree.getOffsetWidth() + 50) + "px";
            h = (mstree.getOffsetHeight() + 50) + "px";
        }
        mstree.setWidth(w);
        mstree.setHeight(h);
    }

    @UiHandler("selectChild")
    void onValueChangeSelectChild(ValueChangeEvent<Boolean> event) {
        mstree.setSelectChild(event.getValue());
    }
}
