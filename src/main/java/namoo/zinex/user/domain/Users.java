package namoo.zinex.user.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import namoo.zinex.core.entity.BaseEntity;
import namoo.zinex.user.enums.Role;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "users")
public class Users extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role = Role.USER;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status = Status.ACTIVE;

    public enum Status {
        ACTIVE,
        SUSPENDED,
        DELETED
    }

    ///  회원가입 등 신규 사용자 생성용 팩토리 메서드
    public static Users createUser(String email, String password, String name) {
        Users user = new Users();
        user.email = email;
        user.password = password;
        user.name = name;
        return user;
    }

   
}
