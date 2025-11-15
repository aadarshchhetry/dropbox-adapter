package com.example.dropbox.service.impl;

import com.example.dropbox.dto.TeamInfoDTO;
import com.example.dropbox.service.DropboxAPIService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@Slf4j
@RequiredArgsConstructor
public class DropboxAPIServiceImpl implements DropboxAPIService {
    @Autowired
    private final WebClient webClient;

    @Value("${api.external.base-url}")
    private String externalApiBaseUrl;

    @Value("${api.external.endpoint}")
    private String externalApiEndpoint;

    @Override
    public TeamInfoDTO getTeamInfo() {
        String fullUri = externalApiBaseUrl + externalApiEndpoint;

        log.info("Fetching team info from: {}", fullUri);

        try {
            TeamInfoDTO teamInfo = webClient
                    .post()
                    .uri(fullUri)
                    .attributes(ServletOAuth2AuthorizedClientExchangeFilterFunction
                            .clientRegistrationId("dbx"))
                    .header("Content-Type", "application/json")
                    .bodyValue("null")
                    .retrieve()
                    .bodyToMono(TeamInfoDTO.class)
                    .block();

            log.info("Successfully fetched team info: {}", teamInfo.getName());
            return teamInfo;

        } catch (Exception exception) {
            log.error("Error fetching team info", exception);
            throw new RuntimeException("Failed to fetch team info: " + exception.getMessage(), exception);
        }
    }
}
