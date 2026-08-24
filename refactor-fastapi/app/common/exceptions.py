"""도메인 예외 — HTTP/SQL/stack 을 서비스 계층 밖으로 새기지 않는다 (NFR-006)."""

from __future__ import annotations


class DomainError(Exception):
    def __init__(self, error_code: str, message: str, status_code: int) -> None:
        super().__init__(message)
        self.error_code = error_code
        self.message = message
        self.status_code = status_code


class NotFoundError(DomainError):
    def __init__(self, message: str = "Resource not found") -> None:
        super().__init__("NOT_FOUND", message, 404)


class InvalidColumnError(DomainError):
    def __init__(self, field_name: str) -> None:
        super().__init__(
            "INVALID_COLUMN",
            "Unknown or disallowed field",
            400,
        )
        self.field_name = field_name


class InvalidInputError(DomainError):
    def __init__(self, message: str = "Invalid input") -> None:
        super().__init__("INVALID_INPUT", message, 400)
