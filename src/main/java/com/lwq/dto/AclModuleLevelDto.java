package com.lwq.dto;

import com.google.common.collect.Lists;
import com.lwq.model.SysAclModule;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.beans.BeanUtils;

import java.util.List;

/**
 * @Author: Lwq
 * @Date: 2019/3/14 20:04
 * @Version 1.0
 * @Describe
 */
@Getter
@Setter
@ToString
public class AclModuleLevelDto extends SysAclModule {

    private List<AclModuleLevelDto> aclModuleList = Lists.newArrayList();

    private List<AclDto> aclList = Lists.newArrayList();
    /**
     * 把传入的的SysAclModule对象复制到AclModuleLevelDto
     * @param aclModule
     * @return
     */
    public static AclModuleLevelDto adapt(SysAclModule aclModule){
        AclModuleLevelDto dto = new AclModuleLevelDto();
        BeanUtils.copyProperties(aclModule,dto);
        return dto;
    }
}
