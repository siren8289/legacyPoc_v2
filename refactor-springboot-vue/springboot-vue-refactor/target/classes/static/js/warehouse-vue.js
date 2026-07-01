const { createApp } = Vue;

// 입출고 기능 Vue 앱: 목록/등록 폼 흐름을 처리한다.
createApp({
    data() {
        return {
            rows: [],
            showModal: false,
            form: {
                warehouseName: "",
                productCode: "",
                productName: "",
                productCategory: "",
                inQty: 0,
                outQty: 0,
                currentStock: 0,
                clientName: "",
                status: "ACTIVE"
            }
        };
    },
    methods: {
        async loadRows() {
            const res = await fetch("/api/warehouses");
            const json = await res.json();
            this.rows = json.data || [];
        },
        openModal() {
            this.showModal = true;
        },
        closeModal() {
            this.showModal = false;
        },
        async createRow() {
            await fetch("/api/warehouses", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(this.form)
            });
            this.closeModal();
            this.form = {
                warehouseName: "",
                productCode: "",
                productName: "",
                productCategory: "",
                inQty: 0,
                outQty: 0,
                currentStock: 0,
                clientName: "",
                status: "ACTIVE"
            };
            this.loadRows();
        }
    },
    mounted() {
        this.loadRows();
    }
}).mount("#app");
