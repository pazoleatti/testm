package com.aplana.sbrf.taxaccounting.web.module.testpage.client;

import com.aplana.gwt.client.*;
import com.aplana.gwt.client.mask.ui.TextMaskBox;
import com.aplana.gwt.client.mask.ui.DateMaskBox;
import com.aplana.gwt.client.mask.ui.MonthYearMaskBox;
import com.aplana.gwt.client.mask.ui.YearMaskBox;
import com.aplana.sbrf.taxaccounting.web.widget.datepicker.DateMaskBoxPicker;
import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.client.RefBookPickerWidget;
import com.aplana.sbrf.taxaccounting.web.widget.style.LabelSeparator;
import com.aplana.sbrf.taxaccounting.web.widget.style.LinkAnchor;
import com.aplana.sbrf.taxaccounting.web.widget.style.LinkButton;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class TestPageView extends ViewWithUiHandlers<TestPageUiHandlers> implements TestPagePresenter.MyView {

    interface Binder extends UiBinder<Widget, TestPageView> {
    }

    @UiField
    LabelSeparator sep;

    @UiField(provided = true)
    MultiListBox mlistbox;

    @UiField
    Label showResult;


    @UiField
    Button showDialog;

    Label lbl1;

    @UiField
    DateMaskBox boxDate;
    @UiField
    MonthYearMaskBox boxMy;
    @UiField
    YearMaskBox boxY;
    @UiField
    TextMaskBox boxText;

    @UiField
    Label boxDateLb;
    @UiField
    Label boxMyLb;
    @UiField
    Label boxYLb;
    @UiField
    Label boxTextLb;
    @UiField
    DateMaskBoxPicker boxDatePicker;
    @UiField
    Label boxDatePickerLb;
    @UiField
    CheckBox checkBoxNull;

    @UiField
    LinkButton linkButtonDisable;
    @UiField
    LinkButton linkButton;

    @UiField
    LinkAnchor linkAnchor;

    @UiField
    LinkAnchor linkAnchorDisable;

    @UiField
    RefBookPickerWidget fpicker;

    @UiField
    RefBookPickerWidget flatPicker;
    @UiField
    Label fPickerLabel;
    @UiField
    Label flatPickerLabel;
    @UiField
    Label fpickerList;
    @UiField
    Label flatPickerList;

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
        mlistbox.addValueChangeHandler(new ValueChangeHandler<List>() {
            @Override
            public void onValueChange(ValueChangeEvent<List> event) {
                List<String> getM = (List<String>) mlistbox.getValue();
                String strCont = "";
                for (String str : getM)
                    strCont = strCont + str + "; ";
                showResult.setText(strCont);
            }
        });

        initWidget(uiBinder.createAndBindUi(this));

        List<TestItem> getM = (List<TestItem>) mlistbox.getValue();
        String strCont = "";
        for (TestItem str : getM)
            strCont = strCont + str.getTitle() + "; ";

        showResult.setText(strCont);

        showDialog.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                ModalWindow mw = new ModalWindow("тест", "http://127.0.0.1:8888/resources/img/question_mark.png");
                lbl1 = new Label("Тест");
                //lbl1.setSize("200px","200px");
                mw.add(lbl1);
                // mw.addAdditionalButton(new ImageButtonLink("http://127.0.0.1:8888/resources/img/email.png", "Отправить письмо"));
            /*    mw.addSaveButtonClickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        lbl1.setText("Нажали кнопку сохранить.");
                    }
                });*/
                mw.setWidth("300px");
                mw.center();
                mw.show();
            }

        });

        checkBoxNull.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                //flatPicker.setEnabled(false);
                //flatPicker.setValue(null, true);

                ArrayList<Long> longs = new ArrayList<Long>();
                if (event.getValue()) {
                    longs.add(332L);
                } else {
                    longs.add(332L);
                    longs.add(331L);
                }
                flatPicker.setValue(longs, true);
                flatPicker.setEnabled(false);

                boxDate.setMayBeNull(event.getValue());
                boxMy.setMayBeNull(event.getValue());
                boxY.setMayBeNull(event.getValue());
                boxText.setMayBeNull(event.getValue());
                boxDatePicker.setCanBeEmpty(event.getValue());
                if (event.getValue()) {
                    boxDate.setValue(null, true);
                    boxMy.setValue(null, true);
                    boxY.setValue(null, true);
                    boxText.setValue(null, true);
                    boxDatePicker.setValue(null, true);
                }
            }
        });

        fpicker.setPeriodDates(null, new Date());

        fpicker.addValueChangeHandler(new ValueChangeHandler<List<Long>>() {
            @Override
            public void onValueChange(ValueChangeEvent<List<Long>> event) {
                fPickerLabel.setText("Выбрано: " + fpicker.getDereferenceValue());
                fpickerList.setText("Список: " + event.getValue());
            }
        });

        flatPicker.setSearchEnabled(false);
        //flatPicker.setManualUpdate(true);
        flatPicker.setPeriodDates(null, new Date());
        flatPicker.addValueChangeHandler(new ValueChangeHandler<List<Long>>() {
            @Override
            public void onValueChange(ValueChangeEvent<List<Long>> event) {
                flatPickerLabel.setText("Выбрано: " + flatPicker.getDereferenceValue());
                flatPickerList.setText("Список: " + event.getValue());
            }
        });
        ArrayList<Long> longs = new ArrayList<Long>();
        longs.add(332L);
        longs.add(331L);
        flatPicker.setValue(longs, true);
        flatPicker.setEnabled(false);

        testMaskBox();

        linkButtonDisable.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                Window.alert("!!!");
            }
        });

        linkButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                Window.alert("!!!!");
            }
        });

        linkAnchor.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                Window.alert("@@@@");
            }
        });

        linkAnchorDisable.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                Window.alert("@@@@");
            }
        });
    }


    private void testMaskBox() {
        Date date = new Date();
        boxDate.setValue(date);
        boxMy.setValue(date);
        boxY.setValue(date);


        boxDate.addValueChangeHandler(new ValueChangeHandler<Date>() {
            @Override
            public void onValueChange(ValueChangeEvent<Date> event) {
                boxDateLb.setText(getTestMaskValues(event.getValue(), boxDate.getValue()));
            }
        });

        boxMy.addValueChangeHandler(new ValueChangeHandler<Date>() {
            @Override
            public void onValueChange(ValueChangeEvent<Date> event) {
                boxMyLb.setText(getTestMaskValues(event.getValue(), boxMy.getValue()));
            }
        });

        boxY.addValueChangeHandler(new ValueChangeHandler<Date>() {
            @Override
            public void onValueChange(ValueChangeEvent<Date> event) {
                boxYLb.setText(getTestMaskValues(event.getValue(), boxY.getValue()));
            }
        });

        boxText.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                boxTextLb.setText(getTestMaskValues(event.getValue(), boxText.getValue()));
                System.out.println(boxY.isEnabled());
                boxY.setEnabled(!boxY.isEnabled());
            }
        });

        boxDatePicker.addValueChangeHandler(new ValueChangeHandler<Date>() {
            @Override
            public void onValueChange(ValueChangeEvent<Date> event) {
                boxDatePickerLb.setText(getTestMaskValues(event.getValue(), boxDatePicker.getValue()));
            }
        });
    }

    private String getTestMaskValues(Object eventValue, Object elemValue) {
        return "Событие: " + String.valueOf(eventValue) + " " + String.valueOf(elemValue) + " .";
    }

}
