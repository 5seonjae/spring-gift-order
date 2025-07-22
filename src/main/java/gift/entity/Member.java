package gift.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "members")
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "is_admin", nullable = false)
    private boolean isAdmin = false;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WishItem> wishItems = new ArrayList<>();

    public Member(Long id, String email, String password) {
        validate(email, password);

        this.id = id;
        this.email = email;
        this.password = password;
    }

    protected Member() {}

    public Member(String email, String password) {
        this(null, email, password);
    }

    private void validate(String email, String password) {
        if (email == null || email.isEmpty()) {
            throw new IllegalArgumentException("이메일은 필수입니다.");
        }

        // ".+@.+\\..+" : something@something.something 형태
        // '.+' : 1자 이상의 아무 문자 / '@' : 반드시 @ 존재 / '\\.' 반드시 . 존재
        if (!email.matches(".+@.+\\..+")) {
            throw new IllegalArgumentException("유효한 이메일 형식이 아닙니다.");
        }

        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("비밀번호는 필수입니다.");
        }

        if (password.length() < 6) {
            throw new IllegalArgumentException("비밀번호는 최소 6자 이상이어야 합니다.");
        }
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public boolean getIsAdmin() {
        return isAdmin;
    }
}
