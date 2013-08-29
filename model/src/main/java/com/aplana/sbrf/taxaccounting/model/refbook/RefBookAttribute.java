package com.aplana.sbrf.taxaccounting.model.refbook;

import java.io.Serializable;

/**
 * Атрибут справочника
 *
 * @author <a href="mailto:Marat.Fayzullin@aplana.com">Файзуллин Марат</a>
 * @since 23.05.13 17:04
 */
public class RefBookAttribute implements Serializable {

	private static final Long serialVersionUID = 1L;

	/** Код атрибута */
	private Long id;

	/** Наименование атрибута */
	private String name;

	/** Псевдоним атрибута */
	private String alias;

	/** Тип атрибута */
	private RefBookAttributeType attributeType;

	/** Если тип атрибута "Ссылка", то данное поле задает справочник, из которого будут подставляться данные. Например,
	 * для справочника "Книга" в атрибуте "Автор" указываем код справочника "Человек".
	 */
	private Long refBookId;

	/** Если тип атрибута "Ссылка", то данное поле задает код отображаемого атрибута. Например, у справочника "Книга"
	 * есть поле "Автор", ссылающее на справочник "Человек". При отображении элементов справочника "Книга" в столбце
	 * "Автор" пользователь ожидает увидеть не код человека, а его ФИО. Для указания того, какое поле отображать и
	 * предназначено данное поле.
	 */
	private Long refBookAttributeId;

	/** Признак видимости атрибута */
	private boolean visible;

	/** Точность. Количество знаков после запятой. Используется для форматирования вещественных значений при выводе
	 * на экран
 	 */
	private int precision;

	/** Ширина атрибута в относительных единицах по аналогии с НФ. Используется для задания ширины столбца при
	 * отображении таблицы справочника на экране.
	 */
	private int width;

	/**
	 * Возвращает код атрибута
	 * @return код атрибута
	 */
	public Long getId() {
		return id;
	}

	/**
	 * Устанавливает код атрибута
	 * @param id код атрибута
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * Возвращает наименование атрибута
	 * @return наименование атрибута
	 */
	public String getName() {
		return name;
	}

	/**
	 * Устанавливает наименование атрибута
	 * @param name наименование атрибута
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Возвращает тип атрибута
	 * @return тип атрибута
	 */
	public RefBookAttributeType getAttributeType() {
		return attributeType;
	}

	/**
	 * Устанавливает тип атрибута
	 * @param attributeType тип атрибута
	 */
	public void setAttributeType(RefBookAttributeType attributeType) {
		this.attributeType = attributeType;
	}

	/**
	 * Возвращает код атрибута для отображения
	 * @return код атрибута
	 */
	public Long getRefBookAttributeId() {
		return refBookAttributeId;
	}

	/**
	 * Устанавливает код отображаемого атрибута
	 * @param refBookAttributeId
	 */
	public void setRefBookAttributeId(Long refBookAttributeId) {
		this.refBookAttributeId = refBookAttributeId;
	}

	/**
	 * Возвращает код справочника, на которое ссылается атрибут
	 * @return код справочника
	 */
	public Long getRefBookId() {
		return refBookId;
	}

	/**
	 * Устанавливает код справочника, на который будет ссылаться атрибут
	 * @param refBookId код справочника
	 */
	public void setRefBookId(Long refBookId) {
		this.refBookId = refBookId;
	}

	/**
	 * Возвращает значение видимости атрибута
	 * @return true - атрибут виден; false - не виден
	 */
	public boolean isVisible() {
		return visible;
	}

	/**
	 * Задает видимость атрибута
	 * @param visible true - атрибут виден; false - не виден
	 */
	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	/**
	 * Возвращает значение точности атрибута
	 * @return точность [кол-во знаков после запятой]
	 */
	public int getPrecision() {
		return precision;
	}

	/**
	 * Устанавливает точность атрибута
	 * @param precision точность [кол-во знаков после запятой]
	 */
	public void setPrecision(int precision) {
		this.precision = precision;
	}

	/**
	 * Возвращает ширину столбца для отображение значений атрибута
	 * @return ширина столбца в относительных единицах
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * Устанавливает ширину столбца
	 * @param width ширина столбца в относительных единицах
	 */
	public void setWidth(int width) {
		this.width = width;
	}

	/**
	 * Возвращает псевдоним атрибута
	 * @return псевдоним
	 */
	public String getAlias() {
		return alias;
	}

	/**
	 * Устанавливает псевдоним атрибута
	 * @param alias псевдоним
	 */
	public void setAlias(String alias) {
		this.alias = alias;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		RefBookAttribute that = (RefBookAttribute) o;
		if (precision != that.precision) {
			return false;
		}
		if (visible != that.visible) {
			return false;
		}
		if (width != that.width) {
			return false;
		}
		if (alias != null ? !alias.equals(that.alias) : that.alias != null) {
			return false;
		}
		if (attributeType != that.attributeType) {
			return false;
		}
		if (id != null ? !id.equals(that.id) : that.id != null) {
			return false;
		}
		if (name != null ? !name.equals(that.name) : that.name != null) {
			return false;
		}
		if (refBookAttributeId != null ? !refBookAttributeId.equals(that.refBookAttributeId) : that.refBookAttributeId != null) {
			return false;
		}
		if (refBookId != null ? !refBookId.equals(that.refBookId) : that.refBookId != null) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		return id != null ? id.hashCode() : 0;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("RefBookAttribute{");
		sb.append("alias='").append(alias).append('\'');
		sb.append(", id=").append(id);
		sb.append(", name='").append(name).append('\'');
		sb.append(", attributeType=").append(attributeType);
		sb.append(", refBookId=").append(refBookId);
		sb.append(", refBookAttributeId=").append(refBookAttributeId);
		sb.append(", visible=").append(visible);
		sb.append(", precision=").append(precision);
		sb.append(", width=").append(width);
		sb.append('}');
		return sb.toString();
	}
}