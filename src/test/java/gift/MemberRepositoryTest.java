package gift;

import gift.entity.Member;
import gift.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class MemberRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("findByEmail & existsByEmail: 저장된 이메일에 대해 조회 및 존재 확인")
    void findByEmail_and_existsByEmail_shouldWork() {
        // given
        Member m = new Member("user@example.com", "secret");
        memberRepository.save(m);

        // when
        Optional<Member> found = memberRepository.findByEmail("user@example.com");
        boolean exists = memberRepository.existsByEmail("user@example.com");

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("user@example.com");
        assertThat(found.get().getIsAdmin()).isFalse();
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("findByEmail: 없는 이메일 조회 시 empty 반환")
    void findByEmail_shouldReturnEmpty_whenNotExists() {
        Optional<Member> found = memberRepository.findByEmail("noone@example.com");
        assertThat(found).isEmpty();
    }
}
