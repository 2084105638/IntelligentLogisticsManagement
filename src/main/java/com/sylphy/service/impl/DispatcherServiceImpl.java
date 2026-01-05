package com.sylphy.service.impl;

import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sylphy.common.RedisCache;
import com.sylphy.common.StringTools;
import com.sylphy.dto.DispatcherLoginDTO;
import com.sylphy.entity.model.Dispatcher;
import com.sylphy.entity.model.User;
import com.sylphy.exception.BusinessException;
import com.sylphy.mapper.DispatcherDao;
import com.sylphy.mapper.UserDao;
import com.sylphy.service.DispatcherService;
import com.sylphy.vo.DispatcherLoginVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class DispatcherServiceImpl implements DispatcherService {
    
    private final DispatcherDao dispatcherDao;
    private final UserDao userDao;
    private final RedisCache redisCache;
    
    public DispatcherServiceImpl(DispatcherDao dispatcherDao, UserDao userDao, RedisCache redisCache) {
        this.dispatcherDao = dispatcherDao;
        this.userDao = userDao;
        this.redisCache = redisCache;
    }
    
    @Override
    public DispatcherLoginVO login(DispatcherLoginDTO loginDTO) {
        User userDB = userDao.selectOne(new LambdaQueryWrapper<User>()
                                                .eq(User::getUsername, loginDTO.getUsername()));
        if (userDB == null) {
            throw new BusinessException("账号或密码错误");
        }
        Dispatcher dispatcher = dispatcherDao.selectOne(new LambdaQueryWrapper<Dispatcher>()
                                                                .eq(Dispatcher::getUserId, userDB.getUserId()));
        if (dispatcher == null) {
            throw new BusinessException("账号或密码错误");
        }

//        String encryptedPassword = DigestUtil.md5Hex(loginDTO.getPassword());
        if (!loginDTO.getPassword().equals(dispatcher.getPassword())) {
            throw new BusinessException("账号或密码错误");
        }
        
        String token = StringTools.generateToken();
        redisCache.saveToken(token, dispatcher.getDispatcherId());
        redisCache.saveDispatcherInfo(dispatcher);
        
        User user = userDao.selectById(dispatcher.getUserId());
        
        DispatcherLoginVO loginVO = new DispatcherLoginVO();
        loginVO.setToken(token);
        loginVO.setDispatcherId(dispatcher.getDispatcherId());
        loginVO.setUserId(dispatcher.getUserId());
        loginVO.setUsername(user.getUsername());
        loginVO.setEmail(dispatcher.getEmail());
        loginVO.setPhone(dispatcher.getPhone());
        
        log.info("调度员登录成功，dispatcherId: {}", dispatcher.getDispatcherId());
        return loginVO;
    }
    
    @Override
    public void logout(String token) {
        Long dispatcherId = redisCache.getDispatcherIdByToken(token);
        if (dispatcherId != null) {
            redisCache.deleteToken(token);
            redisCache.deleteDispatcherInfo(dispatcherId);
            log.info("调度员登出成功，dispatcherId: {}", dispatcherId);
        }
    }
    
    @Override
    public Dispatcher getDispatcherByToken(String token) {
        Long dispatcherId = redisCache.getDispatcherIdByToken(token);
        if (dispatcherId == null) {
            throw new BusinessException("Token 已过期，请重新登录");
        }
        
        redisCache.refreshToken(token);
        
        Dispatcher dispatcher = redisCache.getDispatcherInfo(dispatcherId);
        if (dispatcher != null) {
            redisCache.refreshDispatcherInfo(dispatcherId);
            return dispatcher;
        }
        
        dispatcher = dispatcherDao.selectById(dispatcherId);
        if (dispatcher == null) {
            throw new BusinessException("用户不存在");
        }
        
        redisCache.saveDispatcherInfo(dispatcher);
        return dispatcher;
    }
}
