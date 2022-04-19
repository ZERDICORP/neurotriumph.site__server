package site.neurotriumph.www.annotation;

import site.neurotriumph.www.constant.TokenMarker;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface WithConfirmationToken {
  TokenMarker value();
}