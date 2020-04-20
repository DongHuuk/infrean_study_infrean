package com.infrean_study.mail;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EmailMessage {

    private String to;

    private String subject;

    private String message;

}

//전송 시 Data
