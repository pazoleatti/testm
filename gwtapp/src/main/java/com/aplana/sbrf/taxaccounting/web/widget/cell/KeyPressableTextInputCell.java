/*
 * Copyright 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.aplana.sbrf.taxaccounting.web.widget.cell;

import com.google.gwt.cell.client.AbstractEditableCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.text.shared.SafeHtmlRenderer;
import com.google.gwt.text.shared.SimpleSafeHtmlRenderer;

import static com.google.gwt.dom.client.BrowserEvents.*;

/**
 * An editable text cell. Click to edit, escape to cancel, return to commit.
 */
public class KeyPressableTextInputCell extends
        AbstractEditableCell<String, KeyPressableTextInputCell.ViewData> {

    private boolean textRight = false;

    interface Template extends SafeHtmlTemplates {
        @Template("<input type=\"text\" value=\"{0}\" tabindex=\"-1\"></input>")
        SafeHtml input(String value);
    }

    interface TemplateTextRight extends Template {
        @Override
        @Template("<input type=\"text\" value=\"{0}\" style=\"text-align:right; \" tabindex=\"-1\"></input>")
        SafeHtml input(String value);
    }

	/**
	 * The view data object used by this cell. We need to store both the text and
	 * the state because this cell is rendered differently in edit mode. If we did
	 * not store the edit state, refreshing the cell with view data would always
	 * put us in to edit state, rendering a text box instead of the new text
	 * string.
	 */
	static class ViewData {

		private boolean isEditing;

		/**
		 * If true, this is not the first edit.
		 */
		private boolean isEditingAgain;

		/**
		 * Keep track of the original value at the start of the edit, which might be
		 * the edited value from the previous edit and NOT the actual value.
		 */
		private String original;

		private String text;

		/**
		 * Construct a new ViewData in editing mode.
		 *
		 * @param text the text to edit
		 */
		public ViewData(String text) {
			this.original = text;
			this.text = text;
			this.isEditing = true;
			this.isEditingAgain = false;
		}

		@Override
		public boolean equals(Object o) {
			if (o == null) {
				return false;
			}
			ViewData vd = (ViewData) o;
			return equalsOrBothNull(original, vd.original)
					&& equalsOrBothNull(text, vd.text) && isEditing == vd.isEditing
					&& isEditingAgain == vd.isEditingAgain;
		}

		public String getOriginal() {
			return original;
		}

		public String getText() {
			return text;
		}

		@Override
		public int hashCode() {
			return original.hashCode() + text.hashCode()
					+ Boolean.valueOf(isEditing).hashCode() * 29
					+ Boolean.valueOf(isEditingAgain).hashCode();
		}

		public boolean isEditing() {
			return isEditing;
		}

		public boolean isEditingAgain() {
			return isEditingAgain;
		}

		public void setEditing(boolean isEditing) {
			boolean wasEditing = this.isEditing;
			this.isEditing = isEditing;

			// This is a subsequent edit, so start from where we left off.
			if (!wasEditing && isEditing) {
				isEditingAgain = true;
				original = text;
			}
		}

		public void setText(String text) {
			this.text = text;
		}

		private boolean equalsOrBothNull(Object o1, Object o2) {
			return (o1 == null) ? o2 == null : o1.equals(o2);
		}
	}

	private static Template template;
	private static Template templateRight;

	private final SafeHtmlRenderer<String> renderer;

	/**
	 * Construct a new EditTextCell that will use a
	 * {@link SimpleSafeHtmlRenderer}.
	 */
	public KeyPressableTextInputCell() {
		this(SimpleSafeHtmlRenderer.getInstance(), false);
	}

	/**
	 * Construct a new EditTextCell that will use a given {@link SafeHtmlRenderer}
	 * to render the value when not in edit mode.
	 *
	 * @param renderer a {@link SafeHtmlRenderer SafeHtmlRenderer<String>}
	 *          instance
	 */
	public KeyPressableTextInputCell(SafeHtmlRenderer<String> renderer, boolean textRight) {
        // переделал на MOUSEUP потому что при клике там не правильный порядок идет
        // проброса событий, так как строка перерисовывется еще
		super(CLICK, KEYDOWN, KEYUP,  BLUR);
        this.textRight = textRight;
        if (this.textRight) {
            if (templateRight == null) {
                templateRight = GWT.create(TemplateTextRight.class);
            }
        } else {
            if (template == null) {
                template = GWT.create(Template.class);
            }
        }
		if (renderer == null) {
			throw new IllegalArgumentException("renderer == null");
		}
		this.renderer = renderer;
	}

	@Override
	public boolean isEditing(Context context, Element parent, String value) {
		ViewData viewData = getViewData(context.getKey());
		return viewData == null ? false : viewData.isEditing();
	}

	@Override
	public void onBrowserEvent(Context context, Element parent, String value,
	                           NativeEvent event, ValueUpdater<String> valueUpdater) {
        super.onBrowserEvent(context, parent, value, event, valueUpdater);
		Object key = context.getKey();
		ViewData viewData = getViewData(key);
		if (viewData != null && viewData.isEditing()) {
			// Handle the edit event.
			editEvent(context, parent, value, viewData, event, valueUpdater);
		} else {
			String type = event.getType();
			int keyCode = event.getKeyCode();
			boolean enterPressed = KEYUP.equals(type) && keyCode == KeyCodes.KEY_ENTER;
			if (CLICK.equals(type) || enterPressed) {
                // Go into edit mode.
				if (viewData == null) {
					viewData = new ViewData(value);
					setViewData(key, viewData);
				} else {
					viewData.setEditing(true);
				}
				edit(context, parent, value);
			}
		}
	}

	@Override
	public void render(Context context, String value, SafeHtmlBuilder sb) {
		// Get the view data.
		Object key = context.getKey();
		ViewData viewData = getViewData(key);
		if (viewData != null && !viewData.isEditing() && value != null
				&& value.equals(viewData.getText())) {
			clearViewData(key);
			viewData = null;
		}

		String toRender = value;
		if (viewData != null) {
			String text = viewData.getText();
			if (viewData.isEditing()) {
        /*
         * Do not use the renderer in edit mode because the value of a text
         * input element is always treated as text. SafeHtml isn't valid in the
         * context of the value attribute.
         */
                sb.append(textRight ? templateRight.input(text) : template.input(text));
                return;
			} else {
				// The user pressed enter, but view data still exists.
				toRender = text;
			}
		}

		if (toRender != null && !toRender.trim().isEmpty()) {
			sb.append(renderer.render(toRender));
		} else {
      /*
       * Render a blank space to force the rendered element to have a height.
       * Otherwise it is not clickable.
       */
			sb.appendHtmlConstant("\u00A0");
		}
	}

	@Override
	public boolean resetFocus(Context context, Element parent, String value) {
		if (isEditing(context, parent, value)) {
			getInputElement(parent).focus();
			return true;
		}
		return false;
	}

	/**
	 * Convert the cell to edit mode.
	 *
	 * @param context the {@link Context} of the cell
	 * @param parent the parent element
	 * @param value the current value
	 */
	protected void edit(Context context, Element parent, String value) {
		setValue(context, parent, value);
		InputElement input = getInputElement(parent);
		addInputHandler(input);
		input.focus();
		input.select();

	}

	private native void addInputHandler(Element input)
	/*-{
        var that = this;
        var oldVal = input.value;

        if (input.addEventListener) {    // all browsers except IE before version 9
            input.addEventListener ("input", OnInput, true);
        } else {
            var changeWatcher = {
                timeout: null,
                currentValue: input.value,
                watchForChange: function( el ) {
                    if( el.value != this.currentValue ) {
                        this.changed( el );
                    }
                    this.timeout = setTimeout( function() {
                        changeWatcher.watchForChange(el)
                    }, 20 );
                },
                cancelWatchForChange: function() {
                    clearTimeout( this.timeout );
                    this.timeout = null;
                },
                changed: function( el ) {
                    var res = that.@com.aplana.sbrf.taxaccounting.web.widget.cell.ValidatedInputCell::checkInputtedValue(Ljava/lang/String;)(input.value);
                    if (!res ) {
                        input.value = this.currentValue
                    } else {
                        this.currentValue = el.value;
                    }
                }
            }
            changeWatcher.watchForChange(input);
        }

		function OnInput (event) {
		    res = that.@com.aplana.sbrf.taxaccounting.web.widget.cell.ValidatedInputCell::checkInputtedValue(Ljava/lang/String;)( event.target.value);
		    if (!res && (input.value !== oldVal)) {
		        input.value = oldVal;
		    } else {
		        oldVal = event.target.value;
		    }
		}


    }-*/;

	protected boolean checkInputtedValue(String value) {
		return false;
	}

	/**
	 * Convert the cell to non-edit mode.
	 *
	 * @param context the context of the cell
	 * @param parent the parent Element
	 * @param value the value associated with the cell
	 */
	private void cancel(Context context, Element parent, String value) {
		clearInput(getInputElement(parent));
		setValue(context, parent, value);
	}

	/**
	 * Clear selected from the input element. Both Firefox and IE fire spurious
	 * onblur events after the input is removed from the DOM if selection is not
	 * cleared.
	 *
	 * @param input the input element
	 */
	private native void clearInput(Element input) /*-{
		if (input.selectionEnd)
			input.selectionEnd = input.selectionStart;
		else if ($doc.selection)
			$doc.selection.clear();
	}-*/;

	/**
	 * Commit the current value.
	 *
	 * @param context the context of the cell
	 * @param parent the parent Element
	 * @param viewData the {@link ViewData} object
	 * @param valueUpdater the {@link ValueUpdater}
	 */
	private void commit(Context context, Element parent, ViewData viewData,
	                    ValueUpdater<String> valueUpdater) {
		String value = updateViewData(parent, viewData, false);
		clearInput(getInputElement(parent));
		setValue(context, parent, viewData.getOriginal());
		if (valueUpdater != null) {
			valueUpdater.update(value);
		}
	}

	private void editEvent(Context context, Element parent, String value,
	                       ViewData viewData, NativeEvent event, ValueUpdater<String> valueUpdater) {
		String type = event.getType();
		boolean keyUp = KEYUP.equals(type);
		boolean keyDown = KEYDOWN.equals(type);
		if (keyUp || keyDown) {
			int keyCode = event.getKeyCode();
			if (keyUp && keyCode == KeyCodes.KEY_ENTER) {
				// Commit the change.
				commit(context, parent, viewData, valueUpdater);
			} else if (keyUp && keyCode == KeyCodes.KEY_ESCAPE) {
				// Cancel edit mode.
				String originalText = viewData.getOriginal();
				if (viewData.isEditingAgain()) {
					viewData.setText(originalText);
					viewData.setEditing(false);
				} else {
					setViewData(context.getKey(), null);
				}
				cancel(context, parent, value);
			} else {
				// Update the text in the view data on each key.
				updateViewData(parent, viewData, true);
			}
		} else if (BLUR.equals(type)) {
			// Commit the change. Ensure that we are blurring the input element and
			// not the parent element itself.
			EventTarget eventTarget = event.getEventTarget();
			if (Element.is(eventTarget)) {
				Element target = Element.as(eventTarget);
				if ("input".equals(target.getTagName().toLowerCase())) {
					commit(context, parent, viewData, valueUpdater);
				}
			}
		}
	}

	/**
	 * Get the input element in edit mode.
	 */
	public InputElement getInputElement(Element parent) {
		return parent.getFirstChild().<InputElement> cast();
	}

	/**
	 * Update the view data based on the current value.
	 *
	 * @param parent the parent element
	 * @param viewData the {@link ViewData} object to update
	 * @param isEditing true if in edit mode
	 * @return the new value
	 */
	private String updateViewData(Element parent, ViewData viewData,
	                              boolean isEditing) {
		InputElement input = (InputElement) parent.getFirstChild();
		String value = input.getValue();
		viewData.setText(value);
		viewData.setEditing(isEditing);
		return value;
	}
}
