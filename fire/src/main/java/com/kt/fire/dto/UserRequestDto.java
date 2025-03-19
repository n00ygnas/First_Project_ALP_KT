package com.kt.fire.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class UserRequestDto {
    
    @Getter
    @Setter
    public static class Register {
        private String email;
        private String password;
        private String name;
        private List<String> districtIds;
    }

    @Getter
    @Setter
    public static class Login {
        private String email;
        private String password;
    }
} 