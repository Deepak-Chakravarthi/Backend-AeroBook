 ## DTO & Model Design

The project follows a clean and consistent DTO design approach focused on immutability, 
validation encapsulation, and separation of concerns.

1. Immutable DTOs using Java Records
2. All request and response models are implemented using Java record, ensuring immutability, thread-safety, and reduced boilerplate (no getters/setters, constructors). This makes DTOs predictable and side-effect free across the application.
3. Validation inside DTOs (Self-validating models)
4. Instead of pushing validation logic into service layers, each request DTO contains its own validation methods. This keeps business logic clean and ensures that invalid data is rejected at the boundary itself

## Clear Separation of Concerns
Controllers → handle HTTP layer
DTOs → carry and validate data
Services → contain business logic
This avoids bloated service classes and improves maintainability.

## Example
public record UserUpdateRequest(
String firstName,
String lastName,
String phoneNumber
) {

    public void validate() {
        if (firstName == null || firstName.isBlank()) {
            throw new IllegalArgumentException("First name cannot be empty");
        }
        if (phoneNumber != null && phoneNumber.length() < 10) {
            throw new IllegalArgumentException("Invalid phone number");
        }
    }
}

## Usage in Controller/Service
public UserResponse updateUser(Long id, UserUpdateRequest request) {
request.validate(); // validation at boundary

    User user = userRepository.findById(id)
        .orElseThrow(() -> new NotFoundException("User not found"));

    userMapper.updateEntity(user, request);
    return userMapper.toResponse(userRepository.save(user));
}
## benefits
1. Immutability by design → safer and predictable data flow
2. Validation at entry point → avoids invalid state propagation
3. Cleaner services → focus only on business logic
4. Consistent pattern → easy to scale and maintain across modules