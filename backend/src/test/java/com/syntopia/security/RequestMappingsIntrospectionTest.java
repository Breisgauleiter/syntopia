package com.syntopia.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import java.util.List;

@SpringBootTest
@EnabledIfSystemProperty(named = "show.mappings", matches = "true")
class RequestMappingsIntrospectionTest {

    @Autowired
    private List<RequestMappingHandlerMapping> mappings;

    @Test
    void listMappings() {
        System.out.println("-- Registered Request Mappings --");
        mappings.forEach(m -> m.getHandlerMethods().forEach((info, method) -> {
            System.out.println("[HM] " + info + " -> " + method.getMethod().getDeclaringClass().getSimpleName() + "." + method.getMethod().getName());
        }));
        System.out.println("----------------------------------");
    }
}
