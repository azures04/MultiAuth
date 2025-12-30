package fr.azures04.mods.multiauth.helpers;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class ReflectionHelper {

	public static Field findField(Class<?> clazz, String devName, String srgName) throws NoSuchFieldException {
        try {
            return clazz.getDeclaredField(devName);
        } catch (NoSuchFieldException e) {
            try {
                return clazz.getDeclaredField(srgName);
            } catch (NoSuchFieldException e2) {
                throw e2;
            }
        }
    }
	
	public static void removeFinalModifier(Field field) throws Exception {
        field.setAccessible(true);
        
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
    }
}
