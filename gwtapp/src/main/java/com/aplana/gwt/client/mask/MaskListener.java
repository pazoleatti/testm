package com.aplana.gwt.client.mask;

import com.google.gwt.event.dom.client.*;

/**
 * Установщик обработчиков для текстового ввода с маской.
 * Учитывается вариант если есть выделение в тексте
 *
 * @author aivanov
 */
public class MaskListener {

    public static int F1 = 112;
    public static int F12 = 124;

    private String mask;
    /**
     * Образец маски, котрый отображается для пользователя
     */
    private String picture;

    private final MaskBox textbox;

    public MaskListener(MaskBox box, String msk) {
        this.mask = msk;
        this.textbox = box;

        // Рисуем пользовательскую маску
        StringBuffer pic = new StringBuffer();

        for (char mc : mask.toCharArray()) {
            switch (mc) {
                case '9':
                case 'X':
                    pic.append("_");
                    break;
                default:
                    pic.append(mc);
            }
        }
        picture = pic.toString();

        /*
         *   Через KeyDown обрабатываем удаление
         */
        textbox.addKeyDownHandler(new KeyDownHandler() {
            public void onKeyDown(KeyDownEvent event) {
                int nativeKeyCode = event.getNativeKeyCode();
                System.out.println("KeyDown " + nativeKeyCode);

                StringBuffer applied;   //буфер

                if ((event.isAnyModifierKeyDown() && !event.isShiftKeyDown()) ||
                        (nativeKeyCode >= F1 && nativeKeyCode <= F12)) {
                    System.out.println("KeyDown F1-F12, and non Shift press");
                    return;
                }

                String input = textbox.getText();
                int cursor = textbox.getCursorPos();

                if (nativeKeyCode == KeyCodes.KEY_BACKSPACE || nativeKeyCode == KeyCodes.KEY_DELETE) {
                    System.out.println("KeyDown: baskspace or delete");

                    if (textbox.getSelectionLength() > 0) {
                        System.out.println("KeyDown: textbox has selection");

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

                    char mc = mask.charAt(cursor);  // символ в маске под текущей позицией курсора
                    applied = new StringBuffer();

                    //Заменяем введенный символ на прочерк если он попадает под маску
                    if (mc == '9' || mc == 'X') {
                        applied.append(input.substring(0, cursor));
                        applied.append("_");
                        applied.append(input.substring(cursor + 1));
                    } else
                        applied.append(input);

                    System.out.println(applied.length());
                    textbox.setText(applied.length() != 0 ? applied.toString() : picture);
                    textbox.setCursorPos(cursor);

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
            public void onKeyPress(KeyPressEvent event) {

                int keyCode = event.getNativeEvent().getKeyCode();
                char ch = event.getCharCode();                          // клавиша которую нажал пользователь
                System.out.println("KeyPress " + event.getCharCode());
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

                if (textbox.getSelectionLength() > 0) {
                    System.out.println("KeyPress: textbox has selection");
                    applied = new StringBuffer();

                    int selectStart = textbox.getText().indexOf(textbox.getSelectedText());
                    int selectEnd = selectStart + textbox.getSelectionLength();

                    applied.append(input.substring(0, selectStart));

                    for (int i = 0; i < textbox.getSelectionLength(); i++) {
                        if (mask.toCharArray()[applied.length()] == '9' ||
                                mask.toCharArray()[applied.length()] == 'X')
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
                    System.out.println("KeyPress: has limit on length of mask " + cursor);
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

    public String getMaskPicture() {
        return picture;
    }

}
