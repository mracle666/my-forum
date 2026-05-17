package com.myforum.security;

import lombok.Data;

@Data
public class JwtClaims {
    private Long userId;
    private String username;
    private Integer role;
}
