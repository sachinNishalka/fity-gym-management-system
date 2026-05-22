package pro.sachin.fity.controller;

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import pro.sachin.fity.dto.MemberDTO;
import pro.sachin.fity.model.Member;
import pro.sachin.fity.sercives.MemberService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@RequiredArgsConstructor
@RestController
@RequestMapping("api/v1/member")
@CrossOrigin
public class MemberController {
    private final MemberService memberService;

    @PostMapping("/register")
    ResponseEntity<MemberDTO> registerMember(@RequestBody MemberDTO memberDTO) {
        memberService.registerMember(memberDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(memberDTO);
    }

    // TODO: IMPLEMENT - Update member endpoint
    // TODO: PUT /api/v1/member/{id}
    // TODO: Accept MemberUpdateDTO (allow updating phone, email, address, etc.)
    // TODO: Don't allow changing: memberId, joinedDate, status (business critical
    // fields)

    // TODO: IMPLEMENT - Delete member endpoint (soft delete)
    // TODO: DELETE /api/v1/member/{id}
    // TODO: Don't actually delete - change status to INACTIVE
    // TODO: Check business rule: Can only delete if no active subscriptions

    // TODO: IMPLEMENT - Change member status endpoint
    // TODO: PATCH /api/v1/member/{id}/status
    // TODO: Allow changing between ACTIVE/INACTIVE/SUSPENDED
    // TODO: Log who made the change and when (audit trail)

    // TODO: IMPLEMENT - Get member by ID
    // TODO: GET /api/v1/member/{id}
    // TODO: Return MemberDTO with subscription info, access status, payment status

    // TODO: IMPLEMENT - Search members endpoint
    // TODO: GET /api/v1/member/search?name=john&phone=123&status=active
    // TODO: Support filtering by name, phone, email, status
    // TODO: Return paginated results (Page<MemberDTO>)

    // TODO: IMPLEMENT - Get member with subscription details
    // TODO: GET /api/v1/member/{id}/subscriptions
    // TODO: Return all subscriptions for member (active and historical)

    // TODO: IMPLEMENT - Get member payment history
    // TODO: GET /api/v1/member/{id}/payments
    // TODO: Return all payments made by member

    // TODO: IMPLEMENT - Get member attendance history
    // TODO: GET /api/v1/member/{id}/attendance
    // TODO: Return recent attendance records (last 30 days by default)

    // TODO: IMPLEMENT - Check member eligibility for plan
    // TODO: GET /api/v1/member/{id}/eligible-plans
    // TODO: Return plans member is eligible for based on age, current subscriptions

    // TODO: VALIDATION - Add @Valid annotation to request bodies
    // TODO: VALIDATION - Add global exception handling with @ControllerAdvice
    // TODO: SECURITY - Add authentication/authorization (only staff can register
    // members)

    @GetMapping("/all")
    public ResponseEntity<List<MemberDTO>> getAllMembers() {
        List<MemberDTO> allMembers = memberService.getAllMembers();
        return new ResponseEntity<List<MemberDTO>>(allMembers, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Member> getMemberById(@PathVariable Long id) {
        Member member = memberService.getMemberById(id);
        return new ResponseEntity<Member>(member, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Member> updateMember(@PathVariable Long id, @RequestBody MemberDTO memberDTO) {
        Member updatedMember = memberService.updateMember(id, memberDTO);
        return new ResponseEntity<Member>(updatedMember, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deleteMember(@PathVariable Long id) {
        memberService.deleteMember(id);
        return new ResponseEntity<HttpStatus>(HttpStatus.OK);
    }

}
