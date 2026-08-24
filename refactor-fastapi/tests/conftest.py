from __future__ import annotations

import pytest
from fastapi.testclient import TestClient

from app.config import Settings
from app.main import create_app


@pytest.fixture
def memory_client() -> TestClient:
    return TestClient(create_app(Settings(repository_type="memory")))
