package dev.sosnovsky.task.management.system.service;

import dev.sosnovsky.task.management.system.exception.NotFoundException;
import dev.sosnovsky.task.management.system.model.User;
import dev.sosnovsky.task.management.system.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.Principal;

@Service
@AllArgsConstructor
public class UserPrincipalServiceImpl implements UserPrincipalService {
    private final UserRepository userRepository;

    @Override
    public User getUserFromPrincipal(Principal principal) {
        return userRepository.findByEmail(principal.getName()).orElseThrow(
                () -> new NotFoundException("Пользователь с email " + principal.getName() + " не найден"));
    }
}