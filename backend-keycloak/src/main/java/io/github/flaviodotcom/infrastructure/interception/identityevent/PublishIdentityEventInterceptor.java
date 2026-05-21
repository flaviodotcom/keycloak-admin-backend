package io.github.flaviodotcom.infrastructure.interception.identityevent;

import io.github.flaviodotcom.infrastructure.interception.contracts.ActionPayload;
import jakarta.annotation.Priority;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import lombok.AllArgsConstructor;

@PublishIdentityEvent(eventType = "", subjectType = "")
@Interceptor
@Priority(Interceptor.Priority.APPLICATION)
@AllArgsConstructor
public class PublishIdentityEventInterceptor {

    private final IdentityEventActionHandler handler;

    @AroundInvoke
    public Object intercept(InvocationContext context) throws Exception {
        var result = context.proceed();

        var annotation = this.resolveAnnotation(context);

        if (annotation == null) {
            return result;
        }

        if (result instanceof ActionPayload payload) {
            this.handler.handle(annotation, payload);
        }

        return result;
    }

    private PublishIdentityEvent resolveAnnotation(InvocationContext context) {
        var methodAnnotation = context.getMethod()
                .getAnnotation(PublishIdentityEvent.class);

        if (methodAnnotation != null) {
            return methodAnnotation;
        }

        return context.getTarget()
                .getClass()
                .getAnnotation(PublishIdentityEvent.class);
    }
}