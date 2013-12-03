package com.aplana.sbrf.taxaccounting.web.module.testpage.client;

import com.aplana.gwt.client.*;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.text.shared.Renderer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.ValueListBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TestPageView extends ViewWithUiHandlers<TestPageUiHandlers> implements TestPagePresenter.MyView {

    interface Binder extends UiBinder<Widget, TestPageView> {
    }

    @UiField
    LabelSeparator sep;

    @UiField
    ImageButtonLink ibl;

    @UiField
    ImgBtnLnk ian;

    @UiField
    ListBox listBox;


    @UiField
    RefBookTextBox refbookTextBox;

    @UiField
    MultiListBox mlistbox;

    @Inject
    public TestPageView(final Binder uiBinder) {
        initWidget(uiBinder.createAndBindUi(this));
        ibl.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                Window.alert("Привет!");
            }
        });

        List<RefBookButtonData> list =  new ArrayList();
        list.add(new RefBookButtonData("http://127.0.0.1:8888/resources/img/question_mark.png",new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                Window.alert("Кнопка 1");
            }
        }));
        list.add(new RefBookButtonData("http://127.0.0.1:8888/resources/img/exclamation_mark.png",new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                Window.alert("Кнопка 2");
            }
        }));
        list.add(new RefBookButtonData("http://127.0.0.1:8888/resources/img/email.png",new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                Window.alert("Кнопка 3");
            }
        }));

        refbookTextBox.addButtons(list);

        listBox.addItem("Тест 1");
        listBox.addItem("Тест 2");

        mlistbox.setRenderer(new Renderer() {
            @Override
            public String render(Object object) {
                return object.toString();
            }

            @Override
            public void render(Object object, Appendable appendable) throws IOException {
                object.toString();
            }
        });


        List<String> itemList = new ArrayList<String>();

        itemList.add("Пункт 0");
        itemList.add("Пункт 1");
        itemList.add("Пункт 2");
        itemList.add("Пункт 3");
        itemList.add("Пункт 4");
        itemList.add("Пункт 5");
        itemList.add("Пункт 6");
        itemList.add("Пункт 7");

        List<Boolean> valueList = new ArrayList<Boolean>();

        valueList.add(true);
        valueList.add(false);
        valueList.add(true);
        valueList.add(false);
        valueList.add(true);
        valueList.add(false);
        valueList.add(true);
        valueList.add(false);

        mlistbox.setAvailableValues(itemList);
        mlistbox.setSelectedValues(valueList);





    }

}
