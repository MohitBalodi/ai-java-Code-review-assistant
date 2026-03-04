const API_BASE = "http://localhost:8080/api";
export async function analyzeCode(data) {
    const response = await fetch(`${API_BASE}/analyze`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(data),
    });

    if (!response.ok) {
        throw new Error("Analyze request failed");
    }

    return response.json();
}

export async function optimizeCode(data) {
    const res = await fetch("http://localhost:8080/api/optimize", {
        method: "POST",  // 🔥 CRITICAL
        headers: {
            "Content-Type": "application/json",
        },
        body: JSON.stringify(data),
    });

    if (!res.ok) {
        throw new Error("Optimize request failed");
    }

    return res.json();
}