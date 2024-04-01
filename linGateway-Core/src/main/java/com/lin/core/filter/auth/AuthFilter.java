package com.lin.core.filter.auth;

import com.lin.common.constant.FilterConst;
import com.lin.common.enums.ResponseCode;
import com.lin.common.exception.ResponseException;
import com.lin.common.utils.TimeUtil;
import com.lin.core.context.GatewayContext;
import com.lin.core.filter.Filter;
import com.lin.core.filter.FilterAspect;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.DefaultClaims;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;

/**
 * @author linzj
 * 鉴权过滤器
 */
@Slf4j
@FilterAspect(id = FilterConst.AUTH_FILTER_ID,
        name = FilterConst.AUTH_FILTER_NAME,
        order = FilterConst.AUTH_FILTER_ORDER)
public class AuthFilter implements Filter {
    /**
     * 加密密钥
     */
    private static final String SECRET_KEY ="lin";

    /**
     * cookie键 从对应的cookie中获取这个键 存储的就是我们的token信息
     */
    private static final String COOKIE_NAME = "linGateway-jwt";
    @Override
    public void doFilter(GatewayContext ctx) throws Exception {
        if (ctx.getRule().getFilterConfig(FilterConst.AUTH_FILTER_ID) == null) {
            return;
        }
        String token = ctx.getRequest().getCookie(COOKIE_NAME).value();
        if (StringUtils.isBlank(token)) {
            throw new ResponseException(ResponseCode.UNAUTHORIZED);
        }
        try {
            // 解析useId
            long userId = parseUserId(token);
            // 把用户传给下游
            ctx.getRequest().setUserId(userId);
        } catch (Exception e) {
            throw new ResponseException(ResponseCode.UNAUTHORIZED);
        }

    }

    /**
     * 根据token解析用户id
     * @param token
     * @return
     */
    public long parseUserId(String token) {
        Jwt jwt = Jwts.parser().setSigningKey(SECRET_KEY.getBytes()).parse(token);
        return Long.parseLong(((DefaultClaims) jwt.getBody()).getSubject());
    }

    public static String createToken() {
        return Jwts.builder()
                .setSubject("123456")
                .setIssuedAt(new Date())
                .setIssuer("linGateway")
                .setExpiration(new Date(TimeUtil.getCurrentTimeMills() + 30000*1000))
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY.getBytes())
                .compact();
    }

    public static void main(String[] args) {
        String token = createToken();
        System.out.println(token);
        Jwt jwt = Jwts.parser().setSigningKey(SECRET_KEY.getBytes()).parse(token);
        System.out.println(jwt.getBody().toString());

    }
}
