package pro.sachin.fity.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import pro.sachin.fity.model.AccessStatus;
import pro.sachin.fity.model.MemberAccess;

public interface MemberAccessRepository extends JpaRepository<MemberAccess, Long> {
    
    List<MemberAccess> findAllByAccessStatus(AccessStatus accessStatus);
}
