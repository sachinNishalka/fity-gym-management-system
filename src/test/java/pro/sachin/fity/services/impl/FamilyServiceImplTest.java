package pro.sachin.fity.services.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import pro.sachin.fity.dto.FamilyDTO;
import pro.sachin.fity.mapper.SubscriptionMapper;
import pro.sachin.fity.model.Family;
import pro.sachin.fity.model.Member;
import pro.sachin.fity.repository.FamilyRepository;
import pro.sachin.fity.repository.MemberRepository;
import pro.sachin.fity.repository.PlanRepository;
import pro.sachin.fity.repository.SubscriptionRepository;
import pro.sachin.fity.sercives.impl.FamilyServiceImpl;

@ExtendWith(MockitoExtension.class)
public class FamilyServiceImplTest {

    @Mock
    FamilyRepository familyRepository;
    @Mock
    MemberRepository memberRepository;
    @Mock
    SubscriptionRepository subscriptionRepository;
    @Mock
    SubscriptionMapper subscriptionMapper;
    @Mock
    PlanRepository planRepository;

    @InjectMocks
    FamilyServiceImpl familyService;

    @Test
    void createFamilyShouldAddFamilySuccessfully() {

        // Arrange
        FamilyDTO dto = new FamilyDTO();
        dto.setFamilyName("Smith Family");
        dto.setMemberIds(List.of(1L, 2L));

        Member member1 = new Member();
        member1.setId(1L);

        Member member2 = new Member();
        member2.setId(2L);

        when(memberRepository.findAllById(dto.getMemberIds()))
                .thenReturn(List.of(member1, member2));

        Family savedFamily = new Family();
        savedFamily.setId(1L);
        savedFamily.setFamilyName("Smith Family");

        savedFamily.setMembers(Set.of(member1, member2));

        when(familyRepository.save(any(Family.class)))
                .thenReturn(savedFamily);

        // Act
        Family result = familyService.createFamily(dto);

        // Assert
        assertEquals("Smith Family", result.getFamilyName());

        verify(familyRepository).save(any(Family.class));

        ArgumentCaptor<Family> captor = ArgumentCaptor.forClass(Family.class);

        verify(familyRepository).save(captor.capture());

        Family family = captor.getValue();

        assertEquals("Smith Family", family.getFamilyName());
        assertEquals(2, family.getMembers().size());

    }

}
