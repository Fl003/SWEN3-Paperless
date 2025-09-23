async function handle(res) {
    if (res.status === 404) throw new Error('404')
    if (!res.ok) throw new Error(`HTTP ${res.status}`)
    return res.json()
}

export async function listDocuments() {
    const res = await fetch('/api/v1/documents')
    return handle(res)
}

export async function getDocument(id) {
    const res = await fetch(`/api/v1/documents/${encodeURIComponent(id)}`)
    return handle(res)
}