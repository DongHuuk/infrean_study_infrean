package com.infrean_study.domain;

import com.infrean_study.account.UserAccount;
import lombok.*;

import javax.persistence.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

//EAGER로 EntityGraph 선언
@NamedEntityGraph(name = "Study.withAll", attributeNodes = {
        @NamedAttributeNode("tags"),
        @NamedAttributeNode("zones"),
        @NamedAttributeNode("managers"),
        @NamedAttributeNode("members")})
@Entity
@Getter @Setter @AllArgsConstructor
@NoArgsConstructor @Builder @EqualsAndHashCode(of = "id")
public class Study {

    public static String DEFAULT_IMAGE = "default-banner.jpg";

    @Id @GeneratedValue
    private Long id;

    @ManyToMany
    @Builder.Default
    private Set<Account> managers = new HashSet<>();

    @ManyToMany
    @Builder.Default
    private Set<Account> members = new HashSet<>();

    @Column(unique = true)
    private String path; // URL 값

    private String title;

    private String shortDescription; // 짧은 소개

    @Lob @Basic(fetch = FetchType.EAGER)
    private String fullDescription;

    @Lob @Basic(fetch = FetchType.EAGER)
    private String image;

    @ManyToMany
    @Builder.Default
    private Set<Zone> zones = new HashSet<>();

    @ManyToMany
    @Builder.Default
    private Set<Tag> tags = new HashSet<>();

    private LocalDateTime publishedDateTime;

    private LocalDateTime closedDateTime;

    private LocalDateTime recruitingUpdatedDateTime; // 너무 자주 열닫하지 않게 조절

    private boolean recruiting; // 인원 모집중

    private boolean published; // 공개 여부

    private boolean closed; // 종료 여부

    private boolean useBanner; // 배너 사용 여부

    public void addManager(Account account) {
        this.managers.add(account);
    }

    public boolean isJoinable(UserAccount account) {
        return this.isPublished() && this.isRecruiting() &&
                !this.members.contains(account.getAccount()) && !this.managers.contains(account.getAccount());
    }

    public boolean isMember(UserAccount account) {
        return this.members.contains(account.getAccount());
    }

    public boolean isManager(UserAccount Account) {
        return this.managers.contains(Account.getAccount());
    }
    public boolean isManager(Account Account) {
        return this.managers.contains(Account);
    }

    public String getEncodePath() {
        return URLEncoder.encode(this.path, StandardCharsets.UTF_8);
    }

    public String getImage(){
        return image != null ? image : DEFAULT_IMAGE;
    }
}
