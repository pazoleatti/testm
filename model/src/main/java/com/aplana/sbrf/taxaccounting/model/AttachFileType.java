package com.aplana.sbrf.taxaccounting.model;

/**
 * Категории прикрепленных файлов
 */
public enum AttachFileType {
    TYPE_1(21657200, 1, "ТФ"),
    TYPE_2(21657300, 2, "Исходящий в ФНС"),
    TYPE_3(21657400, 3, "Входящий из ФНС"),
    TYPE_4(21657500, 4, "Отчет"),
    TYPE_5(21657600, 5, "Протокол ошибок"),
    TYPE_6(21657700, 6, "Прочее");

    private final long id;
    private final int code;
    private final String title;

    private AttachFileType(long id, int code, String title) {
        this.id = id;
        this.code = code;
        this.title = title;
    }

    public long getId() {
        return id;
    }

    public int getCode() {
        return code;
    }

    public String getTitle() {
        return title;
    }

    public static AttachFileType fromId(long kindId) {
        for (AttachFileType kind : values()) {
            if (kind.id == kindId) {
                return kind;
            }
        }
        throw new IllegalArgumentException("Wrong AttachFileType id: " + kindId);
    }

    public static AttachFileType fromCode(int kindCode) {
        for (AttachFileType kind : values()) {
            if (kind.code == kindCode) {
                return kind;
            }
        }
        throw new IllegalArgumentException("Wrong AttachFileType id: " + kindCode);
    }

    public static AttachFileType fromName(String name) {
        for (AttachFileType kind : values()) {
            if (kind.title.equals(name)) {
                return kind;
            }
        }
        throw new IllegalArgumentException("Wrong AttachFileType id: " + name);
    }
}
