package pro.sachin.fity.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pro.sachin.fity.model.Member;
import pro.sachin.fity.sercives.MemberService;

@RequiredArgsConstructor
@RestController
@RequestMapping("api/v1/member")
public class MemberController {
    private final MemberService memberService;
    //    TODO: member register controller
    @PostMapping("/register")
    ResponseEntity<Member> registerMember(@RequestBody Member member){
        memberService.registerMember(member);
        return ResponseEntity.ok(member);
    }
//    TODO: update member controller

//    TODO: delete member controller

//    TODO: change member status

//    TODO: other controllers for a member

}
