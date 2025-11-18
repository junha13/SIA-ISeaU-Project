package lx.iseau.feature.auth;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Spring Security의 UserDetails 인터페이스 구현체입니다.
 * DB PK인 userNumber를 저장하여 Controller에서 @AuthenticationPrincipal로 사용 가능하게 합니다.
 */
public class CustomUserDetails implements UserDetails {

    private final int userNumber; // DB PK: @AuthenticationPrincipal로 추출할 핵심 정보
    private final String id; // Firebase UID
    private final String password; // 암호화된 비밀번호 해시 (형식 유지용)
    private final Collection<? extends GrantedAuthority> authorities;

    public CustomUserDetails(Map<String, Object> userInfo) {
        // Map에서 DB 정보 추출 및 타입 캐스팅
        // DB 쿼리(findUserInfoByUserId)의 결과 Map을 받습니다.
        this.userNumber = (Integer) userInfo.get("user_number");
        this.id = (String) userInfo.get("id");
        this.password = (String) userInfo.get("password"); 
        
        // 역할(Role) 정보를 기반으로 권한 설정
        int roleNumber = (Integer) userInfo.get("role_number");
        String roleName = (roleNumber == 1) ? "ROLE_ADMIN" : "ROLE_USER";
        this.authorities = Collections.singletonList(new SimpleGrantedAuthority(roleName));
    }

    /**
     * Controller에서 @AuthenticationPrincipal을 통해 접근할 수 있는 userNumber 게터.
     * @return DB의 회원 고유 번호
     */
    public int getUserNumber() {
        return userNumber;
    }

    // --- Spring Security 필수 메서드 ---
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return id;
    }
    
    // 계정 상태 관리 (발표용 프로젝트에서는 기본적으로 true 반환)
    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }
}