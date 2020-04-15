package com.infrean_study.account;

import com.infrean_study.domain.Account;
import org.springframework.beans.factory.annotation.Autowired;
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
    private AccountService accountService;
    @Autowired
    private AccountRepository accountRepository;

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

        final Account newAccount = accountService.processNewAccount(signUpForm);
        accountService.login(newAccount);
        return "redirect:/";
    }

    @GetMapping("/check-email-token")
    public String checkEmailToken(String token, String email, Model model){
        final Account account = accountRepository.findByEmail(email);
        final String view = "account/checked-Email";
        if(account == null){
            model.addAttribute("error", "wrong.email");
            return view;
        }

        if(!account.isValidToken(token)){
            model.addAttribute("error", "wrong.token");
            return view;
        }

        account.completeSignUp();
        accountService.login(account);
        model.addAttribute("numberOfuser", accountRepository.count());
        model.addAttribute("nickname", account.getNickname());
        return view;
    }

    @PostMapping("/recheck-email")
    public String reCheckEmailToken(@CurrentUser Account account, Model model){
        if(account == null){
            return "index";
        }

        if(!account.canResendMailTimeCheck()){
            model.addAttribute("error", "인증 메일은 1시간에 한번만 보낼 수 있습니다.");
            model.addAttribute("email", account.getEmail());
            return "account/reckecked-email";
        }

        accountService.sendSignUpConfirmEmail(account);
        return "index";
    }

    @GetMapping("/recheck-email")
    public String reCheckEmailToken_btn(@CurrentUser Account account, Model model){
        if(account != null) {
            model.addAttribute(account);
            return "account/reckecked-email";
        }

        //TODO login 화면으로 이동시켜주기
        return "index";
    }


}
