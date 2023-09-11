package com.example.ssafy301.user.controller;

import com.example.ssafy301.user.oauth2.userinfo.KakaoParams;
import com.example.ssafy301.user.oauth2.userinfo.NaverParams;
import com.example.ssafy301.user.service.OAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class LoginController {
    @Autowired
    private final OAuthService oauthService;

    @GetMapping("/login/kakao")
    public ResponseEntity<String> handleKakaoLogin(@RequestParam("code") String code,
                                                   @RequestParam("state") String state){
        KakaoParams kakaoParams = new KakaoParams();
        kakaoParams.setAuthorizationCode(code);  // setAuthorizationCode 메서드가 없다면 생성해야 합니다.
        kakaoParams.setState(state);              // setState 메서드가 없다면 생성해야 합니다.
        log.debug("넘겨받은 Kakao 인증키 :: " + kakaoParams.getAuthorizationCode());

        String accessToken = oauthService.getMemberByOauthLogin(kakaoParams);
        //응답 헤더 생성
        HttpHeaders headers = new HttpHeaders();
        headers.set("accessToken", accessToken);

        return ResponseEntity.ok().headers(headers).body("Response with header using ResponseEntity");
    }

    @PostMapping("/login/naver")
    public ResponseEntity<String> handleNaverLogin(@RequestBody NaverParams naverParams){
        log.debug("넘겨받은 naver 인증키 :: " + naverParams.getAuthorizationCode());

        String accessToken = oauthService.getMemberByOauthLogin(naverParams);
        //응답 헤더 생성
        HttpHeaders headers = new HttpHeaders();
        headers.set("accessToken", accessToken);

        return ResponseEntity.ok().headers(headers).body("Response with header using ResponseEntity");
    }
}
