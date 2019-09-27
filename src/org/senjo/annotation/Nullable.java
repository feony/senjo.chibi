package org.senjo.annotation;

import static java.lang.annotation.ElementType.*;
import java.lang.annotation.*;

@Target({PARAMETER, METHOD, FIELD, LOCAL_VARIABLE}) @Retention(RetentionPolicy.CLASS)
public @interface Nullable { }


