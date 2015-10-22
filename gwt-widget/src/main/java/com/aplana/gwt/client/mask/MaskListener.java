package com.aplana.gwt.client.mask;

import com.google.gwt.event.dom.client.*;

/**
 * Установщик обработчиков для текстового ввода с маской.
 * Учитывается вариант если есть выделение в тексте
 *
 * @author aivanov
 */
public class MaskListener {

    public static final int F1 = 112;
    public static final int F12 = 124;

    private String mask;
    /**
     * Образец маски, котрый отображается для пользователя
     */
    private String picture;
    private boolean isDebug = false;

    private final MaskBox textbox;

    public MaskListener(MaskBox box, String msk) {
        this.mask = msk;
        this.textbox = box;

        // Рисуем пользовательскую маску
        picture = MaskUtils.createMaskPicture(mask);

        /*
         *   Через KeyDown обрабатываем удаление
         */
        textbox.addKeyDownHandler(new KeyDownHandler() {
            @Override
            public void onKeyDown(KeyDownEvent event) {
                int nativeKeyCode = event.getNativeKeyCode();
                debug("KeyDown " + nativeKeyCode);

                if ((event.isAnyModifierKeyDown() && !event.isShiftKeyDown()) ||
                        (nativeKeyCode >= F1 && nativeKeyCode <= F12)) {
                    debug("KeyDown F1-F12, and non Shift press");
                    return;
                }

                if (nativeKeyCode == KeyCodes.KEY_BACKSPACE || nativeKeyCode == KeyCodes.KEY_DELETE) {
                    debug("KeyDown: baskspace or delete");

                    StringBuffer applied;   //буфер

                    String input = textbox.getText();
                    int cursor = textbox.getCursorPos();

                    if (!input.isEmpty() && textbox.getSelectionLength() > 0) {
                        debug("KeyDown: textbox has selection");

                        applied = new StringBuffer();

                        int selectStart = textbox.getText().indexOf(textbox.getSelectedText());  // Начальная позиция выделения
                        int selectEnd = selectStart + textbox.getSelectionLength();              // Конечная

                        applied.append(input.substring(0, selectStart));  // Копируем в буфер  часть что до начала позиции выделения

                        for (int i = 0; i < textbox.getSelectionLength(); i++) {
                            if (mask.toCharArray()[applied.length()] == '9' || mask.toCharArray()[applied.length()] == 'X')
                                applied.append('_');
                            else
                                applied.append(mask.toCharArray()[applied.length()]);
                        }

                        applied.append(input.substring(selectEnd));     // Копируем в буфер часть что после конца позиции выделения

                        // Если получивщийся текст пустой то вставляем маску для пользователя
                        input = applied.length() != 0 ? applied.toString() : picture;

                        cursor = selectStart;
                    }

                    //Вручную сдвигаем курсор, так как событие нажатия на бекспей не прокидывается дальше
                    if (nativeKeyCode == KeyCodes.KEY_BACKSPACE)
                        cursor--;

                    if (cursor < 0) {
                        textbox.setText(picture);
                        textbox.removeExceptionStyle();
                        textbox.setCursorPos(0);
                        return;
                    }

                    applied = new StringBuffer();

                    // если курсор в самом конце - не обратаываем нажатие
                    if(cursor >= mask.length()){
                        return;
                    }
                    char mc = mask.charAt(cursor);  // символ в маске под текущей позицией курсора

                    //Заменяем введенный символ на прочерк если он попадает под маску
                    if ((mc == '9' || mc == 'X') && !input.isEmpty()) {
                        applied.append(input.substring(0, cursor));
                        applied.append("_");
                        applied.append(input.substring(cursor + 1));
                    } else {
                        applied.append(input);
                    }

                    textbox.setText(applied.length() != 0 ? applied.toString() : picture);
                    if (applied.length() != 0) {
                        textbox.setCursorPos(cursor);
                    }

                    textbox.removeExceptionStyle();

                    event.preventDefault();
                    event.stopPropagation();

                }
            }
        });

		/*
         * В KeyPressEvent можно достучаться до кода клавиши в ASCII, используем его
		 */
        textbox.addKeyPressHandler(new KeyPressHandler() {
            @Override
            public void onKeyPress(KeyPressEvent event) {

                int keyCode = event.getNativeEvent().getKeyCode();
                char ch = event.getCharCode();                          // клавиша которую нажал пользователь
                debug("KeyPress " + event.getCharCode());
                StringBuffer applied;                                   // буфер

                if (event.isAnyModifierKeyDown() && !event.isShiftKeyDown()) {
                    return;
                }

                String input;
                if (textbox.getText() == null || textbox.getText().isEmpty()) {
                    input = picture;
                } else {
                    input = textbox.getText();
                }

                int cursor = textbox.getCursorPos();

                if (!textbox.getText().isEmpty() && textbox.getSelectionLength() > 0) {
                    debug("KeyPress: textbox has selection");
                    applied = new StringBuffer();

                    int selectStart = textbox.getText().indexOf(textbox.getSelectedText());
                    int selectEnd = selectStart + textbox.getSelectionLength();

                    applied.append(input.substring(0, selectStart));

                    for (int i = 0; i < textbox.getSelectionLength(); i++) {
                        if (mask.toCharArray()[applied.length()] == '9' || mask.toCharArray()[applied.length()] == 'X')
                            applied.append('_');
                        else
                            applied.append(mask.toCharArray()[applied.length()]);
                    }

                    applied.append(input.substring(selectEnd));    // Копируем часть ввода после выбора в буфер

                    input = applied.toString();

                    cursor = selectStart;
                }

                applied = new StringBuffer(input);                  // новый буфер

                // ничег оне делаем если нажаты эти клавиши
                if (keyCode == KeyCodes.KEY_TAB || keyCode == KeyCodes.KEY_LEFT || keyCode == KeyCodes.KEY_RIGHT) {
                    return;
                }

                if (cursor >= mask.length()) {
                    debug("KeyPress: has limit on length of mask " + cursor);
                    return;
                }

                char mc = mask.charAt(cursor);  // символ в маске под текущей позицией курсора

                boolean loop;
                do {
                    loop = false;
                    switch (mc) {
                        case '9':
                            if (Character.isDigit(ch)) {
                                applied = applied.deleteCharAt(cursor);
                                applied.insert(cursor, ch);
                            } else
                                cursor--;
                            break;
                        case 'X':
                            if (Character.isLetterOrDigit(ch)) {
                                applied = applied.deleteCharAt(cursor);
                                applied.insert(cursor, ch);
                            } else
                                cursor--;
                            break;
                        default:
                            // Перескакивание разделителей
                            if (mc != ch) {
                                loop = true;
                                cursor++;
                                mc = mask.charAt(cursor);
                            }
                    }
                } while (loop);

                cursor++;

                textbox.setText(applied.toString());
                textbox.setCursorPos(cursor);
                textbox.removeExceptionStyle();

                event.preventDefault();
                event.stopPropagation();
            }
        });
    }

    public String getMask() {
        return mask;
    }

    public void setMask(String mask) {
        this.mask = mask;
    }

    public void setDebug(boolean isDebug) {
        this.isDebug = isDebug;
    }

    public boolean isDebug() {
        return isDebug;
    }

    public String getMaskPicture() {
        return picture;
    }

    private void debug(String s) {
        if (isDebug) {
            System.out.println(s);
        }
    }

}
