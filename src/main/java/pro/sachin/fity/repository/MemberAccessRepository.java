package pro.sachin.fity.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import pro.sachin.fity.model.GraceExtension;
import pro.sachin.fity.model.MemberAccess;

public interface MemberAccessRepository extends JpaRepository<MemberAccess, Long> {

    @Query("""
                SELECT ma
                FROM MemberAccess ma
                JOIN FETCH ma.member
            """)
    List<MemberAccess> findAllWithSubscriptionAndMember();
}
