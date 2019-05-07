package com.aplana.sbrf.taxaccounting.model;

public enum Formats {
	NONE       (0, "", ""),
	DD_MM_YYYY (1, "dd.MM.yyyy", "99.99.9999"),
	MM_YYYY    (2, "MM.yyyy", "99.9999"),
	MMM_YYYY   (3, "MMMM yyyy", "99.9999"),
	YYYY       (4, "yyyy", "9999"),
	DD_MM	   (5, "dd.MM", "99.99"),
	BOOLEAN    (6, "", ""), // формат для числовых атрибутов, чтобы их отображать в виде чек-боксов
	DD_MM_YYYY_HH_MM_SS(7, "dd.MM.yyyy HH:mm:ss", "99.99.9999 99:99:99");

	private final int id;
	private final String format;
	private final String mask;

	Formats(int id, String format, String mask) {
		this.id = id;
		this.format = format;
		this.mask = mask;
	}

	public int getId() {
		return id;
	}
	
	public String getFormat() {
		return format;
	}

    public String getMask() {
        return mask;
    }

    public static Formats getById(int id) {
		for (Formats f: values()) {
			if (f.id == id) {
				return f;
			}
		}
		throw new IllegalArgumentException("Wrong format id: " + id);
	}
}