package ru.aosivt.rasterparquet.utils;

import java.lang.reflect.Field;
import java.util.Objects;
import ru.aosivt.rasterparquet.errors.ReflectException;

public final class CommonUtils {

    private static Object getFieldValue(String fieldName, Object e) {
        Field fld = extractField(e.getClass(), fieldName);
        Object obj = null;
        try {
            obj = fld.get(e);
        } catch (IllegalAccessException ex) {
            ex.printStackTrace();
        }
        return obj;
    }

    public static Field extractField(Class<?> aClass, String fieldName) {

        if (Objects.isNull(aClass)) return null;

        Field[] fields = aClass.getDeclaredFields();

        for (Field field : fields) {
            if (fieldName.equals(field.getName())) {
                field.setAccessible(true);
                return field;
            }
        }
        Field field = extractField(aClass.getSuperclass(), fieldName);
        if (Objects.nonNull(field)) return field;
        throw new ReflectException("в классе " + aClass + " отстуствует поле " + fieldName);
    }
}
