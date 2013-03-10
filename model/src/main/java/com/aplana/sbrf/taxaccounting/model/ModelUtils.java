/**
 * 
 */
package com.aplana.sbrf.taxaccounting.model;

import java.util.Collection;

/**
 * Утилиты для работы с моделью
 * @author sgoryachkin
 *
 */
public final class ModelUtils {

	/**
	 * Запрещаем создавать экземляры класса
	 */
	private ModelUtils() {}
	
	public static interface GetPropertiesFunc<T, V>{
		V getProperties(T object);
	}
	
	/**
	 * Поиск объекта в коллекции по значению свойства
	 * @param collection
	 * @param value
	 * @param func
	 * @return
	 */
	public static <T, V> T findByProperties(Collection<T> collection, V value, GetPropertiesFunc<T, V> func){
		for (T object : collection) {
			if (value.equals(func.getProperties(object))){
				return object;
			}
		}
		return null;
	}
	
	public static <T> boolean containsLink(Collection<T> collection, T linkTo){
		for (T object : collection) {
			if (object == linkTo){
				return true;
			}
		}
		return false;
	}
	
	//mapByProperties(){
	//	
	//}

}
