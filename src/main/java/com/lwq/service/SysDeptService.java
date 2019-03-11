package com.lwq.service;

import com.lwq.dao.SysDeptMapper;
import com.lwq.exception.ParamException;
import com.lwq.model.SysDept;
import com.lwq.param.DeptParam;
import com.lwq.util.BeanValidator;
import com.lwq.util.LevelUtil;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;

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

    public void save(DeptParam param){
        BeanValidator.check(param);
        if(checkExist(param.getParentId(),param.getName(),param.getId())){
            throw new ParamException("同一层级下存在相同名称的部门");
        }
        SysDept dept = SysDept.builder()
                .id(param.getId())
                .name(param.getName())
                .seq(param.getSeq())
                .remark(param.getRemark()).build();

        dept.setLevel(LevelUtil.calculateLevel(getLevel(param.getParentId()),param.getParentId()));
        dept.setOperator("system");//TODO
        dept.setOperateIp("127.0.0.1");
        dept.setOperateTime(new Date());

        sysDeptMapper.insertSelective(dept);
    }

    private boolean checkExist(Integer parentId,String deptName,Integer deptId){
//        TODO
        return true;
    }

    private String getLevel(Integer deptId){
        SysDept dept = sysDeptMapper.selectByPrimaryKey(deptId);
        if(dept == null){
            return null;
        }
        return dept.getLevel();
    }
}
