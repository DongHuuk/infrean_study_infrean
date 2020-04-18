package com.infrean_study.settings.validator;

import com.infrean_study.account.AccountRepository;
import com.infrean_study.domain.Account;
import com.infrean_study.settings.form.NicknameForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class NicknameValidator implements Validator {
    @Autowired
    private AccountRepository accountRepository;

    @Override
    public boolean supports(Class<?> clazz) {
        return NicknameForm.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors){
        NicknameForm nicknameForm = (NicknameForm) target;
        Account byNickname = accountRepository.findByNickname(nicknameForm.getNickname());// DB에 존재해서는 안됨
        if(byNickname != null){
            errors.rejectValue("nickname", "wrong.value", "이미 사용중입니다.");
        }
    }
}
