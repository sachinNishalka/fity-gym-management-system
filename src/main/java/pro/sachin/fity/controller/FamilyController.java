package pro.sachin.fity.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pro.sachin.fity.model.Family;
import pro.sachin.fity.sercives.FamilyService;

@RequiredArgsConstructor
@RestController
@RequestMapping("api/v1/family")
public class FamilyController {
    private final FamilyService familyService;
    @PostMapping("/create")
    ResponseEntity<Family> createFamily(@RequestBody Family family){
        familyService.createFamily(family);
        return ResponseEntity.ok(family);
    }
}
