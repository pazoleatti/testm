package com.aplana.sbrf.taxaccounting.web.widget.style;

import com.aplana.sbrf.taxaccounting.web.module.sources.client.SourcesView;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.StyleElement;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.*;
import com.google.inject.Inject;

/**
 * Label-разделитель
 * @author vpetrov
 */
public class LabelSeparator extends Composite implements HasText {

    private static Binder uiBinder = GWT.create(Binder.class);

    interface Binder extends UiBinder<Widget, LabelSeparator> {
    }

    interface Style extends CssResource {
        String panel();
        String sp();
        String sptext();
    }

    @UiField
    HorizontalPanel panel;
    @UiField
    Style style;

    Label textBox = new Label("Разделитель", false);
    HTMLPanel htmlPanel = new HTMLPanel("");

    private boolean textRight = false;

    public LabelSeparator() {
    }

    @Inject
    @UiConstructor
    public LabelSeparator(String text) {
        initWidget(uiBinder.createAndBindUi(this));
        textBox.addStyleName(style.sptext());
        htmlPanel.addStyleName(style.sp());
        panel.addStyleName(style.panel());
        textBox.setText(text);

        setTextRight(false);
    }

    public void setTextRight(boolean textRight){
        panel.clear();
        if (textRight) {
            panel.add(htmlPanel);
            panel.add(textBox);
        } else {
            panel.add(textBox);
            panel.add(htmlPanel);
        }
        panel.setCellWidth(textBox, "1%");
        panel.setCellWidth(htmlPanel, "99%");
        panel.setCellVerticalAlignment(htmlPanel, HasVerticalAlignment.ALIGN_BOTTOM);
    }

    public void setText(String text){
        textBox.setText(text);
    }

    public String getText(){
        return textBox.getText();
    }

    public void setWidth(String width){
        panel.setWidth(width);
    }

    public int getOffsetWidth(){
      return super.getOffsetWidth();
    }

    public void setVisible(boolean visible){
        panel.setVisible(visible);
    }

    public boolean isVisible(){
        return panel.isVisible();
    }
}
