package sample.spring.book.client.infrastructure;

import java.util.stream.Collectors;

import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestInitializer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

public class PropagateUserContextInitializer implements ClientHttpRequestInitializer {

    @Override
    public void initialize(ClientHttpRequest request) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated()) {

            String userId = String.valueOf(auth.getPrincipal());
            String roles = auth.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.joining(","));

            request.getHeaders().add("User-ID", userId);
            request.getHeaders().add("Roles", roles);
        }
    }
}
