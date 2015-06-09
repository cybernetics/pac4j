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
package org.pac4j.saml.sso;

import org.opensaml.saml.common.SAMLObject;
import org.pac4j.saml.context.SAML2MessageContext;
import org.pac4j.core.credentials.Credentials;

/**
 * Handles a SAML protocol profile.
 * @author Misagh Moayyed
 * @since 1.7
 */
public interface SAML2ProfileHandler<T extends SAMLObject> {
    void send(SAML2MessageContext context, T msg, Object state);

    Credentials receive(SAML2MessageContext context);
}
