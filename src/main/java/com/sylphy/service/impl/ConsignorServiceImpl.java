package com.sylphy.service.impl;

import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sylphy.common.RedisCache;
import com.sylphy.common.StringTools;
import com.sylphy.dto.ConsignorChangeInfoDTO;
import com.sylphy.dto.ConsignorLoginDTO;
import com.sylphy.dto.ConsignorRegisterDTO;
import com.sylphy.entity.model.Consignor;
import com.sylphy.entity.model.User;
import com.sylphy.exception.BusinessException;
import com.sylphy.mapper.ConsignorDao;
import com.sylphy.mapper.UserDao;
import com.sylphy.service.ConsignorService;
import com.sylphy.vo.ConsignorLoginVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 货主 Service 实现类
 *
 * @author apple
 * @since 2026/1/3
 */
@Slf4j
@Service
public class ConsignorServiceImpl implements ConsignorService {

    private final ConsignorDao consignorDao;
    private final UserDao userDao;
    private final RedisCache redisCache;

    public ConsignorServiceImpl(ConsignorDao consignorDao, UserDao userDao, RedisCache redisCache) {
        this.consignorDao = consignorDao;
        this.userDao = userDao;
        this.redisCache = redisCache;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void register(ConsignorRegisterDTO registerDTO) {
        // 检查手机号是否已注册
        Consignor existConsignor = selectByPhone(registerDTO.getPhone());
        if (existConsignor != null) {
            throw new BusinessException("该手机号已注册");
        }

        // 检查邮箱是否已注册
        existConsignor = selectByEmail(registerDTO.getEmail());
        if (existConsignor != null) {
            throw new BusinessException("该邮箱已注册");
        }

        // 创建用户 - 使用 MyBatis Plus 的 insert 方法
        User user = new User();
        user.setUsername(StringTools.randomUsername());
        user.setType(1); // 1:货主
        userDao.insert(user);

        // 创建货主
        Consignor consignor = new Consignor();
        consignor.setUserId(user.getUserId());
        consignor.setEmail(registerDTO.getEmail());
        consignor.setPhone(registerDTO.getPhone());
        // 密码加密
        consignor.setPassword(DigestUtil.md5Hex(registerDTO.getPassword()));

        consignorDao.insert(consignor);
        log.info("货主注册成功，consignorId: {}", consignor.getConsignorId());
    }

    @Override
    public ConsignorLoginVO login(ConsignorLoginDTO loginDTO) {
        // 根据账号（手机号或邮箱）查询货主
        Consignor consignor = selectByAccount(loginDTO.getAccount());
        if (consignor == null) {
            throw new BusinessException("账号或密码错误");
        }

        // 验证密码
        String encryptedPassword = DigestUtil.md5Hex(loginDTO.getPassword());
        if (!encryptedPassword.equals(consignor.getPassword())) {
            throw new BusinessException("账号或密码错误");
        }

        // 生成 Token
        String token = StringTools.generateToken();

        // 保存 Token 到 Redis
        redisCache.saveToken(token, consignor.getConsignorId());

        // 缓存货主信息
        redisCache.saveConsignorInfo(consignor);

        // 构建返回对象
        ConsignorLoginVO loginVO = new ConsignorLoginVO();
        loginVO.setToken(token);
        loginVO.setConsignorId(consignor.getConsignorId());
        loginVO.setUserId(consignor.getUserId());
        loginVO.setEmail(consignor.getEmail());
        loginVO.setPhone(consignor.getPhone());

        log.info("货主登录成功，consignorId: {}", consignor.getConsignorId());
        return loginVO;
    }

    @Override
    public void logout(String token) {
        Long consignorId = redisCache.getConsignorIdByToken(token);
        if (consignorId != null) {
            // 删除 Token
            redisCache.deleteToken(token);
            // 删除缓存的货主信息
            redisCache.deleteConsignorInfo(consignorId);
            log.info("货主登出成功，consignorId: {}", consignorId);
        }
    }

    @Override
    public Consignor getConsignorByToken(String token) {
        // 从 Redis 获取货主ID
        Long consignorId = redisCache.getConsignorIdByToken(token);
        if (consignorId == null) {
            throw new BusinessException("Token 已过期，请重新登录");
        }

        // 刷新 Token 过期时间
        redisCache.refreshToken(token);

        // 先从缓存获取
        Consignor consignor = redisCache.getConsignorInfo(consignorId);
        if (consignor != null) {
            // 刷新缓存过期时间
            redisCache.refreshConsignorInfo(consignorId);
            return consignor;
        }

        // 缓存不存在，从数据库查询
        consignor = consignorDao.selectById(consignorId);
        if (consignor == null) {
            throw new BusinessException("用户不存在");
        }

        // 更新缓存
        redisCache.saveConsignorInfo(consignor);
        return consignor;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Consignor updateConsignor(Long consignorId, ConsignorChangeInfoDTO changeInfoDTO) {
        Consignor consignor = consignorDao.selectById(consignorId);
        if (consignor == null) {
            throw new BusinessException("货主不存在");
        }

        // 修改邮箱
        if (StringUtils.hasText(changeInfoDTO.getEmail())) {
            // 检查邮箱是否被其他用户占用
            Consignor exist = consignorDao.selectOne(new LambdaQueryWrapper<Consignor>()
                    .eq(Consignor::getEmail, changeInfoDTO.getEmail())
                    .ne(Consignor::getConsignorId, consignorId));
            if (exist != null) {
                throw new BusinessException("该邮箱已注册");
            }
            consignor.setEmail(changeInfoDTO.getEmail());
        }

        // 修改密码
        if (StringUtils.hasText(changeInfoDTO.getNewPassword())) {
            if (!StringUtils.hasText(changeInfoDTO.getOldPassword())) {
                throw new BusinessException("修改密码需要提供旧密码");
            }
            // 验证旧密码
            String encryptedOldPassword = DigestUtil.md5Hex(changeInfoDTO.getOldPassword());
            if (!encryptedOldPassword.equals(consignor.getPassword())) {
                throw new BusinessException("旧密码错误");
            }
            // 设置新密码
            consignor.setPassword(DigestUtil.md5Hex(changeInfoDTO.getNewPassword()));
        }

        // 更新数据库
        consignorDao.updateById(consignor);

        // 更新缓存
        redisCache.saveConsignorInfo(consignor);

        return consignor;
    }

    /**
     * 根据手机号查询货主
     */
    private Consignor selectByPhone(String phone) {
        return consignorDao.selectOne(new LambdaQueryWrapper<Consignor>()
                .eq(Consignor::getPhone, phone));
    }

    /**
     * 根据邮箱查询货主
     */
    private Consignor selectByEmail(String email) {
        return consignorDao.selectOne(new LambdaQueryWrapper<Consignor>()
                .eq(Consignor::getEmail, email));
    }

    /**
     * 根据账号（手机号或邮箱）查询货主
     */
    private Consignor selectByAccount(String account) {
        return consignorDao.selectOne(new LambdaQueryWrapper<Consignor>()
                .eq(Consignor::getPhone, account)
                .or()
                .eq(Consignor::getEmail, account));
    }
}
