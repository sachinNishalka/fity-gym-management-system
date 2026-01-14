package pro.sachin.fity.sercives.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pro.sachin.fity.model.Family;
import pro.sachin.fity.repository.FamilyRepository;
import pro.sachin.fity.sercives.FamilyService;

@Service
@RequiredArgsConstructor
public class FamilyServiceImpl implements FamilyService {

    private final FamilyRepository familyRepository;

    @Override
    public void createFamily(Family family) {
        familyRepository.save(family);
    }
}
