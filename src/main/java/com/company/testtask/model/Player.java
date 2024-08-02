package com.company.testtask.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class Player {

    @NotNull
    private String nickname;

    @Min(1)
    @Max(10)
    private int number;

    @NotNull
    @Min(1)
    private BigDecimal bet;

    @JsonIgnore
    private BigDecimal payout;
    @JsonIgnore
    private boolean won;
}
