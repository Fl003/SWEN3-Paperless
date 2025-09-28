async function handle(res) {
    if (res.status === 404) throw new Error('404')
    if (!res.ok) throw new Error(`HTTP ${res.status}`)
    return res.json()
}

export async function login(username, password) {
    const res = await fetch('/api/v1/login', {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({
            username: username,
            password: password
        })
    })
    return handle(res)
}

export async function fetchWithAuth(url, options = {}) {
    const token = localStorage.getItem("jwt");

    if (!token) {
        // kein Token vorhanden → zurück zum Login
        window.location.href = "/login";
        throw new Error("No token found");
    }

    const isFormData = options.body instanceof FormData;

    const res = await fetch(url, {
        ...options,
        headers: {
            ...options.headers,
            Authorization: `Bearer ${token}`,   // <-- JWT im Header
            ...(isFormData ? {} : { "Content-Type": "application/json" }),
        },
    });

    if (res.status === 401) {
        // Token ungültig oder abgelaufen → ausloggen und weiterleiten
        localStorage.removeItem("jwt");
        window.location.href = "/login";
        throw new Error("Unauthorized");
    }

    // Optional: Bei 204 keine JSON-Antwort
    if (res.status === 204) return null;
    return handle(res);
}