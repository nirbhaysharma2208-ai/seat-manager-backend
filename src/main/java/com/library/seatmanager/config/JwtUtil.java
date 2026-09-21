//package com.library.seatmanager.config;
//
//import io.jsonwebtoken.*;
//import io.jsonwebtoken.security.Keys;
//import org.springframework.stereotype.Component;
//
//import java.security.Key;
//import java.util.Date;
//
//@Component
//public class JwtUtil {
//
//    private final String SECRET = "mySuperSecretKeyForSeatManagerApplication123456";
//
//    private Key getSigningKey() {
//        return Keys.hmacShaKeyFor(SECRET.getBytes());
//    }
//
//    public String generateToken(String username) {
//
//        return Jwts.builder()
//                .setSubject(username)
//                .setIssuedAt(new Date())
//                .setExpiration(new Date(System.currentTimeMillis() + 86400000)) // 1 day
//                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
//                .compact();
//    }
//
//    public String extractUsername(String token) {
//
//        return Jwts.parserBuilder()
//                .setSigningKey(getSigningKey())
//                .build()
//                .parseClaimsJws(token)
//                .getBody()
//                .getSubject();
//    }
//
//    public boolean validateToken(String token) {
//        try {
//            Jwts.parserBuilder()
//                    .setSigningKey(getSigningKey())
//                    .build()
//                    .parseClaimsJws(token);
//            return true;
//        } catch (Exception e) {
//            return false;
//        }
//    }
//}

package com.library.seatmanager.config;


import com.library.seatmanager.entity.Admin;
import com.library.seatmanager.entity.Employee;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    private final String SECRET =
            "mySuperSecretKeyForSeatManagerApplication123456";

    private final long EXPIRATION_TIME =
            86400000; // 24 hours


    private Key getSigningKey() {

        return Keys.hmacShaKeyFor(
                SECRET.getBytes(StandardCharsets.UTF_8)
        );
    }


    // =========================================================
    // ADMIN TOKEN
    // =========================================================

    public String generateAdminToken(Admin admin) {

        return Jwts.builder()

                .setSubject(admin.getPhone())

                .claim("userType", "ADMIN")

                .claim("userId", admin.getId())

                .setIssuedAt(new Date())

                .setExpiration(
                        new Date(
                                System.currentTimeMillis()
                                        + EXPIRATION_TIME
                        )
                )

                .signWith(
                        getSigningKey(),
                        SignatureAlgorithm.HS256
                )

                .compact();
    }


    // =========================================================
    // EMPLOYEE TOKEN
    // =========================================================

    public String generateEmployeeToken(Employee employee) {

        return Jwts.builder()

                .setSubject(employee.getUsername())

                .claim("userType", "EMPLOYEE")

                .claim("userId", employee.getId())

                .claim(
                        "libraryId",
                        employee.getLibrary().getId()
                )

                .claim(
                        "role",
                        employee.getRole().name()
                )

                .setIssuedAt(new Date())

                .setExpiration(
                        new Date(
                                System.currentTimeMillis()
                                        + EXPIRATION_TIME
                        )
                )

                .signWith(
                        getSigningKey(),
                        SignatureAlgorithm.HS256
                )

                .compact();
    }


    // =========================================================
    // EXTRACT USERNAME
    // =========================================================

    public String extractUsername(String token) {

        return getClaims(token)
                .getSubject();
    }


    // =========================================================
    // EXTRACT CLAIMS
    // =========================================================

    public Claims getClaims(String token) {

        return Jwts.parserBuilder()

                .setSigningKey(getSigningKey())

                .build()

                .parseClaimsJws(token)

                .getBody();
    }


    // =========================================================
    // VALIDATE TOKEN
    // =========================================================

    public boolean validateToken(String token) {

        try {

            getClaims(token);

            return true;

        } catch (Exception e) {

            return false;
        }
    }
}