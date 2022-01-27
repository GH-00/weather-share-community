package com.springproject.weathersharecommunity.service;

import com.springproject.weathersharecommunity.Controller.dto.MemberSaveRequestDto;
import com.springproject.weathersharecommunity.domain.ConfirmToken;
import com.springproject.weathersharecommunity.domain.Member;
import com.springproject.weathersharecommunity.repository.MemberRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.firewall.RequestRejectedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@RequiredArgsConstructor
@Slf4j
@Service
@Getter
public class MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final ConfirmTokenService confirmTokenService;
    @Transactional
    public Long save(MemberSaveRequestDto requestDto){
//        duplicationMember(requestDto);
        requestDto.setPwd(passwordEncoder.encode(requestDto.getPwd()));
        Member member = memberRepository.save(requestDto.toEntity());
        confirmTokenService.createEmailConfirmToken(String.valueOf(member.getId()), member.getUserEmail());

        return member.getId();
    }
    @Transactional
    public void confirmEmail(String token) {
        ConfirmToken findConfirmToken = confirmTokenService.findByIdAndExpired(token);
        Member member = memberRepository.findById(Long.valueOf(findConfirmToken.getUserId()))
                .orElseThrow(()->new IllegalArgumentException("없는 멤버입니다."));
        findConfirmToken.useToken();
        member.setEmailAuth(true);

    }


//    public void duplicationMember(MemberSaveRequestDto requestDto){
//        Optional<Member> checkMember = memberRepository.findByEmail(requestDto.getEmail());
//        if (checkMember.isPresent()) {
//            throw new IllegalStateException("이미 존재하는 회원입니다.");
//        }
//    }
}
