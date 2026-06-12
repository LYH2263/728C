package com.steam.enums;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    SUCCESS(0, "操作成功", HttpStatus.OK),

    BAD_REQUEST(1000, "请求参数错误", HttpStatus.BAD_REQUEST),
    PARAM_VALIDATION_FAILED(1001, "参数校验失败", HttpStatus.BAD_REQUEST),
    PARAM_BIND_FAILED(1002, "参数绑定失败", HttpStatus.BAD_REQUEST),
    EMPTY_GAME_SELECTION(1003, "请选择要购买的游戏", HttpStatus.BAD_REQUEST),
    INVALID_RATING(1004, "评分必须在1-5之间", HttpStatus.BAD_REQUEST),

    UNAUTHORIZED(2000, "未授权访问", HttpStatus.UNAUTHORIZED),
    LOGIN_EXPIRED(2001, "登录已过期，请重新登录", HttpStatus.UNAUTHORIZED),
    FORBIDDEN(2002, "没有权限访问", HttpStatus.FORBIDDEN),
    USER_NOT_FOUND(2003, "用户不存在", HttpStatus.OK),
    USERNAME_ALREADY_EXISTS(2004, "用户名已存在", HttpStatus.OK),
    EMAIL_ALREADY_REGISTERED(2005, "邮箱已被注册", HttpStatus.OK),
    WRONG_PASSWORD(2006, "密码错误", HttpStatus.OK),
    ACCOUNT_DISABLED(2007, "账号已被禁用", HttpStatus.OK),
    EMAIL_ALREADY_USED(2008, "邮箱已被其他用户使用", HttpStatus.OK),
    ORDER_NO_PERMISSION(2009, "无权操作此订单", HttpStatus.OK),
    ORDER_VIEW_NO_PERMISSION(2010, "无权查看此订单", HttpStatus.OK),
    REVIEW_NO_PERMISSION(2011, "评论不存在或无权修改", HttpStatus.OK),

    GAME_NOT_FOUND(3001, "游戏不存在", HttpStatus.OK),
    GAME_NOT_FOUND_BY_ID(3002, "游戏不存在: %s", HttpStatus.OK),
    GAME_ALREADY_OWNED(3003, "您已拥有游戏: %s", HttpStatus.OK),
    GAME_ALREADY_OWNED_GENERIC(3004, "您已拥有该游戏", HttpStatus.OK),
    GAME_OUT_OF_STOCK(3005, "游戏库存不足: %s", HttpStatus.OK),
    GAME_OUT_OF_STOCK_GENERIC(3006, "游戏库存不足", HttpStatus.OK),
    GAME_OFF_SHELF(3007, "游戏已下架: %s", HttpStatus.OK),
    PRICE_CHANGED(3008, "游戏价格已变动，请重新下单", HttpStatus.OK),

    ORDER_NOT_FOUND(4001, "订单不存在", HttpStatus.OK),
    INVALID_ORDER_STATUS(4002, "订单状态不正确", HttpStatus.OK),
    CANCEL_NON_PENDING(4003, "只能取消待支付的订单", HttpStatus.OK),

    INSUFFICIENT_BALANCE(5001, "余额不足，请先充值", HttpStatus.OK),
    INSUFFICIENT_BALANCE_GENERIC(5002, "余额不足", HttpStatus.OK),

    REVIEW_NOT_PURCHASED(6001, "您需要购买游戏后才能发表评论", HttpStatus.OK),
    REVIEW_ALREADY_EXISTS(6002, "您已经评论过该游戏", HttpStatus.OK),
    RATE_LIMIT_EXCEEDED(6003, "操作过于频繁，请稍后再试", HttpStatus.TOO_MANY_REQUESTS),

    CART_ALREADY_CONTAINS(7001, "游戏已在购物车中", HttpStatus.OK),
    CART_ITEM_NOT_FOUND(7002, "购物车中没有该游戏", HttpStatus.OK),

    WISHLIST_ALREADY_CONTAINS(8001, "游戏已在愿望单中", HttpStatus.OK),
    WISHLIST_ITEM_NOT_FOUND(8002, "愿望单中没有该游戏", HttpStatus.OK),

    SYSTEM_ERROR(9000, "系统繁忙，请稍后重试", HttpStatus.INTERNAL_SERVER_ERROR),
    UNKNOWN_ERROR(9999, "未知错误", HttpStatus.INTERNAL_SERVER_ERROR);

    private final Integer code;
    private final String message;
    private final HttpStatus httpStatus;

    ErrorCode(Integer code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public String formatMessage(Object... args) {
        return String.format(this.message, args);
    }
}
