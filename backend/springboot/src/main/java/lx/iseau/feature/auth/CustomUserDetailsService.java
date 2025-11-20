// package lx.iseau.feature.auth;

// import java.util.Map;
// import lx.iseau.feature.auth.AuthDAO;
// import lombok.RequiredArgsConstructor;
// import org.springframework.security.core.userdetails.UserDetails;
// import org.springframework.security.core.userdetails.UserDetailsService; // UserDetailsService 인터페이스를 상속합니다.
// import org.springframework.security.core.userdetails.UsernameNotFoundException;
// import org.springframework.stereotype.Service;

// /**
//  * [필수 구현체] Spring Security의 UserDetailsService 인터페이스 구현 클래스입니다.
//  * Firebase UID를 받아 AuthDAO를 통해 DB에서 사용자 정보를 로드하고 CustomUserDetails로 변환합니다.
//  * @Service로 등록되어 Spring Security의 AuthenticationManager에 의해 호출됩니다.
//  */
// @Service
// @RequiredArgsConstructor
// public class CustomUserDetailsService implements UserDetailsService {

//     private final AuthDAO authDAO; // DB 접근을 위한 DAO 주입

//     /**
//      * FirebaseTokenFilter에서 추출한 UID(id)를 받아 DB에서 사용자 정보를 조회합니다.
//      * @param firebaseUid Firebase에서 인증된 사용자의 고유 ID (tb_user.id와 매핑)
//      * @return CustomUserDetails 객체 (userNumber와 권한 정보 포함)
//      * @throws UsernameNotFoundException DB에 해당 UID 사용자가 없을 경우
//      */
//     @Override
//     public UserDetails loadUserByUsername(String firebaseUid) throws UsernameNotFoundException {
        
//         // 1. AuthDAO를 사용하여 DB에서 사용자 정보 조회 (findUserInfoByUserId 쿼리 호출)
//         Map<String, Object> userInfo = authDAO.findUserNumberByUserId(firebaseUid);

//         if (userInfo == null || userInfo.isEmpty()) {
//             // 2. DB에 등록된 사용자가 없으면 인증 실패 처리
//             throw new UsernameNotFoundException("DB에 등록된 사용자(UID: " + firebaseUid + ")를 찾을 수 없습니다.");
//         }

//         // 3. 조회된 Map 정보를 CustomUserDetails 객체로 변환하여 Spring Security에 반환
//         return new CustomUserDetails(userInfo);
//     }
// }