from __future__ import annotations

from fastapi.testclient import TestClient


def test_health(memory_client: TestClient) -> None:
    response = memory_client.get("/health")
    assert response.status_code == 200
    assert response.json()["repositoryType"] == "memory"


def test_list_seed_rows(memory_client: TestClient) -> None:
    response = memory_client.get("/api/warehouses")
    assert response.status_code == 200
    rows = response.json()["data"]
    assert [row["warehouseIoId"] for row in rows] == [1001, 1002]
    assert rows[0]["warehouseName"] == "서울1창고"


def test_list_page_uses_projection(memory_client: TestClient) -> None:
    response = memory_client.get("/api/warehouses", params={"page": 0, "size": 1})
    body = response.json()["data"]
    assert body["totalCount"] == 2
    assert body["page"] == 0
    assert body["size"] == 1
    item = body["items"][0]
    assert item["warehouseIoId"] == 1002
    assert set(item.keys()) == {"warehouseIoId", "warehouseName", "productName", "currentStock"}


def test_create_and_detail(memory_client: TestClient) -> None:
    created = memory_client.post("/api/warehouses", json={"warehouseName": "대구3창고", "status": "ACTIVE"})
    assert created.status_code == 200
    warehouse_id = created.json()["warehouseIoId"]
    detail = memory_client.get(f"/api/warehouses/{warehouse_id}")
    assert detail.json()["data"]["warehouseName"] == "대구3창고"
    assert detail.json()["data"]["inQty"] == 0


def test_patch_returns_row_for_client_patch(memory_client: TestClient) -> None:
    patched = memory_client.patch(
        "/api/warehouses/1001",
        json={"fieldName": "warehouseName", "value": "서울1창고-수정"},
    )
    assert patched.status_code == 200
    row = patched.json()["data"]
    assert row["warehouseName"] == "서울1창고-수정"
    assert row["productCode"] == "P-100"


def test_patch_rejects_unknown_column(memory_client: TestClient) -> None:
    response = memory_client.patch(
        "/api/warehouses/1001",
        json={"fieldName": "deletedYn", "value": "Y"},
    )
    assert response.status_code == 400
    body = response.json()
    assert body["errorCode"] == "INVALID_COLUMN"
    assert "stack" not in body
    assert "sql" not in str(body).lower()


def test_delete_then_not_found(memory_client: TestClient) -> None:
    deleted = memory_client.delete("/api/warehouses/1002")
    assert deleted.status_code == 200
    missing = memory_client.get("/api/warehouses/1002")
    assert missing.status_code == 404
    assert missing.json()["errorCode"] == "NOT_FOUND"


def test_not_found_has_no_stack(memory_client: TestClient) -> None:
    response = memory_client.get("/api/warehouses/9999")
    assert response.status_code == 404
    assert set(response.json()) <= {"result", "errorCode", "message", "details"}
