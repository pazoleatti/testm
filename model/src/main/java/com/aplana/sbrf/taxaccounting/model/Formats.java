package com.aplana.sbrf.taxaccounting.model;

public enum Formats {
	NONE       (0, ""),
	DD_MM_YYYY (1, "dd.MM.yyyy"),
	MM_YYYY    (2, "MM.yyyy"),
	MMM_YYYY   (3, "MMMM yyyy"),
	YYYY       (4, "yyyy");

	public static String[] months = new String[]{
			"Январь",
			"Февраль",
			"Март",
			"Апрель",
			"Май",
			"Июнь",
			"Июль",
			"Август",
			"Сентябрь",
			"Октябрь",
			"Ноябрь",
			"Декабрь"
	};

	private Formats(int id, String format) {
		this.id = id;
		this.format = format;
	}
	private final int id;
	private final String format;

	public int getId() {
		return id;
	}
	
	public String getFormat() {
		return format;
	}

	public static Formats getById(int id) {
		for (Formats f: values()) {
			if (f.id == id) {
				return f;
			}
		}
		throw new IllegalArgumentException("Wrong format id: " + id);
	}

	public static String getRussianMonthName(int monthNum) {
		return months[monthNum];

	}
}
