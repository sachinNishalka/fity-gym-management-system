package pro.sachin.fity.sercives;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import pro.sachin.fity.dto.MemberDTO;
import pro.sachin.fity.model.Member;

public interface MemberService {

    // TODO: This method should also create MemberAccess record with status=BLOCKED
    // TODO: Member gets ALLOWED access only after subscribing and paying
    void registerMember(MemberDTO memberDTO);

    // TODO: IMPLEMENT - Update member details
    // TODO: Member updateMember(Long memberId, MemberUpdateDTO updateDTO);
    // TODO: Allow updating: phone, email, address, emergency contact
    // TODO: Don't allow updating: memberId, joinedDate, status (use separate method
    // for status)

    // TODO: IMPLEMENT - Soft delete member
    // TODO: void deleteMember(Long memberId);
    // TODO: Change status to INACTIVE instead of actual deletion
    // TODO: Validation: Cannot delete if has active subscriptions
    // TODO: Update member_access.access_status = BLOCKED

    // TODO: IMPLEMENT - Change member status
    // TODO: void changeMemberStatus(Long memberId, MemberStatus newStatus, Long
    // userId);
    // TODO: Allow: ACTIVE, INACTIVE, SUSPENDED
    // TODO: Log who changed status and when (audit trail)
    // TODO: If INACTIVE or SUSPENDED, block door access

    // TODO: IMPLEMENT - Get member by ID with full details
    // TODO: MemberDetailDTO getMemberById(Long memberId);
    // TODO: Include: basic info, current subscription, payment status, access
    // status

    // TODO: IMPLEMENT - Search members
    // TODO: Page<MemberDTO> searchMembers(String name, String phone, MemberStatus
    // status, Pageable pageable);
    // TODO: Support partial name matching, phone lookup, filter by status

    // TODO: IMPLEMENT - Get member's active subscription
    // TODO: Subscription getActiveMemberSubscription(Long memberId);
    // TODO: Return current active subscription or null

    // TODO: IMPLEMENT - Check member eligibility for plan
    // TODO: boolean isEligibleForPlan(Long memberId, Long planId);
    // TODO: Check age for KIDS plans
    // TODO: Check no overlapping active subscriptions
    // TODO: Check member status is ACTIVE

    // TODO: IMPLEMENT - Get member's payment history
    // TODO: List<PaymentDTO> getMemberPaymentHistory(Long memberId);
    // TODO: Return all payments across all subscriptions

    // TODO: IMPLEMENT - Get member's attendance records
    // TODO: List<AttendanceDTO> getMemberAttendance(Long memberId, LocalDate from,
    // LocalDate to);
    // TODO: Return check-in records for date range

    // TODO: IMPLEMENT - Calculate member age from DOB
    // TODO: int calculateAge(Long memberId);
    // TODO: Helper method for age-based plan eligibility

    // method to get all members
    List<MemberDTO> getAllMembers();

    Member getMemberById(Long id);

    Member updateMember(Long memberId, MemberDTO memberDTO);

    void deleteMember(Long memberId);
}
