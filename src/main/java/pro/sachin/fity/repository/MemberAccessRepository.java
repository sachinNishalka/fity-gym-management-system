package pro.sachin.fity.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import pro.sachin.fity.model.MemberAccess;

public interface MemberAccessRepository extends JpaRepository<MemberAccess, Long> {
    
}
