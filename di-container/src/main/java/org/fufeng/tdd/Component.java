package org.fufeng.tdd;

import java.lang.annotation.Annotation;

public record Component(Class<?> type, Annotation qualifier) {
}
