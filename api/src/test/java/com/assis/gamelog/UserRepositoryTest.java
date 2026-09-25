package com.assis.gamelog;

import com.assis.gamelog.model.User;
import com.assis.gamelog.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void shouldPersistUserAndGenerateCreatedAt() {
        User user = User.builder()
                .email("player@email.com")
                .username("player")
                .password("hash")
                .build();

        User saved = userRepository.save(user);

        assertNotNull(saved.getId());
        assertNotNull(saved.getCreatedAt());
    }

    @Test
    void shouldFindUserByEmail() {
        User user = User.builder()
                .email("find@email.com").username("find").password("hash")
                .build();
        entityManager.persistAndFlush(user);

        assertTrue(userRepository.findByEmail("find@email.com").isPresent());
        assertTrue(userRepository.existsByEmail("find@email.com"));
        assertFalse(userRepository.existsByEmail("other@email.com"));
    }

    @Test
    void shouldNotAllowDuplicateEmail() {
        User user1 = User.builder()
                .email("same@email.com").username("first").password("hash")
                .build();
        entityManager.persistAndFlush(user1);

        User user2 = User.builder()
                .email("same@email.com").username("second").password("hash")
                .build();

        assertThrows(DataIntegrityViolationException.class, () -> {
            userRepository.saveAndFlush(user2);
        });
    }
}
