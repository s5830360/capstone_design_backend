package com.example.capstone_design.service;

import com.example.capstone_design.entity.UserAccount;
import com.example.capstone_design.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserAccountRepository repo;

    public List<UserAccount> list() {
        return repo.findAll();
    }

    public void delete(Long id) {
        repo.deleteById(id);
    }

    // 🔹 username → email 기반으로 변경
    public UserAccount findByEmail(String email) {
        return repo.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "user not found"));
    }
}
