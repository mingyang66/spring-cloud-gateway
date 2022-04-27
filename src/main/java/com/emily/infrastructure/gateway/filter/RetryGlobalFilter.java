package com.emily.infrastructure.gateway.filter;

import com.emily.infrastructure.common.utils.json.JSONUtils;
import com.emily.infrastructure.gateway.common.entity.BaseLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;

import static org.springframework.cloud.gateway.filter.factory.RetryGatewayFilterFactory.RETRY_ITERATION_KEY;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR;

/**
 * @author Emily
 * @program: EmilyGateway
 * @description: Retry And Repeat Filter
 * @create: 2021/02/22
 */
public class RetryGlobalFilter implements GlobalFilter, Ordered {
    private static final Logger logger = LoggerFactory.getLogger(RetryGlobalFilter.class);
    /**
     * 优先级顺序
     */
    public int order;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return chain.filter(exchange).doFinally(signalType -> {
            // 获取接口重试次数
            int iteration = exchange.getAttributeOrDefault(RETRY_ITERATION_KEY, 0);
            if (iteration > 0) {
                BaseLogger logEntity = exchange.getAttributeOrDefault(LoggerGlobalFilter.EMILY_LOG_ENTITY, new BaseLogger());
                // 设置请求URL
                URI uri = exchange.getAttribute(GATEWAY_REQUEST_URL_ATTR);
                logEntity.setUrl(uri == null ? null : uri.toString());
                logEntity.setTime(System.currentTimeMillis() - exchange.getAttributeOrDefault(LoggerGlobalFilter.EMILY_REQUEST_TIME, 0L));
                logger.info(JSONUtils.toJSONString(logEntity));
            }
        });
    }

    @Override
    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }
}
