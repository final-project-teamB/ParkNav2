package com.sparta.parknav._global.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD}) //메서드에 타겟 지정
@Retention(RetentionPolicy.RUNTIME) //런타임에 영향을 미침
public @interface NoChecking { // 특정 메서드 로깅 제외 어노테이션
}
