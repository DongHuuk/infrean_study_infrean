package com.infrean_study.module.account;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
public class EmailLoginForm {

    @Email
    @NotBlank
    private String email;

}
