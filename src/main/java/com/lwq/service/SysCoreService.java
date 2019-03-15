package com.lwq.service;

import com.google.common.collect.Lists;
import com.lwq.common.RequestHolder;
import com.lwq.dao.SysAclMapper;
import com.lwq.dao.SysRoleAclMapper;
import com.lwq.dao.SysRoleUserMapper;
import com.lwq.model.SysAcl;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Author: Lwq
 * @Date: 2019/3/15 16:15
 * @Version 1.0
 * @Describe
 */
@Service
public class SysCoreService {

    @Resource
    private SysAclMapper sysAclMapper;

    @Resource
    private SysRoleUserMapper sysRoleUserMapper;

    @Resource
    private SysRoleAclMapper sysRoleAclMapper;

    public List<SysAcl> getCurrentUserAclList(){
        int userId = RequestHolder.getCurrentUser().getId();
        return getUserAclList(userId);
    }

    public List<SysAcl> getRoleAclList(int roleId){
        List<Integer> roleIdList = sysRoleAclMapper.getAclIdListByRoleIdList(Lists.newArrayList(roleId));
        if(CollectionUtils.isEmpty(roleIdList)){
            return Lists.newArrayList();
        }
        return sysAclMapper.getByIdList(roleIdList);
    }

    public List<SysAcl> getUserAclList(int userId){
        if(isSuperAdmin()){
            return sysAclMapper.getAll();
        }
        List<Integer> userRoleIdList = sysRoleUserMapper.getRoleIdListByUserId(userId);
        if(CollectionUtils.isEmpty(userRoleIdList)){
            return Lists.newArrayList();
        }

        List<Integer> roleAclIdList = sysRoleAclMapper.getAclIdListByRoleIdList(userRoleIdList);
        if(CollectionUtils.isEmpty(roleAclIdList)){
            return Lists.newArrayList();
        }

        return sysAclMapper.getByIdList(roleAclIdList);
    }

    public boolean isSuperAdmin(){
        return true;
    }
}
