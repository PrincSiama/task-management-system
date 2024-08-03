package dev.sosnovsky.task.management.system.service;


import dev.sosnovsky.task.management.system.model.User;

import java.security.Principal;

public interface UserPrincipalService {
    User getUserFromPrincipal(Principal principal);
}