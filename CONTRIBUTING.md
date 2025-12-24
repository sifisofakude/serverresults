# Contributing to ServerResults

Thank you for your interest in contributing to **ServerResults**! We welcome contributions from anyone, whether it’s bug reports, feature requests, or code improvements.

---

## How to Contribute

### Reporting Bugs
- Open a GitHub Issue with a **clear title and description**.
- Include **steps to reproduce**, expected behavior, and actual behavior.
- Attach logs or code snippets if applicable.

### Suggesting Features
- Open a GitHub Issue labeled as **“Feature Request”**.
- Provide a brief explanation of the use case and expected behavior.
- Include examples if possible.

### Submitting Pull Requests
1. Fork the repository and create a **feature branch**.
2. Make your changes following the existing **code style and structure**.
3. Ensure that your code is **well-documented**, especially public methods.
4. Include **tests or usage examples** if relevant.
5. Submit a Pull Request with a **clear description** of the changes.

---

## Code Style Guidelines
- Java 8+ syntax
- 4-space indentation
- Javadoc comments for all public methods and classes
- Consistent naming and formatting
- Avoid breaking existing API behavior

---

## Testing
- All network calls are blocking; tests should not run on the main/UI thread.
- Test different HTTP methods (GET, POST, PUT, PATCH, DELETE) when adding features.
- Consider error handling and edge cases.

---

## Communication
- Be respectful and constructive in all discussions.
- Ask questions in Issues if you are unsure about design or implementation.

---

## License
All contributions are covered under the **MIT License** of the project.
