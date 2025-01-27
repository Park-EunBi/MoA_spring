package com.springboot.moa.utils;

import com.springboot.moa.config.BaseException;
import com.springboot.moa.config.secret.Secret;
import com.springboot.moa.user.UserDao;
import com.springboot.moa.user.UserProvider;
import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

import static com.springboot.moa.config.BaseResponseStatus.*;

@Service
public class JwtService {
    //access-token 유효기간 : 30분
    public String createAccessToken(long userId){
        Date now = new Date();
        return Jwts.builder()
                .setHeaderParam("type","jwt")
                .claim("userId",userId)
                .setIssuedAt(now)
                .setExpiration(new Date(System.currentTimeMillis()+1*(1000*60*30)))
                .signWith(SignatureAlgorithm.HS256, Secret.JWT_ACCESS_SECRET_KEY)
                .compact();
    }

    //안드로이드 용 access-token 유효기간 : 2주
    public String createAccessTokenForAndroid(long userId){
        Date now = new Date();
        return Jwts.builder()
                .setHeaderParam("type","jwt")
                .claim("userId",userId)
                .setIssuedAt(now)
                .setExpiration(new Date(System.currentTimeMillis()+1*(1000*60*60*24*30)))
                .signWith(SignatureAlgorithm.HS256, Secret.JWT_ACCESS_SECRET_KEY)
                .compact();
    }
    //refresh-token 유효기간 : 2주
    public String createRefreshToken(long userId){
        Date now = new Date();
        return Jwts.builder()
                .setHeaderParam("type","jwt")
                .claim("userId",userId)
                .setIssuedAt(now)
                .setExpiration(new Date(System.currentTimeMillis()+1*(1000*60*60*24*14)))
                .signWith(SignatureAlgorithm.HS256, Secret.JWT_REFRESH_SECRET_KEY)
                .compact();
    }

    /*
    Header에서 X-ACCESS-TOKEN 으로 JWT 추출
    @return String
     */
    public String getAccessToken(){
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        return request.getHeader("X-ACCESS-TOKEN");
    }

    public String getRefreshToken(){
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        return request.getHeader("REFRESH-TOKEN");
    }

    public long getUserId() throws BaseException {
        //1. JWT 추출
        String accessToken = getAccessToken();
        if(accessToken == null || accessToken.length() == 0){
            throw new BaseException(EMPTY_JWT);
        }

        // 2. JWT parsing
        Jws<Claims> claims;
        try{
            claims = Jwts.parser()
                    .setSigningKey(Secret.JWT_ACCESS_SECRET_KEY)
                    .parseClaimsJws(accessToken);
        } catch (Exception ignored) {
            throw new BaseException(INVALID_JWT);
        }

        // 3. userIdx 추출
        return claims.getBody().get("userId",Long.class);
    }

    public void isValid() throws BaseException{
        String refreshToken = getRefreshToken();
        Jws<Claims> claims;
        try{
            claims = Jwts.parser()
                    .setSigningKey(Secret.JWT_REFRESH_SECRET_KEY)
                    .parseClaimsJws(refreshToken);
        } catch (ExpiredJwtException ignored) {
            throw new BaseException(LOGIN_TIME_OUT_ERROR);
        }
    }

    public long getUserIdByJwt(String jwt) throws BaseException{
        Jws<Claims> claims;
        try{
            claims = Jwts.parser()
                    .setSigningKey(Secret.JWT_ACCESS_SECRET_KEY)
                    .parseClaimsJws(jwt);
        } catch (Exception ignored) {
            throw new BaseException(INVALID_JWT);
        }

        return claims.getBody().get("userId",Long.class);
    }
}
