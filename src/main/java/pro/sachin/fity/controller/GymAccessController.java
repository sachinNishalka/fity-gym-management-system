package pro.sachin.fity.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import pro.sachin.fity.dto.GymAccessDTO;
import pro.sachin.fity.dto.MemberAccessDTO;
import pro.sachin.fity.model.MemberAccess;
import pro.sachin.fity.sercives.MemberAccessService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("api/v1/gym-access")
@RequiredArgsConstructor
@CrossOrigin
public class GymAccessController {

   private final MemberAccessService memberAccessService;

   @GetMapping
   public ResponseEntity<List<MemberAccessDTO>> getAllMemberAccess() {
      List<MemberAccess> memberAccessList = memberAccessService.getAllMemberAccess();

      List<MemberAccessDTO> list = new ArrayList<>();

      for (MemberAccess gymAccess : memberAccessList) {
         MemberAccessDTO memberAccessDTO = new MemberAccessDTO();
         memberAccessDTO.setMemberName(gymAccess.getMember().getFirstName());
         memberAccessDTO.setMemberCode(gymAccess.getMember().getMemberCode());
         memberAccessDTO.setAccessStatus(gymAccess.getAccessStatus());
         memberAccessDTO.setAllowedUntil(gymAccess.getAllowedUntil());

         list.add(memberAccessDTO);
      }

      return ResponseEntity.ok(list);
   }

   @PostMapping("/recover-blocked-access")
   public ResponseEntity<Map<String, Object>> recoverBlockedAccess() {
      List<String> recoveredMembers = memberAccessService.recoverIncorrectlyBlockedMembers();

      Map<String, Object> response = new HashMap<>();
      response.put("recoveredCount", recoveredMembers.size());
      response.put("recoveredMembers", recoveredMembers);
      response.put("message", recoveredMembers.isEmpty()
            ? "No incorrectly blocked members found"
            : "Access restored. Device commands queued for " + recoveredMembers.size() + " member(s).");

      return ResponseEntity.ok(response);
   }

}
