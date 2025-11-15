package com.example.dropbox.serivce;

import com.example.dropbox.dto.TeamInfoDTO;
import com.example.dropbox.service.impl.DropboxAPIServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DropboxAPIServiceImpl Unit Tests")
class DropboxAPIServiceImplTest {

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private WebClient.RequestBodySpec requestBodySpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    private DropboxAPIServiceImpl dropboxAPIService;

    private static final String BASE_URL = "https://api.dropboxapi.com";
    private static final String ENDPOINT = "/2/team/get_info";
    private static final String FULL_URI = BASE_URL + ENDPOINT;

    @BeforeEach
    void setUp() {
        dropboxAPIService = new DropboxAPIServiceImpl(webClient);
        ReflectionTestUtils.setField(dropboxAPIService, "externalApiBaseUrl", BASE_URL);
        ReflectionTestUtils.setField(dropboxAPIService, "externalApiEndpoint", ENDPOINT);
    }

    @Test
    @DisplayName("Should successfully fetch team info with valid response")
    void testGetTeamInfo_Success() {
        TeamInfoDTO expectedTeamInfo = createTestTeamInfo();

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(FULL_URI)).thenReturn(requestBodySpec);
        when(requestBodySpec.attributes(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.header(eq("Content-Type"), eq("application/json"))).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue("null")).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(TeamInfoDTO.class)).thenReturn(Mono.just(expectedTeamInfo));

        TeamInfoDTO result = dropboxAPIService.getTeamInfo();

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Test Team");
        assertThat(result.getTeamId()).isEqualTo("team123");
        assertThat(result.getNumLicensedUsers()).isEqualTo(100);
        assertThat(result.getNumProvisionedUsers()).isEqualTo(80);
        assertThat(result.getNumUsedLicenses()).isEqualTo(75);

        verify(webClient).post();
        verify(requestBodyUriSpec).uri(FULL_URI);
        verify(requestBodySpec).attributes(any());
        verify(requestBodySpec).header("Content-Type", "application/json");
        verify(requestBodySpec).bodyValue("null");
        verify(requestHeadersSpec).retrieve();
        verify(responseSpec).bodyToMono(TeamInfoDTO.class);
    }

    @Test
    @DisplayName("Should throw RuntimeException when WebClient throws 4xx error")
    void testGetTeamInfo_ClientError() {
        WebClientResponseException clientException = WebClientResponseException.create(
                401,
                "Unauthorized",
                null,
                null,
                null
        );

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(FULL_URI)).thenReturn(requestBodySpec);
        when(requestBodySpec.attributes(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.header(eq("Content-Type"), eq("application/json"))).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue("null")).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(TeamInfoDTO.class)).thenReturn(Mono.error(clientException));

        assertThatThrownBy(() -> dropboxAPIService.getTeamInfo())
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to fetch team info")
                .hasCauseInstanceOf(WebClientResponseException.class);

        verify(webClient).post();
    }

    @Test
    @DisplayName("Should throw RuntimeException when WebClient throws 5xx error")
    void testGetTeamInfo_ServerError() {
        WebClientResponseException serverException = WebClientResponseException.create(
                503,
                "Service Unavailable",
                null,
                null,
                null
        );

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(FULL_URI)).thenReturn(requestBodySpec);
        when(requestBodySpec.attributes(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.header(eq("Content-Type"), eq("application/json"))).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue("null")).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(TeamInfoDTO.class)).thenReturn(Mono.error(serverException));

        assertThatThrownBy(() -> dropboxAPIService.getTeamInfo())
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to fetch team info");

        verify(webClient).post();
    }

    @Test
    @DisplayName("Should throw RuntimeException when network error occurs")
    void testGetTeamInfo_NetworkError() {
        RuntimeException networkException = new RuntimeException("Network connection failed");

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(FULL_URI)).thenReturn(requestBodySpec);
        when(requestBodySpec.attributes(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.header(eq("Content-Type"), eq("application/json"))).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue("null")).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(TeamInfoDTO.class)).thenReturn(Mono.error(networkException));

        assertThatThrownBy(() -> dropboxAPIService.getTeamInfo())
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to fetch team info")
                .hasMessageContaining("Network connection failed");
    }

    @Test
    @DisplayName("Should throw RuntimeException when timeout occurs")
    void testGetTeamInfo_TimeoutError() {
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(FULL_URI)).thenReturn(requestBodySpec);
        when(requestBodySpec.attributes(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.header(eq("Content-Type"), eq("application/json"))).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue("null")).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(TeamInfoDTO.class))
                .thenReturn(Mono.error(new RuntimeException("Request timeout")));

        assertThatThrownBy(() -> dropboxAPIService.getTeamInfo())
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to fetch team info");
    }

    @Test
    @DisplayName("Should use correct URI with base URL and endpoint")
    void testGetTeamInfo_CorrectUriConstruction() {
        TeamInfoDTO expectedTeamInfo = createTestTeamInfo();

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(FULL_URI)).thenReturn(requestBodySpec);
        when(requestBodySpec.attributes(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.header(eq("Content-Type"), eq("application/json"))).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue("null")).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(TeamInfoDTO.class)).thenReturn(Mono.just(expectedTeamInfo));

        dropboxAPIService.getTeamInfo();

        verify(requestBodyUriSpec).uri(FULL_URI);
    }

    private TeamInfoDTO createTestTeamInfo() {
        TeamInfoDTO teamInfo = new TeamInfoDTO();
        teamInfo.setName("Test Team");
        teamInfo.setTeamId("team123");
        teamInfo.setNumLicensedUsers(100);
        teamInfo.setNumProvisionedUsers(80);
        teamInfo.setNumUsedLicenses(75);
        return teamInfo;
    }
}
