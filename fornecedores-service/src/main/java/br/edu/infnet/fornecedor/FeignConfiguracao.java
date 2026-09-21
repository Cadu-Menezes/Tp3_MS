package br.edu.infnet.fornecedor;

import feign.RequestInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
public class FeignConfiguracao {
    @Bean
    public RequestInterceptor encaminharToken() {
        return template -> {
            ServletRequestAttributes atributos =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (atributos == null) {
                return;
            }

            HttpServletRequest request = atributos.getRequest();
            String autorizacao = request.getHeader("Authorization");
            if (autorizacao != null && !autorizacao.isBlank()) {
                template.header("Authorization", autorizacao);
            }
        };
    }
}
