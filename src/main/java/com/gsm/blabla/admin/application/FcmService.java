package com.gsm.blabla.admin.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.gson.JsonParseException;
import com.gsm.blabla.admin.dto.PushMessageRequestDto;
import jakarta.transaction.Transactional;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.http.HttpHeaders;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
@Component
public class FcmService {

    private final String API_URL = "https://fcm.googleapis.com/v1/projects/blabla-391200/messages:send";
    private final ObjectMapper objectMapper;

    public Map<String, String> sendMessageTo(PushMessageRequestDto pushMessageRequestDto) throws IOException {
        String targetToken = pushMessageRequestDto.getMessage().getToken();
        String title = pushMessageRequestDto.getMessage().getNotification().getTitle();
        String body = pushMessageRequestDto.getMessage().getNotification().getBody();

        String message = makeMessage(targetToken, title, body);

        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = RequestBody.create(message, MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder()
            .url(API_URL)
            .post(requestBody)
            .addHeader("Authorization", "Bearer " + getAccessToken())
            .addHeader(HttpHeaders.CONTENT_TYPE, "application/json; UTF-8")
            .build();
        client.newCall(request).execute();

        return Collections.singletonMap("message", "알림을 성공적으로 전송했습니다.");
    }

    private String makeMessage(String targetToken, String title, String body) throws JsonParseException, JsonProcessingException {
        PushMessageRequestDto pushMessageRequestDto = PushMessageRequestDto.builder()
            .validateOnly(false)
            .message(PushMessageRequestDto.Message.builder()
                .token(targetToken)
                .notification(PushMessageRequestDto.Notification.builder()
                    .title(title)
                    .body(body)
                    .build()
                )
                .build()
            )
            .build();
        return objectMapper.writeValueAsString(pushMessageRequestDto);
    }

    private String getAccessToken() throws IOException {
        String firebaseConfigPath = "firebase-service-key.json";

        GoogleCredentials googleCredentials = GoogleCredentials
            .fromStream(new ClassPathResource(firebaseConfigPath).getInputStream())
            .createScoped(List.of("https://www.googleapis.com/auth/cloud-platform"));

        googleCredentials.refreshIfExpired();

        return googleCredentials.getAccessToken().getTokenValue();
    }
}
