package com.infrean_study.account;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;

@Controller
public class AccountController  {

    public static final String EMAIL_LOGIN = "account/email-login";

    @Autowired
    private SignUpFormValidator validator;
    @Autowired
    private AccountService accountService;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private EmailLoginFormValidator emailLoginFormValidator;

    @InitBinder("signUpForm")
    private void initBinder_singUp(WebDataBinder webDataBinder){
        webDataBinder.addValidators(validator);
    }

    @InitBinder("emailLoginForm")
    public void initBinder_emailLogin(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(emailLoginFormValidator);
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

        Account newAccount = accountService.complateSignUp(account);

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
    public String reCheckEmailToken_btn(@CurrentUser Account account, Model model) {
        if (account != null) {
            model.addAttribute(account);
            return "account/reckecked-email";
        }

        //TODO login 화면으로 이동시켜주기
        return "index";
    }

    @GetMapping("/profile/{nickname}")
    public String viewProfile(@PathVariable String nickname, Model model, @CurrentUser Account account) {
        final Account byNickname = accountRepository.findByNickname(nickname);
        if(byNickname == null){
            throw new IllegalArgumentException(nickname + "에 해당하는 사용자가 없습니다.");
        }

        model.addAttribute(byNickname);
        model.addAttribute("isOwner", byNickname.equals(account));

        return "account/profile";
    }

    @GetMapping("/email-login")
    public String emailLoginForm(){
        return EMAIL_LOGIN;
    }

    @PostMapping("/email-login")
    public String emailLoginUpdate(@Valid EmailLoginForm emailLoginForm, Errors errors, Model model, RedirectAttributes redirectAttributes){
        //TODO email Token 전송
        if(errors.hasErrors()){
            model.addAttribute("error");
            return "redirect:/account/email-login";
        }
        accountService.sendLoginbyEmail(emailLoginForm.getEmail());
        redirectAttributes.addFlashAttribute("message", "이메일 인증 메일을 발송했습니다");
        return EMAIL_LOGIN;
    }

    @GetMapping("/login-email-token")
    public String loginEmailCheck(String token, String email, Model model){
        Account byNickname = accountRepository.findByEmail(email);

        if(byNickname == null && !byNickname.isValidToken(token)){
            model.addAttribute("error");
            return "account/loign-in-by-email";
        }
        accountService.login(byNickname);
        return "account/loign-in-by-email";
    }
}
