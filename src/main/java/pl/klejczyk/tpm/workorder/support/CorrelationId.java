package pl.klejczyk.tpm.workorder.support;

import org.slf4j.MDC;

public final class CorrelationId {

    public static final String MDC_KEY = "correlationId";

    private CorrelationId() {
    }

    public static String current() {
        return MDC.get(MDC_KEY);
    }

    public static void set(String value) {
        MDC.put(MDC_KEY, value);
    }

    public static void clear() {
        MDC.remove(MDC_KEY);
    }
}
