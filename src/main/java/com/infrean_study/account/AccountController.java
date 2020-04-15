package com.infrean_study.account;

import com.infrean_study.domain.Account;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;

@Controller
public class AccountController implements UserDetailsService {

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
        Account account = accountRepository.findByEmail(email);
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
        final Account newAccount = accountRepository.save(account); // 일단은 내가 인의적으로 추가함

        model.addAttribute("numberOfuser", accountRepository.count());
        model.addAttribute("nickname", newAccount.getNickname());
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


    @Override
    public UserDetails loadUserByUsername(String emailOrNickname) throws UsernameNotFoundException {
        Account account = accountRepository.findByEmail(emailOrNickname);
        if(account == null){
            account = accountRepository.findByNickname(emailOrNickname);
        }

        if(account == null){
            throw new UsernameNotFoundException(emailOrNickname);
        }

        return new UserAccount(account); // pricipal로 구현한 객체를 넘겨줌
    }
}
