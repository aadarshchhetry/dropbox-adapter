package com.example.dropbox.service.impl;

import com.example.dropbox.exception.OAuth2UserInfoException;
import com.example.dropbox.utils.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Component
@Slf4j
public class DropboxOAuth2UserService implements  OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private static final String ERROR_CODE_INVALID_RESPONSE = "invalid_user_info_response";
    private static final String LOG_PREFIX = "[DropboxOAuth2]";

    private final RestTemplate restTemplate;

    public DropboxOAuth2UserService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        log.info("{} Initiating user info request", LOG_PREFIX);

        try {
            String userInfoUri = extractUserInfoUri(userRequest);
            String accessToken = extractAccessToken(userRequest);

            ResponseEntity<Map> response = fetchUserInfo(userInfoUri, accessToken);
            Map<String, Object> responseBody = validateResponse(response);
            Map<String, Object> userAttributes = processResponseBody(responseBody);

            logSuccessfulAuthentication(userAttributes);

            return createOAuth2User(userAttributes);

        } catch (RestClientException ex) {
            log.error("{} REST client error during user info retrieval", LOG_PREFIX, ex);
            throw new OAuth2UserInfoException(
                    ERROR_CODE_INVALID_RESPONSE,
                    "Failed to retrieve user info: " + ex.getMessage(),
                    ex
            );
        } catch (OAuth2UserInfoException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("{} Unexpected error during user info retrieval", LOG_PREFIX, ex);
            throw new OAuth2UserInfoException(
                    ERROR_CODE_INVALID_RESPONSE,
                    "Unexpected error: " + ex.getMessage(),
                    ex
            );
        }
    }

    private String extractUserInfoUri(OAuth2UserRequest userRequest) {
        String uri = userRequest.getClientRegistration()
                .getProviderDetails()
                .getUserInfoEndpoint()
                .getUri();
        log.debug("{} User info URI: {}", LOG_PREFIX, uri);
        return uri;
    }

    private String extractAccessToken(OAuth2UserRequest userRequest) {
        return userRequest.getAccessToken().getTokenValue();
    }

    private ResponseEntity<Map> fetchUserInfo(String userInfoUri, String accessToken) {
        log.debug("{} Sending POST request to Dropbox team admin endpoint", LOG_PREFIX);

        HttpHeaders headers = createHeaders(accessToken);
        HttpEntity<String> requestEntity = new HttpEntity<>(Constants.REQUEST_BODY_NULL, headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                userInfoUri,
                HttpMethod.POST,
                requestEntity,
                Map.class
        );

        log.debug("{} Response status: {}", LOG_PREFIX, response.getStatusCode());
        return response;
    }

    private HttpHeaders createHeaders(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.set(HttpHeaders.CONTENT_TYPE, Constants.CONTENT_TYPE_JSON);
        return headers;
    }

    private Map<String, Object> validateResponse(ResponseEntity<Map> response) {
        Map<String, Object> body = response.getBody();

        if (body == null || body.isEmpty()) {
            log.error("{} Received empty response from user info endpoint", LOG_PREFIX);
            throw new OAuth2UserInfoException(
                    ERROR_CODE_INVALID_RESPONSE,
                    "Empty response from user info endpoint"
            );
        }

        return body;
    }

    private Map<String, Object> processResponseBody(Map<String, Object> responseBody) {
        Map<String, Object> userAttributes = new HashMap<>(responseBody);

        if (responseBody.containsKey(Constants.ADMIN_PROFILE_KEY)) {
            enrichUserAttributesWithAdminProfile(userAttributes, responseBody);
        }

        return userAttributes;
    }

    @SuppressWarnings("unchecked")
    private void enrichUserAttributesWithAdminProfile(
            Map<String, Object> userAttributes,
            Map<String, Object> responseBody) {

        Map<String, Object> adminProfile = (Map<String, Object>) responseBody.get(Constants.ADMIN_PROFILE_KEY);

        userAttributes.put(Constants.TEAM_MEMBER_ID_ATTR, adminProfile.get("team_member_id"));
        userAttributes.put(Constants.EMAIL_ATTR, adminProfile.get("email"));

        if (adminProfile.containsKey(Constants.NAME_KEY)) {
            Map<String, Object> name = (Map<String, Object>) adminProfile.get(Constants.NAME_KEY);
            userAttributes.put(Constants.DISPLAY_NAME_ATTR, name.get(Constants.DISPLAY_NAME_KEY));
        }
    }

    private void logSuccessfulAuthentication(Map<String, Object> userAttributes) {
        String teamMemberId = Optional.ofNullable(userAttributes.get(Constants.TEAM_MEMBER_ID_ATTR))
                .map(Object::toString)
                .orElse("unknown");
        log.info("{} User authenticated successfully: {}", LOG_PREFIX, teamMemberId);
    }

    private OAuth2User createOAuth2User(Map<String, Object> userAttributes) {
        Set<GrantedAuthority> authorities = Collections.singleton(
                new SimpleGrantedAuthority(Constants.ROLE_ADMIN)
        );

        return new DefaultOAuth2User(
                authorities,
                userAttributes,
                Constants.TEAM_MEMBER_ID_ATTR
        );
    }
}
