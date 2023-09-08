package com.gsm.blabla.jwt;

import com.gsm.blabla.global.response.Code;
import com.gsm.blabla.global.exception.GeneralException;
import com.gsm.blabla.jwt.dto.JwtDto;
import com.gsm.blabla.member.domain.Member;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TokenProvider {
    private static final String AUTHORITIES_KEY = "auth";
    private static final String BEARER_TYPE = "Bearer";
    private static final long ACCESS_TOKEN_EXPIRE_TIME = 1000 * 60 * 30;            // 30분
    private static final long REFRESH_TOKEN_EXPIRE_TIME = 1000 * 60 * 60 * 24 * 7;  // 7일

    private final Key key;

    public TokenProvider(@Value("${jwt.secret}") String secretKey) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    public JwtDto generateJwt(Member member) {
        long now = (new Date()).getTime();
        Date accessTokenExpiresIn = new Date(now + (ACCESS_TOKEN_EXPIRE_TIME));
        Date refreshTokenExpiresIn = new Date(now + (REFRESH_TOKEN_EXPIRE_TIME));

        // Access Token 생성
        String accessToken = Jwts.builder()
            .setSubject(member.getId().toString()) // 토큰 용도
            .claim("socialLoginType", member.getSocialLoginType()) // claims(payload에 들어갈 내용) 설정
            .claim("nickname", member.getNickname())
            .claim("countryCode", member.getCountryCode())
            .claim(AUTHORITIES_KEY, member.getRole())
            .setExpiration(accessTokenExpiresIn) // 토큰 만료 시간 설정
            .signWith(key, SignatureAlgorithm.HS512)
            .compact(); // 토큰 생성

        // Refresh Token 생성
        String refreshToken = Jwts.builder()
            .setExpiration(refreshTokenExpiresIn)
            .signWith(key, SignatureAlgorithm.HS512)
            .compact();

        return JwtDto.builder()
            .grantType(BEARER_TYPE)
            .accessToken(accessToken)
            .accessTokenExpiresIn(accessTokenExpiresIn.getTime())
            .refreshToken(refreshToken)
            .refreshTokenExpiresIn(refreshTokenExpiresIn.getTime())
            .build();
    }

    // 토큰 정보 검증
    public Authentication getAuthentication(String accessToken) {
        // 토큰 복호화
        Claims claims = parseClaims(accessToken);

        if (claims.get(AUTHORITIES_KEY) == null) {
            throw new GeneralException(Code.UNAUTHORIZED, "권한 정보가 없는 토큰입니다.");
        }

        // 클레임에서 권한 정보 가져오기
        Collection<? extends GrantedAuthority> authorities =
            Arrays.stream(claims.get(AUTHORITIES_KEY).toString().split(","))
                .map(SimpleGrantedAuthority::new)
                .toList();

        // UserDetails 객체를 만들어서 Authentication 리턴
        UserDetails principal = new User(claims.getSubject(), "", authorities);

        return new UsernamePasswordAuthenticationToken(principal, "", authorities);
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
//            throw new GeneralException(Code.MALFORMED_JWT);
        } catch (ExpiredJwtException e) {
//            throw new GeneralException(Code.EXPIRED_JWT);
        } catch (UnsupportedJwtException e) {
//            throw new GeneralException(Code.UNSUPPORTED_JWT);
        } catch (IllegalArgumentException e) {
//            throw new GeneralException(Code.ILLEGAL_JWT);
        }
        return false;
    }

    private Claims parseClaims(String accessToken) {
        try {
            return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(accessToken).getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }
}
