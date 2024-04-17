package ru.practicum.yandex.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserShortDto {

    @NotBlank(message = "Name length must be between 2 and 250 characters.")
    @Size(min = 2, max = 250, message = "Name length must be between 2 and 250 characters.")
    private String name;

    @Email(message = "Wrong email format.", regexp = "^(?=(.{1,64}@.{1,255}))([-+%_a-zA-Z0-9]{1,64}(\\.[-+%_a-zA-Z0-9]" +
            "[^.]{0,}){0,})@([a-zA-Z0-9_]{0,63}(\\.[a-zA-Z0-9-]{0,}){0,}[^.](\\.[a-zA-Z]{2,60}){1,4})$")
    @NotBlank(message = "Wrong email format.")
    private String email;
}
