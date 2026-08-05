package com.munchy.account.dto.user;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UpdateUserRequest {
    @Size(min = 1, max = 150)
    private String name;

    @Size(max = 30)
    private String phoneNumber;
}
