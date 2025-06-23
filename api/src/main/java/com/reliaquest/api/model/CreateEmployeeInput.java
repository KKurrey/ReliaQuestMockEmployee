package com.reliaquest.api.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateEmployeeInput {

    @NotBlank
    private String name;

    @NotNull @Min(1)
    private Integer salary;

    @NotNull @Min(16)
    @Max(75)
    private Integer age;

    @NotBlank
    private String title;
}
