package com.infrean_study.domain;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Getter @Setter @EqualsAndHashCode(of = "id")
@Builder @AllArgsConstructor @NoArgsConstructor
public class Account {

    @Id
    @GeneratedValue
    private Long id;

    @Column(unique = true)
    private String email;
    @Column(unique = true)
    private String nickname;

    private String password;

    private boolean emailVerified; // 이메일 검증 여부

    private String emailCheckToken; // 이메일 토큰 값

    private LocalDateTime joinedAt; // 계정 생성 일자

    private LocalDateTime emailTokenJoinedAt; // 계정 인증 일자

    private String bio; //프로필 정보

    private String url;

    private String occupation;

    private String location;

    @Lob @Basic(fetch = FetchType.EAGER)
    private String profileImage;

    private boolean studyCreatedByEmail= false;

    private boolean studyCreatedByWeb = true;

    private boolean studyEnrollmentResultByEmail = false;

    private boolean studyEnrollmentResultByWeb = true;

    private boolean studyUpdatedByEmail = false;

    private boolean studyUpdatedByWeb = true;

    @ManyToMany
    private Set<Tag> tags = new HashSet<>();

    @ManyToMany
    private Set<Zone> zones = new HashSet<>();

    public void generateEmailCheckToken() {
        this.emailCheckToken = UUID.randomUUID().toString();
    }

    public void completeSignUp() {
        this.emailVerified = true;
        this.emailTokenJoinedAt = LocalDateTime.now();
    }

    public boolean isValidToken(String token) {
        return this.emailCheckToken.equals(token);
    }

    public Boolean canResendMailTimeCheck() {
        return this.joinedAt.isBefore(LocalDateTime.now().minusHours(1));
    }
}
