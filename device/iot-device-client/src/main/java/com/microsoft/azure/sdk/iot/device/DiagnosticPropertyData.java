package com.microsoft.azure.sdk.iot.device;

/**
 * Data structure to store diagnostic property data
 */
public class DiagnosticPropertyData
{
    /** Key of creation time in diagnostic context. */
    private final static String DIAGNOSTIC_CONTEXT_CREATION_TIME_UTC_PROPERTY = "creationtimeutc";
    /** Equal symbol in diagnostic context. */
    private final static String DIAGNOSTIC_CONTEXT_SYMBOL_EQUAL = "=";
    /** And symbol in diagnostic context. */
    private final static String DIAGNOSTIC_CONTEXT_SYMBOL_AND = "&";

    private String diagnosticId;
    private String diagnosticCreationTimeUtc;

    public DiagnosticPropertyData(String diagnosticId, String diagnosticCreationTimeUtc) {
        /* Codes_SRS_DIAGNOSTICPROPERTYDATA_01_002: [If the diagnosticId or diagnosticCreationTimeUtc is null, the constructor shall throw an IllegalArgumentException.] */
        if (diagnosticId == null || diagnosticId.isEmpty() || diagnosticCreationTimeUtc == null || diagnosticCreationTimeUtc.isEmpty()) {
            throw new IllegalArgumentException("The diagnosticId or diagnosticCreationTimeUtc cannot be null or empty.");
        }

        /* Codes_SRS_DIAGNOSTICPROPERTYDATA_01_001: [The constructor shall save the message body.] */
        this.diagnosticId = diagnosticId;
        this.diagnosticCreationTimeUtc = diagnosticCreationTimeUtc;
    }

    public String getDiagnosticId() {
        return diagnosticId;
    }

    public void setDiagnosticId(String diagnosticId) {
        /* Codes_SRS_DIAGNOSTICPROPERTYDATA_02_001: [A valid diagnosticId shall not be null or empty.] */
        if (diagnosticId == null || diagnosticId.isEmpty()) {
            throw new IllegalArgumentException("The diagnosticId cannot be null or empty.");
        }
        this.diagnosticId = diagnosticId;
    }

    public String getDiagnosticCreationTimeUtc() {
        return diagnosticCreationTimeUtc;
    }

    public void setDiagnosticCreationTimeUtc(String diagnosticCreationTimeUtc) {
        /* Codes_SRS_DIAGNOSTICPROPERTYDATA_02_002: [A valid diagnosticCreationTimeUtc shall not be null or empty.] */
        if (diagnosticCreationTimeUtc == null || diagnosticCreationTimeUtc.isEmpty()) {
            throw new IllegalArgumentException("The diagnosticCreationTimeUtc cannot be null or empty.");
        }
        this.diagnosticCreationTimeUtc = diagnosticCreationTimeUtc;
    }

    /* Codes_SRS_DIAGNOSTICPROPERTYDATA_01_007: [The function shall return concat string of all correlation contexts.] */
    public String getCorrelationContext() {
        String correlationContext = "";
        correlationContext += (DIAGNOSTIC_CONTEXT_CREATION_TIME_UTC_PROPERTY + DIAGNOSTIC_CONTEXT_SYMBOL_EQUAL + diagnosticCreationTimeUtc);
        return correlationContext;
    }
}
