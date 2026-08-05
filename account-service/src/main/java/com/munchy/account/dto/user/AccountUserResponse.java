package com.munchy.account.dto.user;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class AccountUserResponse {
    private UUID id;
    private UUID identityId;
    private String email;
    private String name;
    private String phoneNumber;
    private String pictureUrl;
    private List<String> roles = new ArrayList<>();
}
