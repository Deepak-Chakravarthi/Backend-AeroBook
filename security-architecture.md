## 🔐 Security Architecture

### Authentication Flow
POST /auth/login
→ AuthenticationManager authenticates credentials
→ JwtTokenProvider generates signed JWT
→ Token returned in AuthResponse
→ Client sends token in Authorization: Bearer <token> header
→ JwtAuthenticationFilter validates token on every request
→ SecurityContext populated with UserPrincipal

### Authorization — AOP-Based Gatekeeper
All authorization is handled by `ControllerSecurityAspect` — a single AOP
interceptor that cuts across every `@RestController` method.
Incoming request
↓
@ExemptAuthorization?   → YES → proceed (public endpoint)
↓ NO
@PreAuthorize present?  → NO  → 403 ENDPOINT_NOT_AUTHORIZED
↓ YES
Authenticated?          → NO  → 401 UNAUTHORIZED
↓ YES
Spring evaluates @PreAuthorize expression → proceed or 403

### Role Hierarchy
SUPER_ADMIN    → full access to everything
AIRLINE_ADMIN  → manage flights, schedules, aircraft, fares
AGENT          → manage bookings on behalf of passengers
PASSENGER      → search flights, manage own bookings

### Annotation Contract
@ExemptAuthorization    → fully public, no token needed
/auth/login, /auth/register, GET /flights
@AuthenticatedEndpoint  → any valid token, no role restriction
GET /users/me, GET /airlines
@PreAuthorize(role)     → specific role required
POST /flights, DELETE /airlines
no annotation           → blocked by default (403)

---
