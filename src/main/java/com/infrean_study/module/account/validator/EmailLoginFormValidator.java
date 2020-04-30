package com.infrean_study.module.account;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class EmailLoginFormValidator implements Validator {

    @Autowired
    private AccountRepository accountRepository;

    @Override
    public boolean supports(Class<?> clazz) {
        return EmailLoginForm.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object object, Errors errors) {
        EmailLoginForm loginForm = (EmailLoginForm) object;
        Account byNickname = accountRepository.findByEmail(loginForm.getEmail());
        if(byNickname == null){
            errors.rejectValue("error", "wrong.email", "이메일이 존재하지 않습니다.");
        }
    }
}
