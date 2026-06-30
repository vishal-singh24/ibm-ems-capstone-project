.requestMatchers(HttpMethod.GET,"/api/v1/users")
.hasRole("ADMIN")

.requestMatchers("/api/v1/users/me")
.authenticated()

.requestMatchers(HttpMethod.GET,"/api/v1/users/**")
.authenticated()

.requestMatchers(HttpMethod.PUT,"/api/v1/users/**")
.authenticated()