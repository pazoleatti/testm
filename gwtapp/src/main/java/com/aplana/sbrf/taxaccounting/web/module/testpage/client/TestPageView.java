package com.aplana.sbrf.taxaccounting.web.module.testpage.client;

import com.aplana.gwt.client.*;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.text.shared.Renderer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
//import com.sun.java.swing.plaf.windows.resources.windows;

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
    RefBookTextBox refbookTextBox;

    @UiField(provided = true)
    MultiListBox mlistbox;

    @UiField
    Label showResult;


    @Inject
    public TestPageView(final Binder uiBinder) {
        List<String> itemList = new ArrayList<String>();

        String s1 = "Пункт 1";
        String s3 = "Пункт 3";
        itemList.add("Пункт 0");
        itemList.add(s1);
        itemList.add("Пункт 2");
        itemList.add(s3);
        itemList.add("Пункт 4");
        itemList.add("Пункт 5");
        itemList.add("Пункт 6");
        itemList.add("Пункт 7");

        List<String> valueList = new ArrayList<String>();

        valueList.add(s1);
        valueList.add(s3);

        mlistbox = new MultiListBox(new Renderer() {
            @Override
            public String render(Object object) {
                return object.toString();
            }

            @Override
            public void render(Object object, Appendable appendable) throws IOException {
                object.toString();
            }}, false, true);

         mlistbox.setAvailableValues(itemList);
         mlistbox.setValue(valueList);
         mlistbox.addValueChangeHandler( new ValueChangeHandler<List>() {
             @Override
             public void onValueChange(ValueChangeEvent<List> event) {
                 Window.alert("Значение поменялось!");
             }
         });


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

      /*  mlistbox.setRenderer(new Renderer() {
            @Override
            public String render(Object object) {
                return object.toString();
            }

            @Override
            public void render(Object object, Appendable appendable) throws IOException {
                object.toString();
            }
        });
*/

        List<String> getM = (List<String>)mlistbox.getValue();
        String strCont = "";
        for (String str : getM)
            strCont = strCont + " | " + str;

            showResult.setText(strCont);

    }

}
