package com.guxian.system.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.guxian.system.dto.SysUserDTO;
import com.guxian.system.entity.*;
import com.guxian.system.mapper.*;
import com.guxian.result.Result;
import com.guxian.system.dto.LoginDTO;
import com.guxian.system.entity.*;
import com.guxian.system.mapper.*;
import com.guxian.system.service.SysUserRoleService;
import com.guxian.system.vo.LoginRespVO;
import com.guxian.system.service.SysUserService;
import com.guxian.util.JwtUtil;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    /** BCrypt 密文格式：$2a$10$ + 53 位 */
    private static final Pattern BCRYPT_PATTERN = Pattern.compile("^\\$2[aby]\\$\\d{2}\\$.{53}$");

    /** 新增用户 / 重置密码使用的默认密码 */
    private static final String DEFAULT_PASSWORD = "123456";

    /**
     * 生成默认密码的 BCrypt 密文。
     * 注意：不要写死哈希常量，历史上写死的那个常量并不对应 "123456"，
     * 会导致"新建用户/重置密码后无法登录"。
     */
    private String encodeDefaultPassword() {
        return new BCryptPasswordEncoder().encode(DEFAULT_PASSWORD);
    }

@Autowired
private JwtUtil jwtUtil;

@Resource
private SysUserRoleMapper userRoleMapper;

@Resource
private SysUserRoleService sysUserRoleService;

@Resource
private SysRoleMapper sysRoleMapper;

@Autowired
private SysRoleMenuMapper sysRoleMenuMapper;

@Autowired
private SysMenuMapper sysMenuMapper;


@Override
    public Result<LoginRespVO> login(LoginDTO dto) {
        // 1. 根据账号查询用户，过滤已删除、禁用账号
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, dto.getUsername());
        wrapper.eq(SysUser::getDelFlag, 0);
        wrapper.eq(SysUser::getStatus, 1);
        SysUser user = getOne(wrapper);

        if (user == null) {
            return Result.fail("账号不存在或已被禁用");
        }

        // 密码校验：兼容历史明文密码与 BCrypt 密文（新增/重置密码写入的是 BCrypt）
        if (!matchesPassword(dto.getPassword(), user.getPassword())) {
            return Result.fail("密码错误");
        }

        // 调用公共模块JwtUtil生成token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRealName(), user.getPostType());

        // 组装返回VO
        LoginRespVO resp = new LoginRespVO();
        resp.setId(user.getId());
        resp.setUsername(user.getUsername());
        resp.setRealName(user.getRealName());
        resp.setToken(token);

        return Result.success(resp);
    }

    /**
     * 密码比对：
     *  - 数据库存的是 BCrypt 密文（$2a$/$2b$/$2y$ 开头）时用 BCrypt 校验；
     *  - 历史数据是明文时退化为明文比对，保证老账号仍可登录。
     */
    private boolean matchesPassword(String rawPassword, String storedPassword) {
        if (rawPassword == null || storedPassword == null) {
            return false;
        }
        String stored = storedPassword.trim();
        if (BCRYPT_PATTERN.matcher(stored).matches()) {
            try {
                return new BCryptPasswordEncoder().matches(rawPassword, stored);
            } catch (IllegalArgumentException e) {
                // 密文格式异常时退化为明文比对，避免直接 500
                return stored.equals(rawPassword);
            }
        }
        return stored.equals(rawPassword);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveUser(SysUserDTO dto) {
        SysUser user = new SysUser();
        // 新增时，若同账号记录已被逻辑删除（unique(uk_username) 仍占用），先复活该行再更新，
        // 避免"删除用户后用同一账号重新新增"直接抛 Duplicate entry 500
        Long reusedId = dto.getId();
        if (reusedId == null && dto.getUsername() != null) {
            SysUser deleted = baseMapper.selectDeletedByUsername(dto.getUsername());
            if (deleted != null) {
                baseMapper.restoreById(deleted.getId());
                reusedId = deleted.getId();
            }
        }
        user.setId(reusedId);
        user.setUsername(dto.getUsername());
        user.setRealName(dto.getRealName());
        user.setPhone(dto.getPhone());
        user.setPostType(dto.getPostType());
        user.setStatus(dto.getStatus());

        // 新增用户，设置初始密码 BCrypt加密 123456
        if (reusedId == null) {
            user.setPassword(encodeDefaultPassword());
            this.save(user);
        } else {
            // 编辑（含复活）：读取数据库原有密码，防止被置空
            SysUser oldUser = this.getById(reusedId);
            user.setPassword(oldUser == null || oldUser.getPassword() == null
                    ? encodeDefaultPassword()
                    : oldUser.getPassword());
            this.updateById(user);
        }

        // 清理旧角色，再插入新选中角色
        LambdaQueryWrapper<SysUserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUserRole::getUserId, user.getId());
        userRoleMapper.delete(wrapper);

        List<Long> roleIdList = dto.getRoleIdList();
        if (CollUtil.isNotEmpty(roleIdList)) {
            List<SysUserRole> urList = new ArrayList<>();
            for (Long rid : roleIdList) {
                SysUserRole ur = new SysUserRole();
                ur.setUserId(user.getId());
                ur.setRoleId(rid);
                urList.add(ur);
            }
            sysUserRoleService.saveBatch(urList);
        }
    }


    @Override
    public void resetPwd(Long userId) {
        SysUser user = this.getById(userId);
        // 重置密码 123456
        user.setPassword(encodeDefaultPassword());
        this.updateById(user);
    }

    @Override
    public List<Long> getRoleIdsByUserId(Long userId) {
        LambdaQueryWrapper<SysUserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUserRole::getUserId, userId);
        List<SysUserRole> urList = userRoleMapper.selectList(wrapper);
        return urList.stream().map(SysUserRole::getRoleId).toList();
    }


    @Override
    public IPage<SysUserDTO> page(IPage<SysUser> page, LambdaQueryWrapper<SysUser> wrapper) {
        // 1. 查询用户分页
        IPage<SysUser> userPage = baseMapper.selectPage(page, wrapper);
        List<SysUser> userRecords = userPage.getRecords();
        if(CollUtil.isEmpty(userRecords)){
            return new Page<>();
        }

        // 拿到所有用户id
        List<Long> userIdList = userRecords.stream().map(SysUser::getId).collect(Collectors.toList());

        // 2. 根据用户id批量查询 user_role 中间表
        LambdaQueryWrapper<SysUserRole> urWrapper = Wrappers.lambdaQuery();
        urWrapper.in(SysUserRole::getUserId, userIdList);
        List<SysUserRole> userRoleList = userRoleMapper.selectList(urWrapper);

        // 取出所有角色id
        List<Long> roleIds = userRoleList.stream().map(SysUserRole::getRoleId).collect(Collectors.toList());
        List<SysRole> roleList = sysRoleMapper.selectBatchIds(roleIds);

        // 角色id -> 角色名 map
        Map<Long,String> roleIdNameMap = roleList.stream()
                .collect(Collectors.toMap(SysRole::getId, SysRole::getRoleName));

        // 用户id -> 该用户所有角色ID
        Map<Long, List<Long>> userIdToRoleIdMap = userRoleList.stream()
                .collect(Collectors.groupingBy(SysUserRole::getUserId,
                        Collectors.mapping(SysUserRole::getRoleId, Collectors.toList())));

        // ======================【新增开始：菜单相关查询】======================
        // 批量查询角色菜单中间表 sys_role_menu
        LambdaQueryWrapper<SysRoleMenu> rmWrapper = Wrappers.lambdaQuery();
        rmWrapper.in(SysRoleMenu::getRoleId, roleIds);
        List<SysRoleMenu> roleMenuList = sysRoleMenuMapper.selectList(rmWrapper);

        // 角色ID -> 菜单ID列表
        Map<Long, List<Long>> roleIdToMenuIdMap = roleMenuList.stream()
                .collect(Collectors.groupingBy(SysRoleMenu::getRoleId,
                        Collectors.mapping(SysRoleMenu::getMenuId, Collectors.toList())));

        // 提取所有菜单ID，去重
        List<Long> allMenuIds = roleMenuList.stream()
                .map(SysRoleMenu::getMenuId)
                .distinct()
                .collect(Collectors.toList());

        // 查询菜单，构建菜单ID -> 菜单名称映射
        Map<Long, String> menuIdToNameMap = new HashMap<>();
        if (!CollUtil.isEmpty(allMenuIds)) {
            LambdaQueryWrapper<SysMenu> menuWrapper = Wrappers.lambdaQuery();
            menuWrapper.in(SysMenu::getId, allMenuIds);
            List<SysMenu> menuList = sysMenuMapper.selectList(menuWrapper);
            menuIdToNameMap = menuList.stream()
                    .collect(Collectors.toMap(SysMenu::getId, SysMenu::getMenuName));
        }

        // 用户ID -> 该用户所有菜单名称（HashSet自动去重）
        Map<Long, Set<String>> userIdToMenuNameMap = new HashMap<>();
        for (SysUserRole userRole : userRoleList) {
            Long uid = userRole.getUserId();
            Long rid = userRole.getRoleId();
            List<Long> midList = roleIdToMenuIdMap.get(rid);
            if (CollUtil.isEmpty(midList)) {
                continue;
            }
            for (Long mid : midList) {
                String menuName = menuIdToNameMap.get(mid);
                if (menuName != null) {
                    userIdToMenuNameMap.computeIfAbsent(uid, k -> new HashSet<>()).add(menuName);
                }
            }
        }
        // ======================【新增结束：菜单相关查询】======================

        // 3. 组装DTO
        List<SysUserDTO> dtoList = userRecords.stream().map(user -> {
            SysUserDTO dto = BeanUtil.copyProperties(user, SysUserDTO.class);
            List<Long> rIds = userIdToRoleIdMap.get(user.getId());
            if(rIds != null){
                List<String> rNameList = rIds.stream().map(rid-> roleIdNameMap.get(rid)).collect(Collectors.toList());
                dto.setRoleNameList(rNameList);
                // 逗号拼接，给表格展示
                dto.setRoleNames(String.join(",",rNameList));
                dto.setRoleIdList(rIds);
            }

            // ======================【新增：给menuNames赋值】======================
            Set<String> menuNameSet = userIdToMenuNameMap.get(user.getId());
            if (menuNameSet != null && menuNameSet.size() > 0) {
                dto.setMenuNames(String.join(",", menuNameSet));
            } else {
                dto.setMenuNames("");
            }
            // ==================================================================
            return dto;
        }).collect(Collectors.toList());

        // 组装分页返回
        Page<SysUserDTO> dtoPage = new Page<>();
        BeanUtil.copyProperties(userPage, dtoPage, "records");
        dtoPage.setRecords(dtoList);
        return dtoPage;
    }


}
