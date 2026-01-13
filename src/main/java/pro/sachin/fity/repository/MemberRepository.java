package pro.sachin.fity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pro.sachin.fity.model.Member;

public interface MemberRepository extends JpaRepository<Member, Integer> {
}
