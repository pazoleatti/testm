package com.aplana.sbrf.taxaccounting.model;

/**
 * Категории прикрепленных файлов
 */
public enum AttachFileType {
	TYPE_1(1, "ТФ"),
    TYPE_2(2, "Исходящий в ФНС"),
    TYPE_3(3, "Входящий из ФНС"),
    TYPE_4(4, "Отчет"),
    TYPE_5(5, "Протокол ошибок"),
    TYPE_6(6, "Прочее");

	private final int id;
	private final String title;

	private AttachFileType(int id, String title) {
		this.id = id;
		this.title = title;
	}
	
	public int getId() {
		return id;
	}
	
	public String getTitle() {
		return title;
	}
	
	public static AttachFileType fromId(int kindId) {
		for (AttachFileType kind: values()) {
			if (kind.id == kindId) {
				return kind;
			}
		}
		throw new IllegalArgumentException("Wrong AttachFileType id: " + kindId);
	}

    public static AttachFileType fromName(String name) {
        for (AttachFileType kind: values()) {
            if (kind.title.equals(name)) {
                return kind;
            }
        }
        throw new IllegalArgumentException("Wrong AttachFileType id: " + name);
    }
}
