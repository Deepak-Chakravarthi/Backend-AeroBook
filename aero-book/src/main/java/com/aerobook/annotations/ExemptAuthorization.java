package com.aerobook.annotations;


import java.lang.annotation.*;

/**
 * Marks a controller method as exempt from authorization checks.
 * Apply to CUD methods that should be publicly accessible
 * without requiring authentication or @PreAuthorize.
 *
 * Use sparingly — only for genuinely public mutation endpoints
 * e.g. /auth/register, /auth/forgot-password, /webhooks/payment-callback
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ExemptAuthorization {

    /**
     * Optional reason — documents WHY this endpoint is exempt.
     * Enforces intentional usage.
     */
    String reason() default "";
}