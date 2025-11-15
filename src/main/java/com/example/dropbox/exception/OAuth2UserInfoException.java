package com.example.dropbox.exception;

import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;

public class OAuth2UserInfoException extends OAuth2AuthenticationException {

    public OAuth2UserInfoException(String errorCode, String description) {
        super(new OAuth2Error(errorCode, description, null));
    }

    public OAuth2UserInfoException(String errorCode, String description, Throwable cause) {
        super(new OAuth2Error(errorCode, description, null), cause);
    }
}
