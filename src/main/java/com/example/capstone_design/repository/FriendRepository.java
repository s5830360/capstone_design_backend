package com.example.capstone_design.repository;

import com.example.capstone_design.entity.Friend;
import com.example.capstone_design.entity.FriendStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface FriendRepository extends JpaRepository<Friend, Long> {

    // 🔹 중복 요청 방지 (한쪽에서 이미 요청한 경우 확인)
    Optional<Friend> findByUserIdAndFriendId(Long userId, Long friendId);

    // 🔹 내가 보낸 요청 조회
    List<Friend> findAllByUserId(Long userId);

    // 🔹 내가 받은 요청 조회
    List<Friend> findAllByFriendId(Long friendId);

    // 🔹 내가 보낸 요청 중 특정 상태 조회 (예: PENDING, ACCEPTED)
    List<Friend> findAllByUserIdAndStatus(Long userId, FriendStatus status);

    // 🔹 내가 받은 요청 중 특정 상태 조회
    List<Friend> findAllByFriendIdAndStatus(Long friendId, FriendStatus status);

    List<Friend> findByUserIdOrFriendIdAndStatus(Long userId, Long friendId, FriendStatus status);

    @Query("""
        SELECT COUNT(f) > 0 FROM Friend f
        WHERE (
            (f.user.id = :id1 AND f.friend.id = :id2) OR
            (f.user.id = :id2 AND f.friend.id = :id1)
        )
        AND f.status = :status
    """)
    boolean existsFriendship(Long id1, Long id2, FriendStatus status);

}