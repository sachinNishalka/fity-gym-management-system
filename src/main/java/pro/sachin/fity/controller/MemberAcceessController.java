package pro.sachin.fity.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import pro.sachin.fity.dto.GymAccessDTO;
import pro.sachin.fity.dto.MemberAccessDTO;
import pro.sachin.fity.sercives.MemberAccessService;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("api/v1/gym-access")
@RequiredArgsConstructor
public class MemberAcceessController {

   private final MemberAccessService memberAccessService;

   @GetMapping
   public ResponseEntity<List<MemberAccessDTO>> getAllMemberAccess() {

      List<MemberAccessDTO> memberAccessDTO = memberAccessService.getAllMembersAccess();
      return ResponseEntity.ok(memberAccessDTO);

   }

}
