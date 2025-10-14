package at.technikum.paperless.utils;

import at.technikum.paperless.domain.User;
import at.technikum.paperless.exception.MissingAuthorException;
import at.technikum.paperless.repository.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class UserUtils {
    @Autowired
    private UserRepository userRepository;

    public User getCurrentUser(){
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) return null;
        var username = authentication.getName();
        return userRepository.findByUsername(username).orElseThrow(MissingAuthorException::new);
    }
}
