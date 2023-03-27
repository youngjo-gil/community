package com.example.community.member.service.impl;

import com.example.community.CommunityApplication;
import com.example.community.member.entity.Member;
import com.example.community.member.entity.MemberRoleCode;
import com.example.community.member.exception.MemberExistException;
import com.example.community.member.exception.MemberNotExistException;
import com.example.community.member.exception.NotCorrectPassword;
import com.example.community.member.model.MemberDto;
import com.example.community.member.repository.MemberRepository;
import com.example.community.member.service.MemberService;
import com.example.community.utils.CookieUtil;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class MemberServiceImpl implements MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;

    private final Logger logger = LoggerFactory.getLogger(CommunityApplication.class);

    @Override
    public Member register(MemberDto.SignUp parameter) {
        logger.info("회원가입 시작");

        boolean exists = memberRepository.existsByUsername(parameter.getUsername());

        if(exists){
            throw new MemberExistException();
        }

        List<String> role = new ArrayList<>();
        role.add(MemberRoleCode.ROLE_USER.name());

        parameter.setPassword(passwordEncoder.encode(parameter.getPassword()));
        parameter.setRoles(role);

        Member result = memberRepository.save(MemberDto.SignUp.of(parameter));

        logger.info("회원가입 종료");
        return result;
    }

    @Override
    public Member authenticate(MemberDto.SignIn member) {
        Member user = memberRepository.findByUsername(member.getUsername())
                .orElseThrow(() -> new MemberNotExistException());

        if(!passwordEncoder.matches(member.getPassword(), user.getPassword())){
            throw new NotCorrectPassword();
        }

        UsernamePasswordAuthenticationToken authenticationToken
                = new UsernamePasswordAuthenticationToken(member.getUsername(), member.getPassword());


        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);



        return user;
    }

    @Override
    public boolean logout() {
        return true;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException("couldn't find user -> " + username));
        return member;
    }


}