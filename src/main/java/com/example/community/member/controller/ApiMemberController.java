package com.example.community.member.controller;

import com.example.community.member.entity.Member;
import com.example.community.member.model.MemberDto;
import com.example.community.member.service.MemberService;
import com.example.community.member.utils.TokenProvider;
import com.example.community.utils.CookieUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.net.URI;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/member")
public class ApiMemberController {
    private final MemberService memberService;
    private final TokenProvider tokenProvider;
    private final CookieUtil cookieUtil;


    @PostMapping("/register")
    public ResponseEntity<?> register(
            @RequestBody MemberDto.SignUp request
    ){
        Member result = memberService.register(request);
        HttpHeaders headers = new HttpHeaders();

        headers.setLocation(
                URI.create("/member/register-complete")
        );

        return new ResponseEntity<>(headers, HttpStatus.MOVED_PERMANENTLY);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody MemberDto.SignIn request,
            HttpServletResponse response
    ){
        Member member = memberService.authenticate(request);
        String token = tokenProvider.generateToken(
                member.getUsername(), member.getRoles());

        Cookie cookie = cookieUtil.createCookie("X-AUTH-TOKEN"
                , token);

        response.addCookie(cookie);

        return ResponseEntity.ok()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .build();
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            HttpServletResponse response
    ){
        boolean logoutResult = memberService.logout();

        Cookie cookie = cookieUtil.createCookie("X-AUTH-TOKEN", null);
        response.addCookie(cookie);

        return ResponseEntity.ok(logoutResult);
    }
}
