package com.lwq.service;

import com.google.common.collect.Lists;
import com.lwq.common.RequestHolder;
import com.lwq.dao.SysAclMapper;
import com.lwq.dao.SysRoleAclMapper;
import com.lwq.dao.SysRoleUserMapper;
import com.lwq.model.SysAcl;
import com.lwq.model.SysUser;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
        SysUser sysUser = RequestHolder.getCurrentUser();
        if(sysUser.getMail().contains("admin")){
            return true;
        }
        return false;
    }

    public boolean hasUrlAcl(String url){
        if(isSuperAdmin()){
            return true;
        }
        List<SysAcl> aclList = sysAclMapper.getByUrl(url);
        if(CollectionUtils.isEmpty(aclList)){
            return true;
        }
        List<SysAcl> userAclList = getCurrentUserAclList();
        Set<Integer> userAclIdSet = userAclList.stream()
                .map(acl -> acl.getId())
                .collect(Collectors.toSet());

        boolean hasValidAcl = false;
        for(SysAcl acl:aclList){
            if(acl==null || acl.getStatus()!=1){
                continue;
            }
            hasValidAcl = true;
            if(userAclIdSet.contains(acl.getId())){
                return true;
            }
        }
        if(!hasValidAcl){
            return true;
        }
        return false;
    }
}
