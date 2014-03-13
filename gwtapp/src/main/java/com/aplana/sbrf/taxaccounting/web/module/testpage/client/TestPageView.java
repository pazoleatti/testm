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
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.client.ui.TextBox;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class TestPageView extends ViewWithUiHandlers<TestPageUiHandlers> implements TestPagePresenter.MyView {

    public static final DateTimeFormat formatDMY = DateTimeFormat.getFormat("dd.MM.yyyy HH:mm:ss");

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
    DateMaskBoxPicker boxDatePicker;
    @UiField
    Label boxDateLb,
            boxMyLb,
            boxYLb,
            boxTextLb,
            boxDatePickerLb;
    @UiField
    CheckBox checkBoxNull;

    @UiField
    LinkButton
            linkButtonDisable,
            linkButton;
    @UiField
    LinkAnchor
            linkAnchor,
            linkAnchorDisable;

    @UiField
    RefBookPickerWidget hPicker;
    @UiField
    Label hPickerLabel,
            hPickerList;
    @UiField
    CheckBox hpMultiPickCb,
            hpDisabledCb,
            hpSearchCb,
            hpManualCb;
    @UiField
    TextBox hpValueTb;
    @UiField
    Button hpSetValueBtn;

    @UiField
    RefBookPickerWidget fPicker;

    @UiField
    Label fPickerLabel,
            fPickerList;
    @UiField
    CheckBox fpMultiPickCb,
            fpDisabledCb,
            fpSearchCb,
            fpManualCb;
    @UiField
    TextBox fpValueTb;
    @UiField
    Button fpSetValueBtn;


    @Inject
    public TestPageView(final Binder uiBinder) {

        multiListBox();

        initWidget(uiBinder.createAndBindUi(this));

        List<TestItem> getM = (List<TestItem>) mlistbox.getValue();
        String strCont = "";
        for (TestItem str : getM)
            strCont = strCont + str.getTitle() + "; ";

        showResult.setText(strCont);

        modalWind();

        lineinRefBook();

        heirarRefBook();

        testMaskBox();

        linkButtons();

    }

    private void multiListBox() {
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
    }

    private void modalWind() {
        showDialog.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                ModalWindow mw = new ModalWindow("тест", "http://127.0.0.1:8888/resources/img/question_mark.png");
                lbl1 = new Label("Тут будет содержаться любой объект. А пока закрой меня.");
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
    }

    private void lineinRefBook() {
        Date date  = new Date();
        fPicker.setPeriodDates(null, date);
        fPicker.addValueChangeHandler(new ValueChangeHandler<List<Long>>() {
            @Override
            public void onValueChange(ValueChangeEvent<List<Long>> event) {
                fPickerLabel.setText("Выбрано: " + fPicker.getDereferenceValue());
                fPickerList.setText("Список: " + event.getValue());
            }
        });
        fpDisabledCb.setValue(!fPicker.isEnabled());
        fpManualCb.setValue(fPicker.isManualUpdate());
        fpMultiPickCb.setValue(hPicker.getMultiSelect());
        fpSearchCb.setValue(!hPicker.getSearchEnabled());
        fpSetValueBtn.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                String s = fpValueTb.getText();
                List<Long> longs = new LinkedList<Long>();
                if (s != null && !s.trim().isEmpty()) {
                    s = s.trim();
                    if (s.contains(",")) {
                        for (String s1 : s.split(",")) {
                            if (s1 != null && !s1.trim().isEmpty()) {
                                longs.add(Long.valueOf(s1.trim()));
                            }
                        }
                    } else {
                        longs.add(Long.valueOf(s));
                    }
                } else {
                    longs = null;
                }
                fPicker.setValue(longs, true);
            }
        });
        fpSearchCb.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                fPicker.setSearchEnabled(!event.getValue());
            }
        });
        fpMultiPickCb.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                fPicker.setMultiSelect(event.getValue());
            }
        });
        fpManualCb.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                fPicker.setManualUpdate(event.getValue());
            }
        });
        fpDisabledCb.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                fPicker.setEnabled(!event.getValue());
            }
        });
    }

    private void heirarRefBook() {
        Date date  = new Date();
        hPicker.setPeriodDates(new Date(date.getTime() - 1000000L), new Date(date.getTime() + 1000000L));
        hPicker.addValueChangeHandler(new ValueChangeHandler<List<Long>>() {
            @Override
            public void onValueChange(ValueChangeEvent<List<Long>> event) {
                hPickerLabel.setText("Выбрано: " + hPicker.getDereferenceValue());
                hPickerList.setText("Список: " + event.getValue());
            }
        });
        hpDisabledCb.setValue(!hPicker.isEnabled());
        hpManualCb.setValue(hPicker.isManualUpdate());
        hpMultiPickCb.setValue(hPicker.getMultiSelect());
        hpSearchCb.setValue(!hPicker.getSearchEnabled());
        hpSetValueBtn.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                String s = hpValueTb.getText();
                List<Long> longs = new LinkedList<Long>();
                if (s != null && !s.trim().isEmpty()) {
                    s = s.trim();
                    if (s.contains(",")) {
                        for (String s1 : s.split(",")) {
                            if (s1 != null && !s1.trim().isEmpty()) {
                                longs.add(Long.valueOf(s1.trim()));
                            }
                        }
                    } else {
                        longs.add(Long.valueOf(s));
                    }
                } else {
                    longs = null;
                }
                hPicker.setValue(longs, true);
            }
        });
        hpSearchCb.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
               hPicker.setSearchEnabled(!event.getValue());
            }
        });
        hpMultiPickCb.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                hPicker.setMultiSelect(event.getValue());
            }
        });
        hpManualCb.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                hPicker.setManualUpdate(event.getValue());
            }
        });
        hpDisabledCb.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                hPicker.setEnabled(!event.getValue());
            }
        });
    }

    private void linkButtons() {
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
        boxDatePicker.setLimitDates(new Date(2014 - 1900, 3 - 1, 1), new Date(2014 - 1900, 3 - 1, 27));
        //boxDatePicker.setValue(new Date(2013 - 1900, 2, 1));
        checkBoxNull.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
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
            }
        });

        boxDatePicker.addValueChangeHandler(new ValueChangeHandler<Date>() {
            @Override
            public void onValueChange(ValueChangeEvent<Date> event) {
                boxDatePickerLb.setText(getTestMaskValues(event.getValue(), boxDatePicker.getValue()));
            }
        });
    }

    private String getTestMaskValues(String eventValue, String elemValue) {
        return "Событие: \"" + eventValue + "\", значение: \"" + elemValue + "\" .";
    }

    private String getTestMaskValues(Date eventValue, Date elemValue) {
        return "Событие: \"" + (eventValue != null ? formatDMY.format(eventValue) : null) + "\", значение: \"" + (elemValue != null ? formatDMY.format(elemValue) : null) + "\" .";
    }

}
