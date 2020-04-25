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
@NamedEntityGraph(name = "Study.withTagsAndManagers", attributeNodes = {
        @NamedAttributeNode("managers"),
        @NamedAttributeNode("tags")})
@NamedEntityGraph(name = "Study.withZonesAndManagers", attributeNodes = {
        @NamedAttributeNode("managers"),
        @NamedAttributeNode("zones")})
@NamedEntityGraph(name = "Study.withRecruitingAndPublishedAndClosed", attributeNodes = {
        @NamedAttributeNode("managers"),
        @NamedAttributeNode("recruiting"),
        @NamedAttributeNode("published"),
        @NamedAttributeNode("closed")})
@NamedEntityGraph(name = "Study.withMembersAndPath", attributeNodes = {
        @NamedAttributeNode("managers"),
        @NamedAttributeNode("path")})
@NamedEntityGraph(name = "Study.withMembersAndTitle", attributeNodes = {
        @NamedAttributeNode("managers"),
        @NamedAttributeNode("title")})
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

    public void publish() {
        if(!this.closed && !this.published){
            this.published = true;
            this.publishedDateTime = LocalDateTime.now();
        }else {
            throw new RuntimeException("스터디를 공개할 수 없는 상태입니다. 스터디를 이미 공개했거나 종료되었습니다");
        }
    }

    public void close() {
        if(!this.closed && this.published){
            this.closed = true;
            this.closedDateTime = LocalDateTime.now();
        }else {
            throw new RuntimeException("스터디를 종료할 수 없습니다. 스터디를 공개하지 않았거나 이미 종료 된 스터디입니다.");
        }
    }

    public void startRecruiting() {
        if(canUpdateRecruiting()){
            this.recruiting = true;
            this.recruitingUpdatedDateTime = LocalDateTime.now();
        }else {
            throw new RuntimeException("인원 모집을 시작할 수 없습니다. 스터디를 공개하거나 한 시간 뒤 다시 시도하세요.");
        }
    }

    public void stopRecruiting() {
        if(this.recruiting && this.published){
            this.recruiting = false;
            this.recruitingUpdatedDateTime = LocalDateTime.now();
        }else {
            throw new RuntimeException("인원 모집을 멈출 수 없습니다. 스터디를 공개하거나 한 시간 뒤 다시 시도하세요.");
        }
    }

    public boolean canUpdateRecruiting() {
        return this.published && this.recruitingUpdatedDateTime == null || this.recruitingUpdatedDateTime.isBefore(LocalDateTime.now().minusHours(1));
    }
}
