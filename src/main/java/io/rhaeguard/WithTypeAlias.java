package io.rhaeguard;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
@Repeatable(WithTypeAliases.class)
public @interface WithTypeAlias {
    String alias();
    Class<?> aliasFor();
}
