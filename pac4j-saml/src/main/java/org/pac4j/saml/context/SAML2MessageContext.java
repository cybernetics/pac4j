/*
  Copyright 2012 -2014 pac4j organization

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package org.pac4j.saml.context;

import org.opensaml.messaging.context.MessageContext;
import org.opensaml.profile.context.ProfileRequestContext;
import org.opensaml.saml.common.SAMLObject;
import org.opensaml.saml.common.messaging.context.SAMLBindingContext;
import org.opensaml.saml.common.messaging.context.SAMLEndpointContext;
import org.opensaml.saml.common.messaging.context.SAMLMetadataContext;
import org.opensaml.saml.common.messaging.context.SAMLPeerEntityContext;
import org.opensaml.saml.common.messaging.context.SAMLProtocolContext;
import org.opensaml.saml.common.messaging.context.SAMLSelfEntityContext;
import org.opensaml.saml.common.messaging.context.SAMLSubjectNameIdentifierContext;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.BaseID;
import org.opensaml.saml.saml2.core.SubjectConfirmation;
import org.opensaml.saml.saml2.metadata.AssertionConsumerService;
import org.opensaml.saml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import org.opensaml.saml.saml2.metadata.SingleSignOnService;
import org.opensaml.xmlsec.context.SecurityParametersContext;
import org.pac4j.saml.exceptions.SAMLException;
import org.pac4j.saml.storage.SAMLMessageStorage;
import org.pac4j.saml.transport.SimpleRequestAdapter;
import org.pac4j.saml.transport.SimpleResponseAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Allow to store additional information for SAML processing.
 * 
 * @author Michael Remond
 * @version 1.5.0
 */
@SuppressWarnings("rawtypes")
public class SAML2MessageContext extends MessageContext<SAMLObject> {

    /* valid subject assertion */
    private Assertion subjectAssertion;

    /* id of the authn request */
    private String requestId;

    /** BaseID retrieved either from the Subject or from a SubjectConfirmation */
    private BaseID baseID;

    /** SubjectConfirmations used during assertion evaluation. */
    private List<SubjectConfirmation> subjectConfirmations = new ArrayList<SubjectConfirmation>();

    private MetadataResolver metadataProvider;

    private SAMLMessageStorage samlMessageStorage;


    public SAML2MessageContext() {
        super();
    }

    public SAML2MessageContext(final MessageContext<SAMLObject> ctx) {
        this();
        super.setParent(ctx);
    }

    public final Assertion getSubjectAssertion() {
        return this.subjectAssertion;
    }

    public final SPSSODescriptor getSPSSODescriptor() {
        final SAMLMetadataContext selfContext = getSAMLSelfMetadataContext();
        final SPSSODescriptor spDescriptor = (SPSSODescriptor) selfContext.getRoleDescriptor();
        return spDescriptor;
    }

    public final IDPSSODescriptor getIDPSSODescriptor() {
        final SAMLMetadataContext peerContext = getSAMLPeerMetadataContext();
        final IDPSSODescriptor idpssoDescriptor = (IDPSSODescriptor) peerContext.getRoleDescriptor();
        return idpssoDescriptor;
    }

    public final SingleSignOnService getIDPSingleSignOnService(final String binding) {
        final List<SingleSignOnService> services = getIDPSSODescriptor().getSingleSignOnServices();
        for (final SingleSignOnService service : services) {
            if (service.getBinding().equals(binding)) {
                return service;
            }
        }
        throw new SAMLException("Identity provider has no single sign on service available for the selected profile"
                + getIDPSSODescriptor());
    }

    public final AssertionConsumerService getSPAssertionConsumerService() {
        return getSPAssertionConsumerService(null);
    }

    public final AssertionConsumerService getSPAssertionConsumerService(final String acsIndex) {
        final SPSSODescriptor spssoDescriptor = getSPSSODescriptor();
        final List<AssertionConsumerService> services = spssoDescriptor.getAssertionConsumerServices();

        // Get by index
        if (acsIndex != null) {
            for (final AssertionConsumerService service : services) {
                if (acsIndex.equals(service.getIndex())) {
                    return service;
                }
            }
            throw new SAMLException("Assertion consumer service with index " + acsIndex
                    + " could not be found for spDescriptor " + spssoDescriptor);
        }

        // Get default
        if (spssoDescriptor.getDefaultAssertionConsumerService() != null) {
            return spssoDescriptor.getDefaultAssertionConsumerService();
        }

        // Get first
        if (services.size() > 0) {
            return services.iterator().next();
        }

        throw new SAMLException("No assertion consumer services could be found for " + spssoDescriptor);
    }

    public final ProfileRequestContext getProfileRequestContext() {
        return this.getSubcontext(ProfileRequestContext.class, true);
    }

    public final SAMLSelfEntityContext getSAMLSelfEntityContext() {
        return this.getSubcontext(SAMLSelfEntityContext.class, true);
    }

    public final SAMLMetadataContext getSAMLSelfMetadataContext() {
        return getSAMLSelfEntityContext().getSubcontext(SAMLMetadataContext.class, true);
    }

    public final SAMLMetadataContext getSAMLPeerMetadataContext() {
        return getSAMLPeerEntityContext().getSubcontext(SAMLMetadataContext.class, true);
    }

    public SAMLMetadataContext getSAMLMetadataContext() {
        return this.getSubcontext(SAMLMetadataContext.class, true);
    }

    public final SAMLPeerEntityContext getSAMLPeerEntityContext() {
        return this.getSubcontext(SAMLPeerEntityContext.class, true);
    }

    public final SAMLSubjectNameIdentifierContext getSAMLSubjectNameIdentifierContext() {
        return this.getSubcontext(SAMLSubjectNameIdentifierContext.class, true);
    }

    public final void setSubjectAssertion(final Assertion subjectAssertion) {
        this.subjectAssertion = subjectAssertion;
    }

    public String getRequestId() {
        return this.requestId;
    }

    public void setRequestId(final String requestId) {
        this.requestId = requestId;
    }

    public final BaseID getBaseID() {
        return baseID;
    }
    
    public final void setBaseID(final BaseID baseID) {
        this.baseID = baseID;
    }

    public final List<SubjectConfirmation> getSubjectConfirmations() {
        return subjectConfirmations;
    }
    
    public void setSubjectConfirmations(final List<SubjectConfirmation> subjectConfirmations) {
        this.subjectConfirmations = subjectConfirmations;
    }

    public final void setMetadataProvider(final MetadataResolver metadataProvider) {
        this.metadataProvider = metadataProvider;
    }

    public final SAMLEndpointContext getSAMLPeerEndpointContext() {
        return getSAMLPeerEntityContext().getSubcontext(SAMLEndpointContext.class, true);
    }


    public final SAMLEndpointContext getSAMLSelfEndpointContext() {
        return getSAMLSelfEntityContext().getSubcontext(SAMLEndpointContext.class, true);
    }

    public final SAMLBindingContext getSAMLBindingContext() {
        return this.getSubcontext(SAMLBindingContext.class, true);
    }

    public final SecurityParametersContext getSecurityParametersContext() {
        return this.getSubcontext(SecurityParametersContext.class, true);
    }

    public final SAMLProtocolContext getSAMLSelfProtocolContext() {
        return this.getSAMLSelfEntityContext().getSubcontext(SAMLProtocolContext.class, true);
    }

    public final SAMLProtocolContext getSAMLProtocolContext() {
        return this.getSubcontext(SAMLProtocolContext.class, true);
    }

    public final SimpleResponseAdapter getProfileRequestContextOutboundMessageTransportResponse() {
        return (SimpleResponseAdapter) getProfileRequestContext().getOutboundMessageContext().getMessage();
    }

    public final SimpleRequestAdapter getProfileRequestContextInboundMessageTransportRequest() {
        return (SimpleRequestAdapter) getProfileRequestContext().getInboundMessageContext().getMessage();
    }

    public final SAMLEndpointContext getSAMLEndpointContext() {
        return this.getSubcontext(SAMLEndpointContext.class, true);
    }

    public final SAMLMessageStorage getSAMLMessageStorage() {
        return this.samlMessageStorage;
    }

    public final void setSAMLMessageStorage(final SAMLMessageStorage samlMessageStorage) {
        this.samlMessageStorage = samlMessageStorage;
    }
}
