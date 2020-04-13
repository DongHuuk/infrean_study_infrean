package com.infrean_study.account;

import com.infrean_study.domain.Account;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;

@Controller
public class AccountController {

    @Autowired
    private SignUpFormValidator validator;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private JavaMailSender javaMailSender;

    @InitBinder("signUpForm")
    private void initBinder(WebDataBinder webDataBinder){
        webDataBinder.addValidators(validator);
    }

    @GetMapping("/sign-up")
    public String signUp(Model model){
        model.addAttribute(new SignUpForm());
        return "account/sign-up";
    }

    @PostMapping("/sign-up")
    public String sighUpSubmit(@Valid SignUpForm signUpForm, Errors errors){
        if(errors.hasErrors()){
            return "account/sign-up";
        }

        Account account = Account.builder()
                .email(signUpForm.getEmail())
                .nickname(signUpForm.getNickname())
                .password(signUpForm.getPassword()) // TODO encoding 해야함
                .studyEnrollmentResultByEmail(true)
                .studyCreatedByWeb(true)
                .studyUpdateByWeb(true)
                .build();
        final Account newAccount = accountRepository.save(account);

        newAccount.generateEmailCheckToken(); // token 생성
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(newAccount.getEmail());
        mailMessage.setSubject("회원가입 인증 메일"); // 제목
        mailMessage.setText("/check-email-token?token=" + newAccount.getEmailCheckToken() + "&email="
                + newAccount.getEmail()); //본문 토큰 인증
        javaMailSender.send(mailMessage);

        //TODO 가입 처리
        return "redirect:/";

    }

}
