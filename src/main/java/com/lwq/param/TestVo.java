package com.lwq.param;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @Author: Lwq
 * @Date: 2019/3/11 19:29
 * @Version 1.0
 * @Describe
 */
@Getter
@Setter
public class TestVo {

    @NotBlank
    private String msg;

    @NotNull
    @Max(value = 10,message = "id不能大于10")
    @Min(value = 0,message = "id不能小于0")
    private Integer id;

    @NotEmpty
    private List<String> str;
}
