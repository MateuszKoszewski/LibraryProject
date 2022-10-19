package com.mateusz.library.constants;


public enum Role {

    ROLE_USER("ROLE_USER", Authority.USER_AUTHORITIES),
    ROLE_ADMIN("ROLE_ADMIN", Authority.ADMIN_AUTHORITIES),
    ROLE_SUPER_ADMIN("ROLE_SUPER_ADMIN", Authority.SUPER_ADMIN_AUTHORITIES);

    private String name;
    private String[] authorities;

    Role (String name, String... authorities) {
        this.name = this.toString();
        this.authorities = authorities;
    }

    public String[] getAuthorities() {
        return authorities;
    }

    public String getName() {
        return name;
    }
}
