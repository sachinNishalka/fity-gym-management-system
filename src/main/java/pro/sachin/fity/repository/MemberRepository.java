package pro.sachin.fity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pro.sachin.fity.model.Member;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
}
