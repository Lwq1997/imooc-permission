package com.lwq.service;

import com.google.common.base.Preconditions;
import com.lwq.common.RequestHolder;
import com.lwq.dao.SysAclMapper;
import com.lwq.dao.SysAclModuleMapper;
import com.lwq.exception.ParamException;
import com.lwq.model.SysAclModule;
import com.lwq.model.SysDept;
import com.lwq.param.AclModuleParam;
import com.lwq.util.BeanValidator;
import com.lwq.util.IpUtil;
import com.lwq.util.LevelUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * @Author: Lwq
 * @Date: 2019/3/14 19:27
 * @Version 1.0
 * @Describe
 */
@Service
public class SysAclModuleService {

    @Resource
    private SysAclModuleMapper sysAclModuleMapper;

    @Resource
    private SysAclMapper sysAclMapper;

    public void save(AclModuleParam param){
        BeanValidator.check(param);
        if(checkExist(param.getParentId(),param.getName(),param.getId())){
            throw new ParamException("同一层级下存在相同名称的权限模块");
        }
        SysAclModule aclModule = SysAclModule.builder()
                .name(param.getName())
                .parentId(param.getParentId())
                .seq(param.getSeq())
                .status(param.getStatus())
                .remark(param.getRemark())
                .build();

        aclModule.setLevel(LevelUtil.calculateLevel(getLevel(param.getParentId()),param.getParentId()));
        aclModule.setOperator(RequestHolder.getCurrentUser().getUsername());//TODO
        aclModule.setOperateIp(IpUtil.getRemoteIp(RequestHolder.getCurrentRequest()));
        aclModule.setOperateTime(new Date());

        sysAclModuleMapper.insertSelective(aclModule);
    }

    public void update(AclModuleParam param){
        BeanValidator.check(param);
        if(checkExist(param.getParentId(),param.getName(),param.getId())){
            throw new ParamException("同一层级下存在相同名称的权限模块");
        }

        SysAclModule beforeAclModule = sysAclModuleMapper.selectByPrimaryKey(param.getId());
        Preconditions.checkNotNull(beforeAclModule,"待更新的权限模块不存在");

        SysAclModule afterAclModule = SysAclModule.builder()
                .name(param.getName())
                .parentId(param.getParentId())
                .seq(param.getSeq())
                .status(param.getStatus())
                .remark(param.getRemark())
                .id(param.getId())
                .build();

        afterAclModule.setLevel(LevelUtil.calculateLevel(getLevel(param.getParentId()),param.getParentId()));
        afterAclModule.setOperator(RequestHolder.getCurrentUser().getUsername());//TODO
        afterAclModule.setOperateIp(IpUtil.getRemoteIp(RequestHolder.getCurrentRequest()));
        afterAclModule.setOperateTime(new Date());

        updateWithChild(beforeAclModule,afterAclModule);
    }

    @Transactional
    public void updateWithChild(SysAclModule before,SysAclModule after){
        String newLevelPerfix = after.getLevel();
        String oldLevelPerfix = before.getLevel();

        if(StringUtils.equals(oldLevelPerfix,newLevelPerfix)){
            List<SysAclModule> sysAclModuleList = sysAclModuleMapper.getChildAclModuleListByLevel(oldLevelPerfix);
            if(CollectionUtils.isNotEmpty(sysAclModuleList)){
                for(SysAclModule aclModule:sysAclModuleList){
                    String level = aclModule.getLevel();
                    if(level.indexOf(oldLevelPerfix)==0){
                        level = newLevelPerfix+level.substring(oldLevelPerfix.length());
                        aclModule.setLevel(level);
                    }
                }
                sysAclModuleMapper.batchUpdateLevel(sysAclModuleList);
            }
        }
        sysAclModuleMapper.updateByPrimaryKey(after);
    }

    private boolean checkExist(Integer parentId,String aclModuleName,Integer aclModuleId){
        return sysAclModuleMapper.countByNameAndParentId(parentId,aclModuleName,aclModuleId) > 0;
    }

    private String getLevel(Integer aclModuleId){
        SysAclModule aclModule = sysAclModuleMapper.selectByPrimaryKey(aclModuleId);
        if(aclModule == null){
            return null;
        }
        return aclModule.getLevel();
    }

    public void delete(int aclModuleId) {
        SysAclModule aclModule = sysAclModuleMapper.selectByPrimaryKey(aclModuleId);
        Preconditions.checkNotNull(aclModule,"当前权限模块不存在");
        if(sysAclModuleMapper.countByParentId(aclModule.getId())>0){
            throw new ParamException("当前权限模块下有子模块，无法删除");
        }
        if(sysAclMapper.countByAclModuleId(aclModule.getId())>0){
            throw new ParamException("当前权限模块下有权限点，无法删除");
        }
        sysAclModuleMapper.deleteByPrimaryKey(aclModuleId);
    }
}
