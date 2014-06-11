package com.aplana.sbrf.taxaccounting.web.widget.style;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.IsEditor;
import com.google.gwt.editor.client.LeafValueEditor;
import com.google.gwt.editor.client.adapters.TakesValueEditor;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HasValue;

/**
 * Кнопочка влево-вправо
 *
 * @author aivanov
 * @since 19.05.2014
 */
public class LeftRightToggleButton extends Button implements HasValue<Boolean>, IsEditor<LeafValueEditor<Boolean>> {

    public static interface IconResource extends ClientBundle {
        @Source("arrow_left_white.png")
        ImageResource left();

        @Source("arrow_right_white.png")
        ImageResource right();
    }
    public static IconResource iconResource = GWT.create(IconResource.class);

    /* false - лево, true - право */
    private Boolean state = false;
    private String leftTitle;
    private String rightTitle;
    private LeafValueEditor<Boolean> editor;

    private String leftImageUrl = "<img border='0' src='"+iconResource.left().getSafeUri().asString() +"' />";
    private String rightImageUrl = "<img border='0' src='"+iconResource.right().getSafeUri().asString() +"' />";

    public LeftRightToggleButton() {
        this(false);
    }

    public LeftRightToggleButton(final Boolean state) {
        super();
        setImage();
        addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                setValue(!LeftRightToggleButton.this.state, true);
            }
        });
    }

    private void setImage(){
        setHTML(state != null && state ? rightImageUrl: leftImageUrl);
        setTitle(state != null && state ? rightTitle: leftTitle);
    }

    public String getLeftTitle() {
        return leftTitle;
    }

    public void setLeftTitle(String leftTitle) {
        this.leftTitle = leftTitle;
        setTitle(state != null && state ? rightTitle: leftTitle);
    }

    public String getRightTitle() {
        return rightTitle;
    }

    public void setRightTitle(String rightTitle) {
        this.rightTitle = rightTitle;
        setTitle(state != null && state ? rightTitle: leftTitle);
    }

    public void setTitles(String leftTitle, String rightTitle){
        this.leftTitle = leftTitle;
        this.rightTitle = rightTitle;
        setTitle(state != null && state ? rightTitle: leftTitle);
    }

    @Override
    public Boolean getValue() {
        return state;
    }

    @Override
    public void setValue(Boolean value) {
        setValue(value, false);
    }

    @Override
    public void setValue(Boolean value, boolean fireEvents) {
        state = value;
        setImage();
        if (fireEvents) {
            ValueChangeEvent.fire(this, state);
        }
    }

    public HandlerRegistration addValueChangeHandler(
            ValueChangeHandler<Boolean> handler) {
        return addHandler(handler, ValueChangeEvent.getType());
    }

    public LeafValueEditor<Boolean> asEditor() {
        if (editor == null) {
            editor = TakesValueEditor.of(this);
        }
        return editor;
    }
}
