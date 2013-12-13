package com.aplana.sbrf.taxaccounting.web.module.testpage.client;

import com.aplana.gwt.client.*;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Label;
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
    RefBookTextBox refbookTextBox;

    @UiField(provided = true)
    MultiListBox mlistbox;

    @UiField
    Label showResult;

    @UiField(provided = true)
    TypicalFormHeader formHeader;
    @UiField
    Label lbl;


    @UiField
    CheckBox chk;


    @Inject
    public TestPageView(final Binder uiBinder) {
        List<TestItem> itemList = new ArrayList<TestItem>();

        itemList.add(new TestItem("aad", 1));
        itemList.add(new TestItem("фыв", 2));
        itemList.add(new TestItem("aaaaaaaaaaaaaaaaaaaaaasssssssssssssssssssssssssssdddddddddddddddddddddddddddddddddddddddfffffffffffffffffffffffffffffffffffffeeeeeeeeeeeeeeeeaxxaz", 3));

        List<TestItem> valueList = new ArrayList<TestItem>();
        valueList.add(new TestItem("фыв", 2));

        mlistbox = new MultiListBox(new AbstractRenderer<TestItem>() {
            @Override
            public String render(TestItem item) {
                return item.getTitle();
            }
        }, true, true);

        mlistbox.setAvailableValues(itemList);
        mlistbox.setValue(valueList);
        mlistbox.addValueChangeHandler(new ValueChangeHandler<List>() {
            @Override
            public void onValueChange(ValueChangeEvent<List> event) {
                List<TestItem> getM = (List<TestItem>) mlistbox.getValue();
                String strCont = "";
                for (TestItem str : getM)
                    strCont = strCont + str.getTitle() + "; ";
                showResult.setText(strCont);
            }
        });

         mlistbox.setAvailableValues(itemList);
         mlistbox.setValue(valueList);
         mlistbox.addValueChangeHandler( new ValueChangeHandler<List>() {
             @Override
             public void onValueChange(ValueChangeEvent<List> event) {
                 List<String> getM = (List<String>)mlistbox.getValue();
                 String strCont = "";
                 for (String str : getM)
                     strCont = strCont + str + "; ";
                 showResult.setText(strCont);
             }
         });

        formHeader = new TypicalFormHeader();
        formHeader.addLeftWidget(new Label("Список налоговых форм пример"));
        formHeader.addLeftWidget(new Label("-"));

        Label label = new Label("Например кнопка");
        label.getElement().getStyle().setProperty("fontSize", 20, Style.Unit.PX);
        formHeader.addRightWidget(label);
        formHeader.addRightWidget(new Label("Режим редактирования"));
        formHeader.addMiddleWidget(new Label("Сводная форма начисленных доходов (доходы сложные) Очень длинный заголовок бла бла бал ба лаб ла бал аб лалалалалалал ла ла"));

        initWidget(uiBinder.createAndBindUi(this));

        ibl.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                Window.alert("Привет!");
            }
        });

        List<RefBookButtonData> list = new ArrayList();
        list.add(new RefBookButtonData("http://127.0.0.1:8888/resources/img/question_mark.png", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                Window.alert("Кнопка 1");
            }
        }));
        list.add(new RefBookButtonData("http://127.0.0.1:8888/resources/img/exclamation_mark.png", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                Window.alert("Кнопка 2");
            }
        }));
        list.add(new RefBookButtonData("http://127.0.0.1:8888/resources/img/email.png", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                Window.alert("Кнопка 3");
            }
        }));

        refbookTextBox.addButtons(list);

        List<TestItem> getM = (List<TestItem>) mlistbox.getValue();
        String strCont = "";
        for (TestItem str : getM)
            strCont = strCont + str.getTitle() + "; ";

        showResult.setText(strCont);


        //put this handler in the constructor
        lbl.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                chk.setValue(!chk.getValue(), true);
                //This will so it will manually operate the checkbox
            }
        });

    }

}
