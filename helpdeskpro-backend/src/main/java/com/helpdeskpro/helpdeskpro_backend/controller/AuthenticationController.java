package com.helpdeskpro.helpdeskpro_backend.controller;

import org.springframework.web.bind.annotation.RestController;

import com.helpdeskpro.helpdeskpro_backend.dtos.CognitoTokenResponseDto;
import com.helpdeskpro.helpdeskpro_backend.dtos.TokenDto;
import com.helpdeskpro.helpdeskpro_backend.dtos.UrlDto;

import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController 
public class AuthenticationController {
    @Value("${auth.cognito.uri}")
    private String cognitoUri;

    @Value("${spring.security.oauth2.resourceserver.jwt.clientid}")
    private String clientId;

    @Value("${spring.security.oauth2.resourceserver.jwt.clientsecret}")
    private String clientSecret;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @GetMapping("/auth/url")
    public ResponseEntity<UrlDto> url(){
        String url=cognitoUri+
                "/oauth2/autorize?"+
                "response_type=code"+
                "&client_id="+clientId+
                "&redirect_uri=http://localhost:3000/oauth2/idpresponse"+
                "&scope=email+openid+profile";
        return ResponseEntity.ok(new UrlDto(url));
    }

    @GetMapping("/auth/callback")
    public ResponseEntity<TokenDto> callback(@RequestParam("code") String code){
        String urlStr=cognitoUri+"/oauth2/token?"+
                "grant_type=authorization_code"+
                "&client_id="+clientId+
                "&code="+code+
                "&redirect_uri=http://localhost:3000/oauth2/idpresponse";
        String authenticationInfo=clientId+":"+clientSecret;
        String basicAuthentication = Base64.getEncoder().encodeToString(authenticationInfo.getBytes());

        HttpRequest request;
        try{
        request = HttpRequest.newBuilder(new URI(urlStr))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Authorization", "Basic "+basicAuthentication)
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        } catch (URISyntaxException e){
            throw new RuntimeException(e);
        }

        HttpClient client = HttpClient.newHttpClient();

        HttpResponse<String> response;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            throw new RuntimeException(e);
        }

        if(response.statusCode()!=200){
            throw new RuntimeException("Failed to get token from Cognito");
        }

        CognitoTokenResponseDto token;
        token = MAPPER.readValue(response.body(), CognitoTokenResponseDto.class);

        return ResponseEntity.ok(new TokenDto(token.id_token()));
    }
}
