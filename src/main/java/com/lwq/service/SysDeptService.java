package com.lwq.service;

import com.google.common.base.Preconditions;
import com.lwq.common.RequestHolder;
import com.lwq.dao.SysDeptMapper;
import com.lwq.dao.SysUserMapper;
import com.lwq.exception.ParamException;
import com.lwq.model.SysDept;
import com.lwq.param.DeptParam;
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
 * @Date: 2019/3/11 22:05
 * @Version 1.0
 * @Describe
 */
@Service
public class SysDeptService {

    @Resource
    private SysDeptMapper sysDeptMapper;

    @Resource
    private SysUserMapper sysUserMapper;

    public void save(DeptParam param){
        BeanValidator.check(param);
        if(checkExist(param.getParentId(),param.getName(),param.getId())){
            throw new ParamException("同一层级下存在相同名称的部门");
        }
        SysDept dept = SysDept.builder()
                .name(param.getName())
                .parentId(param.getParentId())
                .seq(param.getSeq())
                .remark(param.getRemark()).build();

        dept.setLevel(LevelUtil.calculateLevel(getLevel(param.getParentId()),param.getParentId()));
        dept.setOperator(RequestHolder.getCurrentUser().getUsername());//TODO
        dept.setOperateIp(IpUtil.getRemoteIp(RequestHolder.getCurrentRequest()));
        dept.setOperateTime(new Date());

        sysDeptMapper.insertSelective(dept);
    }

    public void update(DeptParam param){
        BeanValidator.check(param);
        if(checkExist(param.getParentId(),param.getName(),param.getId())){
            throw new ParamException("同一层级下存在相同名称的部门");
        }

        SysDept beforeDept = sysDeptMapper.selectByPrimaryKey(param.getId());
        Preconditions.checkNotNull(beforeDept,"待更新的部门不存在");

        SysDept afterDept = SysDept.builder()
                .id(param.getId())
                .name(param.getName())
                .parentId(param.getParentId())
                .seq(param.getSeq())
                .remark(param.getRemark()).build();

        afterDept.setLevel(LevelUtil.calculateLevel(getLevel(param.getParentId()),param.getParentId()));
        afterDept.setOperator(RequestHolder.getCurrentUser().getUsername());//TODO
        afterDept.setOperateIp(IpUtil.getRemoteIp(RequestHolder.getCurrentRequest()));
        afterDept.setOperateTime(new Date());

        updateWithChild(beforeDept,afterDept);
    }

    @Transactional
    public void updateWithChild(SysDept before, SysDept after){

        String newLevelPerfix = after.getLevel();
        String oldLevelPerfix = before.getLevel();

        if(StringUtils.equals(oldLevelPerfix,newLevelPerfix)){
            List<SysDept> deptList = sysDeptMapper.getChildDeptListByLevel(oldLevelPerfix);
            if(CollectionUtils.isNotEmpty(deptList)){
                for(SysDept dept:deptList){
                    String level = dept.getLevel();
                    if(level.indexOf(oldLevelPerfix)==0){
                        level = newLevelPerfix+level.substring(oldLevelPerfix.length());
                        dept.setLevel(level);
                    }
                }
                sysDeptMapper.batchUpdateLevel(deptList);
            }
        }
        sysDeptMapper.updateByPrimaryKey(after);
    }

    private boolean checkExist(Integer parentId,String deptName,Integer deptId){
        return sysDeptMapper.countByNameAndParentId(parentId,deptName,deptId) > 0;
    }

    private String getLevel(Integer deptId){
        SysDept dept = sysDeptMapper.selectByPrimaryKey(deptId);
        if(dept == null){
            return null;
        }
        return dept.getLevel();
    }


    public void delete(int deptId) {
        SysDept dept = sysDeptMapper.selectByPrimaryKey(deptId);
        Preconditions.checkNotNull(dept,"待删除的部门不存在");
        if(sysDeptMapper.countByParentId(dept.getId())>0){
            throw new ParamException("当前部门下面有子部门,无法删除");
        }
        if(sysUserMapper.countByDeptId(deptId)>0){
            throw new ParamException("当前部门下面由用户,无法删除");
        }
        sysDeptMapper.deleteByPrimaryKey(deptId);
    }
}
