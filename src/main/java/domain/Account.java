package domain;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

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

    private LocalDateTime joinedAt;

    private String bio; //프로필 정보

    private String url;

    private String occupation;

    private String location;

    @Lob @Basic(fetch = FetchType.EAGER)
    private String profileImage;


}
