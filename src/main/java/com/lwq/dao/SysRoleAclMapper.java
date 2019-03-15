package com.lwq.dao;

import com.lwq.model.SysRoleAcl;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SysRoleAclMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(SysRoleAcl record);

    int insertSelective(SysRoleAcl record);

    SysRoleAcl selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(SysRoleAcl record);

    int updateByPrimaryKey(SysRoleAcl record);

    List<Integer> getAclIdListByRoleIdList(@Param("roleIdList") List<Integer> userRoleIdList);

    void deleteByRoleId(@Param("roleId") Integer roleId);

    void batchInsert(@Param("roleAclList") List<SysRoleAcl> roleAclList);
}