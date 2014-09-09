package com.aplana.gwt.client.dialog;

import com.aplana.gwt.client.ModalWindow;
import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Image;

/**
 * Класс содержит статические методы вывода диалогового и информационного сообщений.
 * User: vpetrov
 * Date: 14.01.14
 * Time: 18:43
 */
public final class Dialog extends ModalWindow {

    public static enum PredefinedButton {
        YES, NO, OK, CANCEL, CLOSE
    }

    public static String WARNING_MESSAGE = "Внимание!";
    public static String ERROR_MESSAGE = "Ошибка";
    public static String INFO_MESSAGE = "Информация";
    public static String CONFIRM_MESSAGE = "Подтверждение";

    private static final Dialog INSTANCE = new Dialog();

    private static DialogPanel dialogPanel = new DialogPanel();
    private static ModalWindowResources mwRes = GWT.create(ModalWindowResources.class);

    interface ModalWindowResources extends ClientBundle {
        @Source("icon-info.gif")
        @ImageResource.ImageOptions(repeatStyle = ImageResource.RepeatStyle.Both, preventInlining = true)
        ImageResource infoImage();

        @Source("icon-error.gif")
        @ImageResource.ImageOptions(repeatStyle = ImageResource.RepeatStyle.Both, preventInlining = true)
        ImageResource errorImage();

        @Source("icon-question.gif")
        @ImageResource.ImageOptions(repeatStyle = ImageResource.RepeatStyle.Both, preventInlining = true)
        ImageResource questionImage();

        @Source("icon-warning.gif")
        @ImageResource.ImageOptions(repeatStyle = ImageResource.RepeatStyle.Both, preventInlining = true)
        ImageResource warningImage();
    }

    public static Image infoImage = new Image(mwRes.infoImage());
    public static Image errorImage = new Image(mwRes.errorImage());
    public static Image questionImage = new Image(mwRes.questionImage());
    public static Image warningImage = new Image(mwRes.warningImage());

    private boolean initDialog = false;

    private Dialog() {
        isResizable = false;
    }

    /**
     * Метод отображает окно сообщения.
     * Его вызываеют другие методы, после предварительной настройки
     *
     * @param title   - заголовок сообщения
     * @param text    - текст сообщения
     * @param handler - обработчик
     */
    private static void showDialog(String title, String text, DialogHandler handler) {
        INSTANCE.setDialogPanel();
        dialogPanel.setDialogHandler(handler);
        dialogPanel.setText(text);
        INSTANCE.setTitle(title);
        INSTANCE.show();
        INSTANCE.center();
    }

    /**
     * Добавляет в диалоговое окно виджет на котором размещены все элементы
     */
    private void setDialogPanel() {
        if (!initDialog)
            add(dialogPanel);
        initDialog = true;
    }

    /**
     * Вызывает окно предупреждения.
     *
     * @param text - Текст сообщения
     */
    public static void warningMessage(String text) {
        warningMessage(WARNING_MESSAGE, text);
    }

    /**
     * Вызывает окно предупреждения.
     *
     * @param title - Заголовок
     * @param text  - Текст сообщения
     */
    public static void warningMessage(String title, String text) {
        warningMessage(title, text, new DialogHandler() {
            @Override
            public void close() {
                Dialog.hideMessage();
            }
        });
    }

    /**
     * Вызывает окно предупреждения.
     *
     * @param text    - Текст сообщения
     * @param handler - обарботчик
     */
    public static void warningMessage(String text, DialogHandler handler) {
        warningMessage(WARNING_MESSAGE, text, handler);
    }

    /**
     * Вызывает окно предупреждения.
     *
     * @param title   - Заголовок
     * @param text    - Текст сообщения
     * @param handler - обработчик
     */
    public static void warningMessage(String title, String text, DialogHandler handler) {
        INSTANCE.setGlassEnabled(true);
        dialogPanel.setPredefinedButtons(PredefinedButton.CLOSE);
        dialogPanel.setImage(warningImage);
        dialogPanel.setImageVisible(true);
        showDialog(title, text, handler);
    }

    /**
     * Вызывает окно с сообщением об ошибке
     *
     * @param text - Текст сообщения
     */
    public static void errorMessage(String text) {
        errorMessage(ERROR_MESSAGE, text);
    }

    /**
     * Вызывает окно с сообщением об ошибке
     *
     * @param title - Заголовок
     * @param text  - Текст сообщения
     */
    public static void errorMessage(String title, String text) {
        errorMessage(title, text, new DialogHandler() {});
    }

    /**
     * Вызывает окно с сообщением об ошибке
     *
     * @param text    - Текст сообщения
     * @param handler - обработчик
     */
    public static void errorMessage(String text, DialogHandler handler) {
        errorMessage(ERROR_MESSAGE, text, handler);
    }

    /**
     * Вызывает окно с сообщением об ошибке
     *
     * @param title   - Заголовок
     * @param text    - Текст сообщения
     * @param handler - обработчик
     */
    public static void errorMessage(String title, String text, DialogHandler handler) {
        INSTANCE.setGlassEnabled(true);
        dialogPanel.setPredefinedButtons(PredefinedButton.CLOSE);
        dialogPanel.setImage(errorImage);
        dialogPanel.setImageVisible(true);
        showDialog(title, text, handler);
    }

    /**
     * Вызывает информационное окно с кнопкой "ОК"
     *
     * @param text - Текст сообщения
     */
    public static void infoMessage(String text) {
        infoMessage(INFO_MESSAGE, text);
    }

    /**
     * Вызывает информационное окно с кнопкой "ОК"
     *
     * @param title - Заголовок
     * @param text  - Текст сообщения
     */
    public static void infoMessage(String title, String text) {
        infoMessage(title, text, new DialogHandler(){});
    }

    /**
     * Вызывает информационное окно с кнопкой "ОК"
     *
     * @param text    - Текст сообщения
     * @param handler - обработчик
     */
    public static void infoMessage(String text, DialogHandler handler) {
        infoMessage(INFO_MESSAGE, text, handler);
    }

    /**
     * Вызывает информационное окно с кнопкой "ОК"
     *
     * @param title   - Заголовок
     * @param text    - Текст сообщения
     * @param handler - обработчик
     */
    public static void infoMessage(String title, String text, DialogHandler handler) {
        INSTANCE.setGlassEnabled(false);
        dialogPanel.setPredefinedButtons(PredefinedButton.OK);
        dialogPanel.setImage(infoImage);
        dialogPanel.setImageVisible(true);
        showDialog(title, text, handler);
    }

    /**
     * Вызывает диалоговое окно с кнопками "да", "нет", "закрыть"
     *
     * @param text - Текст сообщения
     */
    public static void confirmMessage(String text) {
        confirmMessage(CONFIRM_MESSAGE, text);
    }

    /**
     * Вызывает диалоговое окно с кнопками "да", "нет", "закрыть"
     *
     * @param title - Заголовок
     * @param text  - Текст сообщения
     */
    public static void confirmMessage(String title, String text) {
        confirmMessage(title, text, new DialogHandler() {
            @Override
            public void yes() {
                super.yes();
                Dialog.hideMessage();
            }

            @Override
            public void no() {
                super.no();
                Dialog.hideMessage();
            }
        });
    }

    /**
     * Вызывает диалоговое окно с кнопками "да", "нет", "закрыть"
     *
     * @param text    - Текст сообщения
     * @param handler - обработчик
     */
    public static void confirmMessage(String text, DialogHandler handler) {
        confirmMessage(CONFIRM_MESSAGE, text, handler);
    }

    /**
     * Вызывает диалоговое окно с кнопками "да", "нет", "закрыть"
     *
     * @param title   - Заголовок
     * @param text    - Текст сообщения
     * @param handler - обработчик
     */
    public static void confirmMessage(String title, String text, DialogHandler handler) {
        INSTANCE.setGlassEnabled(true);
        dialogPanel.setPredefinedButtons(PredefinedButton.YES, PredefinedButton.NO, PredefinedButton.CLOSE);
        dialogPanel.setImage(questionImage);
        dialogPanel.setImageVisible(true);
        showDialog(title, text, handler);
    }

    /**
     * Скрывает окно сообщения
     */
    public static void hideMessage() {
        INSTANCE.hide();
    }
}
