package com.microsoft.azure.sdk.iot.device;

import java.util.HashSet;
import java.util.Set;

public class Template
{
    public Object templateTestPublic;
    private Object templateTestPrivate;
    private Set unionSet;

    /**
     * Constructor which creates an instance of this class if valid tag is provided
     * @param tag Object to be saved. Cannot be {@code null}
     * @throws IllegalArgumentException if the input parameter is {@code null}
     */
    Template(Object tag) throws IllegalArgumentException
    {
        if (tag == null)
        {
            // Codes_SRS_TEMPLATE_99_002: [If the input parameter is null, the constructor shall throw an IllegalArgumentException.]
            throw new IllegalArgumentException("Tag cannot be null");
        }

        // Codes_SRS_TEMPLATE_99_001: [The constructor shall save the input parameters.]
        this.templateTestPrivate = tag;

        // Codes_SRS_TEMPLATE_99_003: [The constructor shall create a new instance of the public and private objects.]
        this.templateTestPublic = new Object();
    }

    /**
     * Open on an instance of this class. If already
     * opened, this method shall do nothing.
     */
    void open()
    {
        // Codes_SRS_TEMPLATE_99_005: [If open is already called then this method shall do nothing and return.]
        if (this.unionSet == null)
        {
            // Codes_SRS_TEMPLATE_99_004: [The method shall create a new instance of the unionSet .]
            this.unionSet = new HashSet();
        }
    }

    /**
     * Close the instance of this class. If already
     * closed or never opened, this method shall do nothing.
     */
    void close()
    {
        if (this.unionSet == null)
        {
            // Codes_SRS_TEMPLATE_99_007: [If close is already called then this method shall do nothing and return.]
            return;
        }

        // Codes_SRS_TEMPLATE_99_006: [This method shall clear the unionSet and set all the members ready for garbage collection.]
        this.unionSet.clear();
        this.unionSet = null;
        this.templateTestPublic = null;
        this.templateTestPrivate = null;
    }

    /**
     * Getter for the private object
     * @return the private object.
     */
    public Object getTemplateTestPrivate()
    {
        // Codes_SRS_TEMPLATE_99_008: [The method shall return the private member object.]
        return templateTestPrivate;
    }

    /**
     * Getter for the state of set at the invoked instance
     * @return the set with (or without) union invoked so far.
     */
    public Set<?> getUnionSet()
    {
        // Codes_SRS_TEMPLATE_99_009: [The method shall return the current instance of the union set.]
        return unionSet;
    }

    /**
     * Adds the given collection to the existing set
     * @param collection A collection which cannot be {@code null} or empty
     *                   to be added to existing collection
     * @throws IllegalArgumentException if the given collection is {@code null}
     *                                  or empty
     */
    public void addToSet(Set<?> collection) throws IllegalArgumentException
    {
        if(collection == null || collection.size() == 0)
        {
            // Codes_SRS_TEMPLATE_99_011: [The method shall throw IllegalArgumentException if the collection to be added was either empty or null .]
            throw new IllegalArgumentException("New set cannot be null or empty");
        }

        if (this.unionSet == null)
        {
            // Codes_SRS_TEMPLATE_99_012: [The method shall throw IllegalStateException if it is called before calling open. ]
            throw new IllegalStateException("Please open before use");
        }

        // Codes_SRS_TEMPLATE_99_010: [The method shall add the collection to the union set .]
        this.unionSet.addAll(collection);
    }

}
