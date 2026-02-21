package com.bookshop.security;

import com.bookshop.entity.UserAccount;
import com.bookshop.repository.UserAccountRepo;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserDetailsServiceImp implements UserDetailsService {

    private UserAccountRepo userAccountRepo;


    public UserDetailsServiceImp(UserAccountRepo userAccountRepo) {
        this.userAccountRepo = userAccountRepo;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<UserAccount> userAccount = this.userAccountRepo.findByUsername(username);
        if (userAccount.isEmpty()) {
            throw new BadCredentialsException("Invalid username or password");
        }
        return userAccount.get();
//        return User.builder().username(userAccount.get().getUsername()).authorities(userAccount.get().getAuthorities()).password(userAccount.get().getPassword()).build();
    }
}
