package dev.sosnovsky.task.management.system.service;

import dev.sosnovsky.task.management.system.exception.NotFoundException;
import dev.sosnovsky.task.management.system.model.User;
import dev.sosnovsky.task.management.system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) {
        Optional<User> user = userRepository.findByEmail(email);
        return user.orElseThrow(() -> new NotFoundException("Пользователь с email " + email + " не найден"));
    }
}