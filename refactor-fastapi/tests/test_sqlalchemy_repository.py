from __future__ import annotations

from fastapi.testclient import TestClient

from app.config import Settings
from app.main import create_app
from app.persistence.session import create_engine_from_url, create_schema


def test_sqlalchemy_list_and_soft_delete(tmp_path) -> None:
    url = f"sqlite:///{tmp_path / 'parity.db'}"
    create_schema(create_engine_from_url(url))
    client = TestClient(create_app(Settings(repository_type="sqlalchemy", database_url=url)))

    created = client.post("/api/warehouses", json={"warehouseName": "인천창고", "productName": "베어링"})
    warehouse_id = created.json()["warehouseIoId"]
    listed = client.get("/api/warehouses", params={"page": 0, "size": 10}).json()["data"]
    assert listed["totalCount"] == 1
    assert listed["items"][0]["productName"] == "베어링"

    client.delete(f"/api/warehouses/{warehouse_id}")
    after_delete = client.get("/api/warehouses", params={"page": 0, "size": 10}).json()["data"]
    assert after_delete["totalCount"] == 0
    assert client.get(f"/api/warehouses/{warehouse_id}").status_code == 404


def test_sqlalchemy_investor_search(tmp_path) -> None:
    url = f"sqlite:///{tmp_path / 'investors.db'}"
    create_schema(create_engine_from_url(url))
    client = TestClient(create_app(Settings(repository_type="sqlalchemy", database_url=url)))
    client.post("/api/investors", json={"investorName": "김민수", "investorGrade": "VIP"})
    client.post("/api/investors", json={"investorName": "이서연", "investorGrade": "GOLD"})
    rows = client.get("/api/investors", params={"name": "김"}).json()["data"]
    assert [row["investorName"] for row in rows] == ["김민수"]
