package ru.practicum.main.comment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InputCommentDto {
    @NotBlank
    @Size(min = 3, max = 2000,message = "Text length should be min = 3, max = 2000")
    private String text;
}
