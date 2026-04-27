const { createApp } = Vue;

// 투자자 기능 Vue 앱: 목록 조회와 등록 모달 상호작용을 처리한다.
createApp({
    data() {
        return {
            keyword: "",
            rows: [],
            showModal: false,
            form: {
                investorName: "",
                investorGrade: "",
                totalAmount: 0,
                lastProductName: "",
                screenMemo: ""
            }
        };
    },
    methods: {
        async loadRows() {
            // 목록 조회 후 Vue 상태(rows)를 갱신한다.
            const res = await fetch(`/api/investors?name=${encodeURIComponent(this.keyword || "")}`);
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
            // 등록 요청 후 목록 상태를 다시 갱신한다.
            await fetch("/api/investors", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(this.form)
            });
            this.closeModal();
            this.form = {
                investorName: "",
                investorGrade: "",
                totalAmount: 0,
                lastProductName: "",
                screenMemo: ""
            };
            this.loadRows();
        }
    },
    mounted() {
        this.loadRows();
    }
}).mount("#app");
