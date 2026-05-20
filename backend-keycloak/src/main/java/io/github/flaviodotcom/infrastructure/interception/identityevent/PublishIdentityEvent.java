package io.github.flaviodotcom.infrastructure.interception.identityevent;

import jakarta.enterprise.util.Nonbinding;
import jakarta.interceptor.InterceptorBinding;

import java.lang.annotation.*;

@Inherited
@InterceptorBinding
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface PublishIdentityEvent {

    @Nonbinding
    String eventType();

    @Nonbinding
    String subjectType();
}
