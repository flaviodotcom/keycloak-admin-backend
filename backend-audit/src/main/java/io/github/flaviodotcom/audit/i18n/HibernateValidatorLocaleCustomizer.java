package io.github.flaviodotcom.audit.i18n;

import io.quarkus.hibernate.validator.ValidatorFactoryCustomizer;
import io.quarkus.vertx.http.runtime.CurrentVertxRequest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.validation.MessageInterpolator;
import jakarta.ws.rs.core.HttpHeaders;
import lombok.AllArgsConstructor;
import org.hibernate.validator.BaseHibernateValidatorConfiguration;
import org.hibernate.validator.messageinterpolation.ResourceBundleMessageInterpolator;

import java.util.Locale;

@ApplicationScoped
@AllArgsConstructor
public class HibernateValidatorLocaleCustomizer implements ValidatorFactoryCustomizer {

    private final CurrentVertxRequest currentVertxRequest;

    @Override
    public void customize(BaseHibernateValidatorConfiguration<?> configuration) {
        configuration
                .defaultLocale(MessageBundleCatalog.defaultLocale())
                .localeResolver(context -> this.resolveCurrentRequestLocale())
                .messageInterpolator(new RequestLocaleMessageInterpolator());
    }

    private Locale resolveCurrentRequestLocale() {
        var routingContext = this.currentVertxRequest.getCurrent();
        if (routingContext == null) {
            return MessageBundleCatalog.defaultLocale();
        }

        return HttpLocaleResolver.resolve(routingContext.request().getHeader(HttpHeaders.ACCEPT_LANGUAGE));
    }

    private final class RequestLocaleMessageInterpolator implements MessageInterpolator {

        private final ResourceBundleMessageInterpolator delegate = new ResourceBundleMessageInterpolator(
                MessageBundleCatalog::validationMessages
        );

        @Override
        public String interpolate(String messageTemplate, Context context) {
            return this.delegate.interpolate(messageTemplate, context, HibernateValidatorLocaleCustomizer.this.resolveCurrentRequestLocale());
        }

        @Override
        public String interpolate(String messageTemplate, Context context, Locale locale) {
            return this.delegate.interpolate(messageTemplate, context, MessageBundleCatalog.supportedOrDefault(locale));
        }
    }
}
