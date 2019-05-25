package org.btelman.pref.processor;

import org.btelman.prefs.annotation.PreferenceBool;

import javax.lang.model.element.Element;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

/**
 * Created by Brendon on 5/25/2019.
 */
public class AnnotatedPreferenceBoolean extends AnnotatedPreference{

    public final boolean defaultBool;
    private final int strId;

    AnnotatedPreferenceBoolean(Element element) {
        super(element);
        defaultBool = element.getAnnotation(PreferenceBool.class).defaultObject();
        strId = element.getAnnotation(PreferenceBool.class).id();
    }

    @Override
    boolean isTypeValid(Elements elements, Types types) {
        return true;//element.asType().getKind().equals(TypeKind.BOOLEAN);
    }

    @Override
    int getResId() {
        return strId;
    }

    @Override
    Object getDefaultValue() {
        return defaultBool;
    }
}
