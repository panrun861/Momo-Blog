package xyz.kuailemao.service.impl;

import cn.hutool.core.lang.TypeReference;
import cn.hutool.core.thread.NamedThreadFactory;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Service;
import xyz.kuailemao.constants.ThirdPartyInterfaceConst;
import xyz.kuailemao.domain.dto.IpResult;
import xyz.kuailemao.domain.entity.BlackList;
import xyz.kuailemao.domain.entity.Log;
import xyz.kuailemao.domain.entity.LoginLog;
import xyz.kuailemao.domain.entity.User;
import xyz.kuailemao.domain.ip.IpDetail;
import xyz.kuailemao.handler.GlobalUncaughtExceptionHandler;
import xyz.kuailemao.mapper.BlackListMapper;
import xyz.kuailemao.mapper.LogMapper;
import xyz.kuailemao.mapper.LoginLogMapper;
import xyz.kuailemao.mapper.UserMapper;
import xyz.kuailemao.service.IpService;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author kuailemao
 * @since 2024/9/25 上午11:06
 * IP 处理类 - 优化版（增加超时控制与内网过滤）
 */
@Slf4j
@Service
public class IpServiceImpl implements IpService, DisposableBean {

    // 优化：增加核心线程数，防止单个请求卡死整个 IP 刷新队列
    private static final ExecutorService EXECUTOR = new ThreadPoolExecutor(4, 8,
            60L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(1000),
            new NamedThreadFactory("refresh-ipDetail", null, false,
                    GlobalUncaughtExceptionHandler.getInstance()));

    @Resource
    private BlackListMapper blackListMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private LoginLogMapper loginLogMapper;

    @Resource
    private LogMapper logMapper;

    /**
     * 判断是否为内网或回环地址，避免无效的外部接口调用
     */
    private boolean isInternalIp(String ip) {
        return StrUtil.isBlank(ip) || "127.0.0.1".equals(ip) || "localhost".equals(ip) || ip.startsWith("172.") || ip.startsWith("192.") || ip.startsWith("0:0:0");
    }

    @Override
    public void refreshIpDetailAsyncByBid(Long bid) {
        EXECUTOR.execute(() -> {
            BlackList blackList = blackListMapper.selectById(bid);
            if (Objects.isNull(blackList)) return;
            String ip = blackList.getIpInfo().getCreateIp();

            if (isInternalIp(ip)) return;

            IpDetail ipDetail = TryGetIpDetailOrNullTreeTimes(ip);
            if (Objects.nonNull(ipDetail)) {
                blackList.getIpInfo().setIpDetail(ipDetail);
                blackListMapper.updateById(blackList);
            }
        });
    }

    @Override
    public void refreshIpDetailAsyncByUidAndRegister(Long uid) {
        EXECUTOR.execute(() -> {
            User user = userMapper.selectById(uid);
            if (Objects.isNull(user)) return;
            String ip = user.getRegisterIp();

            if (isInternalIp(ip)) {
                user.setRegisterAddress("内网/本地");
                userMapper.updateById(user);
                return;
            }

            IpDetail ipDetail = TryGetIpDetailOrNullTreeTimes(ip);
            if (Objects.nonNull(ipDetail)) {
                user.setRegisterAddress(buildAddr(ipDetail.getRegion(), ipDetail.getCity(), ipDetail.getCountry()));
            } else {
                user.setRegisterAddress("未知");
                log.error("register get ip detail fail ip:{},uid:{}", ip, uid);

            }
            userMapper.updateById(user);
        });
    }

    /**
     * 异步刷新登录ip详情获取
     *
     * @param uid 用户id
     */
    @Override
    public void refreshIpDetailAsyncByUidAndLogin(Long uid) {
        EXECUTOR.execute(() -> {
            User user = userMapper.selectById(uid);
            if (Objects.isNull(user)) {
                return;
            }
            String ip = user.getLoginIp();
            if (StrUtil.isBlank(ip)) {
                return;
            }
            IpDetail ipDetail = TryGetIpDetailOrNullTreeTimes(ip);
            if (Objects.nonNull(ipDetail)) {
                user.setLoginAddress(buildAddr(ipDetail.getRegion(), ipDetail.getCity(), ipDetail.getCountry()));
            } else {
                user.setRegisterAddress("未知");
                log.error("login get ip detail fail ip:{},uid:{}", ip, uid);
            }
            userMapper.updateById(user);
        });
    }

    /**
     * 异步刷新登录日志ip详情获取
     *
     * @param loginLogId 登录日志id
     */
    @Override
    public void refreshIpDetailAsyncByLogIdAndLogin(Long loginLogId) {
        EXECUTOR.execute(() -> {
            LoginLog loginLog = loginLogMapper.selectById(loginLogId);
            if (Objects.isNull(loginLog)) {
                return;
            }
            String ip = loginLog.getIp();
            if (StrUtil.isBlank(ip)) {
                return;
            }
            IpDetail ipDetail = TryGetIpDetailOrNullTreeTimes(ip);
            if (Objects.nonNull(ipDetail)) {
                loginLog.setAddress(buildAddr(ipDetail.getRegion(), ipDetail.getCity(), ipDetail.getCountry()));
            } else {
                loginLog.setAddress("未知");
            }
            loginLogMapper.updateById(loginLog);
        });
    }

    @Override
    public void refreshIpDetailAsyncByLogId(Long logId) {
        EXECUTOR.execute(() -> {
            Log logEntity = logMapper.selectById(logId);
            if (Objects.isNull(logEntity)) return;
            String ip = logEntity.getIp();

            if (isInternalIp(ip)) {
                logEntity.setAddress("内网/本地");
                logMapper.updateById(logEntity);
                return;
            }

            IpDetail ipDetail = TryGetIpDetailOrNullTreeTimes(ip);
            if (Objects.nonNull(ipDetail)) {
                logEntity.setAddress(buildAddr(ipDetail.getRegion(), ipDetail.getCity(), ipDetail.getCountry()));
            } else {
                logEntity.setAddress("未知");
            }
            logMapper.updateById(logEntity);
        });
    }

    private String buildAddr(String region, String city, String country) {
        if ("内网IP".equals(city)) return "内网IP";
        if (!"中国".equals(country)) return country;
        if ("XX".equals(region) && "XX".equals(city)) return "未知";
        if ("XX".equals(region)) return city;
        if ("XX".equals(city)) return region;
        return region.equals(city) ? region : region + " " + city;
    }

    private static IpDetail TryGetIpDetailOrNullTreeTimes(String ip) {
        for (int i = 0; i < 3; i++) {
            IpDetail ipDetail = getIpDetailOrNull(ip);
            if (Objects.nonNull(ipDetail)) {
                return ipDetail;
            }
            log.info("IP 解析重试中，当前次数：{}，IP：{}", i, ip);
            try {
                Thread.sleep(1500); // 略微减少睡眠时间
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        return null;
    }

    public static IpDetail getIpDetailOrNull(String ip) {
        try {
            // 优化核心：增加 timeout(2000)，防止在 Docker 环境中因网络不通导致线程无限挂起
            String body = HttpUtil.createGet(StrUtil.format(ThirdPartyInterfaceConst.TAOBAO_IP_DETAIL, ip))
                    .timeout(2000)
                    .execute()
                    .body();

            IpResult<IpDetail> result = JSONUtil.toBean(body, new TypeReference<IpResult<IpDetail>>() {}, false);
            if (result != null && result.isSuccess()) {
                return result.getData();
            }
        } catch (Exception e) {
            log.warn("调用第三方接口获取 IP 详情失败 [ip: {}]: {}", ip, e.getMessage());
        }
        return null;
    }

    @Override
    public void destroy() throws InterruptedException {
        EXECUTOR.shutdown();
        if (!EXECUTOR.awaitTermination(10, TimeUnit.SECONDS)) {
            log.error("IP 线程池关闭超时");
        }
    }
}
