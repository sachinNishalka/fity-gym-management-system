package pro.sachin.fity.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import pro.sachin.fity.dto.GymAccessDTO;
import pro.sachin.fity.sercives.MemberAccessService;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("api/v1/gym-access")
@RequiredArgsConstructor
public class GymAccessController {

    private final MemberAccessService memberAccessService;
    
   @GetMapping("/all-members-access")
   public ResponseEntity<List<GymAccessDTO>> getMethodName() {
      List<GymAccessDTO> gymAccessDTOs = memberAccessService.getAllMemberAccess();
      return ResponseEntity.ok(gymAccessDTOs);
   }
   


}
