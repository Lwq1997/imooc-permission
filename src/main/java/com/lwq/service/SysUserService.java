package com.lwq.service;

import com.google.common.base.Preconditions;
import com.lwq.beans.PageQuery;
import com.lwq.beans.PageResult;
import com.lwq.common.RequestHolder;
import com.lwq.dao.SysUserMapper;
import com.lwq.exception.ParamException;
import com.lwq.model.SysUser;
import com.lwq.param.UserParam;
import com.lwq.util.BeanValidator;
import com.lwq.util.IpUtil;
import com.lwq.util.MD5Util;
import com.lwq.util.PasswordUtil;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * @Author: Lwq
 * @Date: 2019/3/14 13:40
 * @Version 1.0
 * @Describe
 */
@Service
public class SysUserService {

    @Resource
    private SysUserMapper sysUserMapper;

    @Resource
    private SysLogService sysLogService;

    public void save(UserParam param){
        BeanValidator.check(param);

        if(checkTelephoneExist(param.getTelephone(),param.getId())){
            throw new ParamException("电话已被占用");
        }
        if(checkEmailExist(param.getMail(),param.getId())){
            throw new ParamException("邮箱已被占用");
        }

        String password = PasswordUtil.randomPassword();
        password = "12345678";
        String encryptedPassword = MD5Util.encrypt(password);
        SysUser user = SysUser.builder()
                .username(param.getUsername())
                .telephone(param.getTelephone())
                .mail(param.getMail())
                .password(encryptedPassword)
                .deptId(param.getDeptId())
                .status(param.getStatus())
                .remark(param.getRemark()).build();

        user.setOperator(RequestHolder.getCurrentUser().getUsername());
        user.setOperateIp(IpUtil.getRemoteIp(RequestHolder.getCurrentRequest()));
        user.setOperateTime(new Date());

        //TODO:sendEmail

        sysUserMapper.insertSelective(user);
        sysLogService.saveUserLog(null,user);
    }

    public void update(UserParam param){
        BeanValidator.check(param);
        if(checkTelephoneExist(param.getTelephone(),param.getId())){
            throw new ParamException("电话已被占用");
        }
        if(checkEmailExist(param.getMail(),param.getId())){
            throw new ParamException("邮箱已被占用");
        }

        SysUser beforeUser = sysUserMapper.selectByPrimaryKey(param.getId());
        Preconditions.checkNotNull(beforeUser,"待更新的用户不存在");

        SysUser afterUser = SysUser.builder()
                .id(param.getId())
                .username(param.getUsername())
                .telephone(param.getTelephone())
                .mail(param.getMail())
                .password(beforeUser.getPassword())
                .deptId(param.getDeptId())
                .status(param.getStatus())
                .remark(param.getRemark()).build();

        afterUser.setOperator(RequestHolder.getCurrentUser().getUsername());//TODO
        afterUser.setOperateIp(IpUtil.getRemoteIp(RequestHolder.getCurrentRequest()));
        afterUser.setOperateTime(new Date());

        sysUserMapper.updateByPrimaryKeySelective(afterUser);
        sysLogService.saveUserLog(beforeUser,afterUser);
    }


    public boolean checkEmailExist(String mail,Integer userId){
        return sysUserMapper.countByMail(mail,userId)>0;
    }

    public boolean checkTelephoneExist(String telephone,Integer userId){
        return sysUserMapper.countByTelephone(telephone,userId)>0;
    }

    public SysUser findByKeyword(String keyword){
        return sysUserMapper.findByKeyword(keyword);
    }

    public PageResult<SysUser> getPageByDeptId(int deptId, PageQuery page) {
        BeanValidator.check(page);
        int count = sysUserMapper.countByDeptId(deptId);
        if (count > 0) {
            List<SysUser> list = sysUserMapper.getPageByDeptId(deptId, page);
            return PageResult.<SysUser>builder().total(count).data(list).build();
        }
        return PageResult.<SysUser>builder().build();
    }

    public List<SysUser> getAll() {
        return sysUserMapper.getAll();
    }
}
