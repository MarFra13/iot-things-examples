/*
 * Bosch SI Example Code License Version 1.0, January 2016
 *
 * Copyright 2017 Bosch Software Innovations GmbH ("Bosch SI"). All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 * following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 * BOSCH SI PROVIDES THE PROGRAM "AS IS" WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR IMPLIED, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE ENTIRE RISK AS TO THE
 * QUALITY AND PERFORMANCE OF THE PROGRAM IS WITH YOU. SHOULD THE PROGRAM PROVE DEFECTIVE, YOU ASSUME THE COST OF ALL
 * NECESSARY SERVICING, REPAIR OR CORRECTION. THIS SHALL NOT APPLY TO MATERIAL DEFECTS AND DEFECTS OF TITLE WHICH BOSCH
 * SI HAS FRAUDULENTLY CONCEALED. APART FROM THE CASES STIPULATED ABOVE, BOSCH SI SHALL BE LIABLE WITHOUT LIMITATION FOR
 * INTENT OR GROSS NEGLIGENCE, FOR INJURIES TO LIFE, BODY OR HEALTH AND ACCORDING TO THE PROVISIONS OF THE GERMAN
 * PRODUCT LIABILITY ACT (PRODUKTHAFTUNGSGESETZ). THE SCOPE OF A GUARANTEE GRANTED BY BOSCH SI SHALL REMAIN UNAFFECTED
 * BY LIMITATIONS OF LIABILITY. IN ALL OTHER CASES, LIABILITY OF BOSCH SI IS EXCLUDED. THESE LIMITATIONS OF LIABILITY
 * ALSO APPLY IN REGARD TO THE FAULT OF VICARIOUS AGENTS OF BOSCH SI AND THE PERSONAL LIABILITY OF BOSCH SI'S EMPLOYEES,
 * REPRESENTATIVES AND ORGANS.
 */
package com.bosch.cr.examples.jwt.auth;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;

import com.bosch.cr.examples.jwt.ConfigurationProperties;
import com.bosch.cr.examples.jwt.ConfigurationProperty;

/**
 * Servlet to start the google oauth flow. Redirects requests to /oauth2start/google to google.
 */
@WebServlet("/oauth2start/google")
public class GoogleStartServlet extends HttpServlet {

    private static final long serialVersionUID = 940262123430910542L;

    /**
     * The base URL for oauth authorization requests.
     */
    private static final String GOOGLE_OAUTH2_AUTH_URL = "https://accounts.google.com/o/oauth2/v2/auth";

    /**
     * The scope to include in JSON Web Tokens.
     */
    private static final String OPENID_SCOPE = "openid";

    private String authorizationUri;

    @Override
    public void init() throws ServletException {
        super.init();

        final ConfigurationProperties config = ConfigurationProperties.getInstance();
        final String clientId = config.getPropertyAsString(ConfigurationProperty.GOOGLE_CLIENT_ID);
        final String redirectUrl = config.getPropertyAsString(ConfigurationProperty.GOOGLE_CLIENT_REDIRECT_URL);

        authorizationUri = buildAuthorizationUri(clientId, redirectUrl);
    }

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp)
            throws ServletException, IOException {
        resp.sendRedirect(authorizationUri);
    }

    /**
     * Builds the URI for oauth authorization requests for the given {@code clientId} and {@code redirectUrl}.
     *
     * @return the URI.
     */
    private String buildAuthorizationUri(final String clientId, final String redirectUrl) {
        try {
            final OAuthClientRequest authorizationRequest =
                    OAuthClientRequest.authorizationLocation(GOOGLE_OAUTH2_AUTH_URL) //
                            .setClientId(clientId) //
                            .setResponseType(OAuth.OAUTH_CODE) //
                            .setRedirectURI(redirectUrl) //
                            .setScope(OPENID_SCOPE) //
                            .buildQueryMessage();

            return authorizationRequest.getLocationUri();
        } catch (final OAuthSystemException e) {
            throw new IllegalStateException("Building authorization URI failed", e);
        }
    }
}
